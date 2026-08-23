package md.ramaiana.foodmarket.domain.product.core.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import md.ramaiana.foodmarket.domain.product.core.response.ProductResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminProductSearchUseCaseTest {

  @Mock
  ProductRepository productRepository;

  @InjectMocks
  AdminProductSearchUseCase useCase;

  @Test
  void should_pass_filters_through_and_map_page_to_paged_response() {
    ProductEntity entity = new ProductEntity(1, "Cola", "pcs", 1f, "erp-cola", null, 1f, 10, 20,
        Instant.now(), null, null, Set.of());
    when(productRepository.search(eq("cola"), eq(10), eq(20), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 25), 1));

    PagedResponse<ProductResponse> response = useCase.execute(
        new AdminProductSearchCriteria("cola", 10, 20, 0, 25, "name", Sort.Direction.ASC));

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().getFirst().name()).isEqualTo("Cola");
    assertThat(response.totalElements()).isEqualTo(1);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(productRepository).search(eq("cola"), eq(10), eq(20), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(25);
  }

  @Test
  void should_reject_sort_column_outside_the_whitelist() {
    assertThatThrownBy(() -> useCase.execute(new AdminProductSearchCriteria(null, null, null, 0, 25, "passwd", Sort.Direction.ASC)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("passwd");

    verifyNoInteractions(productRepository);
  }
}
