package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderItemResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderListResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for searching orders by period.
 */
@UseCase
@RequiredArgsConstructor
public class OrderSearchByPeriodUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public OrderListResponse execute(@NonNull OrderListRequest request) {
    if (request.getClientId() == null) {
      throw new BadRequestException("ClientId is required");
    }

    // Validate client
    clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new NotFoundException(String.format("Client with ID [%s] not found", request.getClientId())));

    Instant dateFrom = Instant.ofEpochMilli(request.getDateFrom());
    Instant dateTo = Instant.ofEpochMilli(request.getDateTo());

    PageRequest pageable = PageRequest.of(request.getPageNo(), request.getPageSize(),
        Sort.Direction.valueOf(request.getSortDirection().toString()), request.getSortColumn());

    Page<OrderEntity> ordersPage = orderRepository.findByClientIdAndDeletedAtIsNullAndCreatedAtBetween(
        request.getClientId(),
        dateFrom,
        dateTo,
        pageable
    );

    List<OrderResponse> orders = ordersPage.stream()
        .map(order -> {
          List<OrderItemResponse> items = order.getItems().stream()
              .map(item -> new OrderItemResponse(
                  item,
                  productRepository.findNameById(item.getProductId())
              ))
              .collect(Collectors.toList());
          return new OrderResponse(order, items);
        })
        .collect(Collectors.toList());

    return new OrderListResponse(
        orders,
        ordersPage.getNumber(),
        ordersPage.getSize(),
        ordersPage.getTotalPages()
    );
  }
}
