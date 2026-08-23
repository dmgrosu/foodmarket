package md.ramaiana.foodmarket.domain.client.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.AdminClientSearchUseCase;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientSearchCriteria;
import md.ramaiana.foodmarket.domain.client.presentation.voter.AdminClientAccessVoter;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Client controller.
 */
@Validated
@RestController
@RequestMapping("/admin/client")
@RequiredArgsConstructor
public class AdminClientController {

  // Access voters
  private final AdminClientAccessVoter accessVoter;

  // Use cases
  private final AdminClientSearchUseCase adminClientSearchUseCase;

  /**
   * Search clients.
   */
  @GetMapping("/search")
  public PagedResponse<ClientResponse> search(
      @RequestParam(value = "name", required = false) @Nullable String name,
      @RequestParam(value = "idno", required = false) @Nullable String idno,
      @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "25") @Min(1) int pageSize,
      @RequestParam(value = "sortColumn", defaultValue = "name") String sortColumn,
      @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection) {
    accessVoter.assertCanSearch();
    return adminClientSearchUseCase.execute(
        new ClientSearchCriteria(name, idno, pageNo, pageSize, sortColumn, sortDirection));
  }
}
