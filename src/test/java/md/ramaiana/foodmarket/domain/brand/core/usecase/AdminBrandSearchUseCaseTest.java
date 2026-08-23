package md.ramaiana.foodmarket.domain.brand.core.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import md.ramaiana.foodmarket.domain.brand.core.response.BrandResponse;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandRepository;
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

/**
 * Sort column whitelisting itself is validated end-to-end against H2 in
 * {@code BrandRepositoryTest}, since it lives in {@code BrandRepositoryImpl} next to the SQL it
 * protects. This test covers the use case's own responsibility: turning criteria into a
 * {@link Pageable} and a {@code Page} into a {@link md.ramaiana.foodmarket.shared.response.PagedResponse}.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminBrandSearchUseCaseTest {

  @Mock
  BrandRepository brandRepository;

  @InjectMocks
  AdminBrandSearchUseCase useCase;

  @Test
  void should_build_pageable_from_criteria_and_map_page_to_paged_response() {
    BrandEntity entity = new BrandEntity(1, "Cola", "erp-cola", Instant.now(), null);
    when(brandRepository.search(eq("cola"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 25), 1));

    PagedResponse<BrandResponse> response =
        useCase.execute(new BrandSearchCriteria("cola", 0, 25, "name", Sort.Direction.ASC));

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().getFirst().name()).isEqualTo("Cola");
    assertThat(response.totalElements()).isEqualTo(1);
    assertThat(response.totalPages()).isEqualTo(1);
    assertThat(response.currentPage()).isEqualTo(0);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(brandRepository).search(eq("cola"), pageableCaptor.capture());
    Pageable used = pageableCaptor.getValue();
    assertThat(used.getPageNumber()).isEqualTo(0);
    assertThat(used.getPageSize()).isEqualTo(25);
    assertThat(used.getSort().getOrderFor("name")).isNotNull();
    assertThat(Objects.requireNonNull(used.getSort().getOrderFor("name")).getDirection()).isEqualTo(Sort.Direction.ASC);
  }

  @Test
  void should_reject_sort_column_outside_the_whitelist() {
    assertThatThrownBy(() -> useCase.execute(new BrandSearchCriteria(null, 0, 25, "passwd", Sort.Direction.ASC)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("passwd");

    verifyNoInteractions(brandRepository);
  }
}
