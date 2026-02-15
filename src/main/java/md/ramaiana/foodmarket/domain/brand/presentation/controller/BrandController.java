package md.ramaiana.foodmarket.domain.brand.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Brand", description = "Brand management endpoints")
public class BrandController {

  // Access voters
  private final BrandAccessVoter accessVoter;

  // Use cases
  private final BrandSearchUseCase brandSearchUseCase;

  /**
   * Get all brands.
   */
  @GetMapping("/getAll")
  @Operation(
      operationId = "getAllBrands",
      summary = "Get all brands",
      description = "Retrieve a list of all available brands"
  )
  public List<BrandResponse> getAll() {
    accessVoter.assertCanGetAll();
    return brandSearchUseCase.execute();
  }
}