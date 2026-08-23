package md.ramaiana.foodmarket.domain.client.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientSearchCriteria;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientSearchUseCase;
import md.ramaiana.foodmarket.domain.client.presentation.voter.ClientAccessVoter;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client controller.
 */
@Validated
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

  // Access voters
  private final ClientAccessVoter accessVoter;

  // Use cases
  private final ClientSearchUseCase clientSearchUseCase;

  /**
   * Find by idno.
   */
  @GetMapping("/findByIdno")
  public ClientResponse findByIdno(@RequestParam("idno") @NonNull String idno) {
    accessVoter.assertCanFindByIdno();
    return clientSearchUseCase.executeByIdno(idno);
  }

  /**
   * Search clients. Administrators only.
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
    return clientSearchUseCase.execute(
        new ClientSearchCriteria(name, idno, pageNo, pageSize, sortColumn, sortDirection));
  }
}
