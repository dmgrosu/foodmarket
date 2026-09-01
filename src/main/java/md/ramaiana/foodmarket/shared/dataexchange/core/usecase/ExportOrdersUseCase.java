package md.ramaiana.foodmarket.shared.dataexchange.core.usecase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.transform.stream.StreamResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.order.data.OrderExportRepository;
import md.ramaiana.foodmarket.domain.order.data.OrderExportRepository.PendingItemRow;
import md.ramaiana.foodmarket.domain.order.data.OrderExportRepository.PendingOrderRow;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpOrderDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.ErpOrderItemDto;
import md.ramaiana.foodmarket.shared.dataexchange.dto.OrdersDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.Marshaller;

/**
 * Writes placed orders into the data-exchange folder for the ERP to collect, and marks them handed
 * over.
 * <p>
 * The outbound half of the exchange the {@code Import*UseCase}s implement inbound, and shaped the
 * same way: this class owns the file, the database work belongs to components that manage their own
 * transactions. That is also what keeps it within the rule that a transaction must never span an
 * external call — the read commits before the file is written, and the file is closed before the
 * orders are marked.
 * <p>
 * We never read a result back. The ERP decides what becomes of an order, and whatever it decides
 * reaches us through the regular import.
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class ExportOrdersUseCase {

    /**
     * Each batch is written to its own file, so a batch can never overwrite one the ERP has not
     * collected yet. The ERP picks up {@code orders-data-*.xml}.
     */
    private static final String ORDERS_DATA_FILE_PREFIX = "orders-data-";
    private static final String ORDERS_DATA_FILE_SUFFIX = ".xml";
    /**
     * Written under this suffix and renamed once complete, so a reader globbing for {@code .xml}
     * cannot pick up a half-written file.
     */
    private static final String IN_PROGRESS_SUFFIX = ".tmp";

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter XML_TIMESTAMP =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Setter
    @Value("${dataFolderPath}")
    private String exchangeFolderPath;
    private final OrderExportRepository orderExportRepository;
    private final Marshaller marshaller;

    /**
     * Export every order awaiting hand-over, as one batch.
     */
    public void execute() {
        try {
            List<PendingOrderRow> orders = orderExportRepository.findPendingOrders();
            if (orders.isEmpty()) {
                log.debug("Skip orders export - no placed orders waiting");
                return;
            }

            List<Integer> orderIds = orders.stream().map(PendingOrderRow::id).toList();
            Map<Integer, List<PendingItemRow>> itemsByOrderId =
                    orderExportRepository.findPendingItems(orderIds).stream()
                            .collect(Collectors.groupingBy(PendingItemRow::orderId));

            Instant exportedAt = Instant.now();
            Path file = writeFile(toOrdersData(orders, itemsByOrderId), exportedAt);

            // Deliberately after the file exists. A batch marked exported but never written is lost
            // silently; a batch written but not marked is written again next cycle, and the ERP drops
            // the repeat on the order id.
            orderExportRepository.markExported(orderIds, exportedAt);

            log.info("Exported {} orders to {}", orders.size(), file.getFileName());
        } catch (Exception ex) {
            log.error("Error while exporting orders: {}", ex.getMessage(), ex);
        }
    }

    @NonNull
    private OrdersDataDto toOrdersData(@NonNull List<PendingOrderRow> orders,
                                       @NonNull Map<Integer, List<PendingItemRow>> itemsByOrderId) {
        return new OrdersDataDto(orders.stream()
                .map(order -> toOrder(order, itemsByOrderId.getOrDefault(order.id(), List.of())))
                .toList());
    }

    @NonNull
    private ErpOrderDto toOrder(@NonNull PendingOrderRow order, @NonNull List<PendingItemRow> items) {
        float totalWeight = 0f;
        for (PendingItemRow item : items) {
            totalWeight += item.weight() == null ? 0f : item.weight();
        }

        return new ErpOrderDto(
                order.id(),
                format(order.createdAt()),
                format(order.placedAt()),
                order.clientErpCode(),
                order.clientIdno(),
                order.storageErpCode(),
                order.priceType(),
                order.totalSum() == null ? 0f : order.totalSum(),
                totalWeight,
                items.stream().map(this::toItem).toList());
    }

    @NonNull
    private ErpOrderItemDto toItem(@NonNull PendingItemRow item) {
        return new ErpOrderItemDto(
                item.productErpCode(),
                nullSafe(item.quantity()),
                nullSafe(item.price()),
                nullSafe(item.sum()),
                nullSafe(item.weight()));
    }

    private float nullSafe(Float value) {
        return value == null ? 0f : value;
    }

    private String format(Instant instant) {
        return instant == null ? null : XML_TIMESTAMP.format(instant);
    }

    /**
     * Marshal into the exchange folder, atomically.
     *
     * @return the path the ERP will see.
     */
    @NonNull
    private Path writeFile(@NonNull OrdersDataDto ordersData, @NonNull Instant exportedAt)
            throws IOException {
        Path folder = Path.of(exchangeFolderPath);
        Files.createDirectories(folder);

        String name = ORDERS_DATA_FILE_PREFIX + FILE_TIMESTAMP.format(exportedAt) + ORDERS_DATA_FILE_SUFFIX;
        Path target = folder.resolve(name);
        Path inProgress = folder.resolve(name + IN_PROGRESS_SUFFIX);

        try (OutputStream out = Files.newOutputStream(inProgress)) {
            marshaller.marshal(ordersData, new StreamResult(out));
        }

        return Files.move(inProgress, target,
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
