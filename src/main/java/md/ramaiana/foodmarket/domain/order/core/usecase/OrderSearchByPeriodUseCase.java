package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderListResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for listing the caller's orders over a period.
 */
@UseCase
@RequiredArgsConstructor
public class OrderSearchByPeriodUseCase {

  /**
   * Sort is a free-text request field, so it is constrained to an explicit set of entity properties
   * rather than passed straight to the persistence layer.
   */
  private static final Set<String> SORTABLE_PROPERTIES =
      Set.of("id", "createdAt", "placedAt", "totalSum", "state");

  private final OrderRepository orderRepository;
  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public OrderListResponse execute(@NonNull OrderListRequest request) {
    if (!SORTABLE_PROPERTIES.contains(request.getSortColumn())) {
      throw new BadRequestException(String.format("Unknown sort column '%s'", request.getSortColumn()));
    }
    if (request.getDateFrom() > request.getDateTo()) {
      throw new BadRequestException("dateFrom must not be after dateTo");
    }

    PageRequest pageable = PageRequest.of(request.getPageNo(), request.getPageSize(),
        Sort.by(request.getSortDirection(), request.getSortColumn()));

    Page<OrderEntity> ordersPage = orderRepository.findByClientIdAndDeletedAtIsNullAndCreatedAtBetween(
        orderLoader.getCurrentClientId(),
        Instant.ofEpochMilli(request.getDateFrom()),
        Instant.ofEpochMilli(request.getDateTo()),
        pageable
    );

    List<OrderResponse> orders = responseAssembler.assembleAll(ordersPage.getContent());

    return new OrderListResponse(
        orders,
        ordersPage.getNumber(),
        ordersPage.getSize(),
        ordersPage.getTotalPages(),
        ordersPage.getTotalElements()
    );
  }
}
