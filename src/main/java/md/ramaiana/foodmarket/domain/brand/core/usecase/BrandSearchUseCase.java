package md.ramaiana.foodmarket.domain.brand.core.usecase;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.brand.core.response.BrandResponse;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for searching brands.
 */
@UseCase
@RequiredArgsConstructor
public class BrandSearchUseCase {

  private final BrandRepository brandRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public List<BrandResponse> execute() {
    List<BrandEntity> brands = brandRepository.findAll();
    return brands.stream()
        .map(BrandResponse::new)
        .collect(Collectors.toList());
  }
}
