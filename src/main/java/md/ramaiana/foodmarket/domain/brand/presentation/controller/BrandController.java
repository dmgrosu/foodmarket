package md.ramaiana.foodmarket.domain.brand.presentation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.brand.core.response.BrandResponse;
import md.ramaiana.foodmarket.domain.brand.core.usecase.BrandSearchUseCase;
import md.ramaiana.foodmarket.domain.brand.presentation.voter.BrandAccessVoter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Brand controller.
 */
@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {

  // Access voters
  private final BrandAccessVoter accessVoter;

  // Use cases
  private final BrandSearchUseCase brandSearchUseCase;

  /**
   * Get all brands.
   */
  @GetMapping("/getAll")
  public List<BrandResponse> getAll() {
    accessVoter.assertCanGetAll();
    return brandSearchUseCase.execute();
  }
}