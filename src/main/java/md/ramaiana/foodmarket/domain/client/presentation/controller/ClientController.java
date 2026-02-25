package md.ramaiana.foodmarket.domain.client.presentation.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdnoUseCase;
import md.ramaiana.foodmarket.domain.client.presentation.voter.ClientAccessVoter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client controller.
 */
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

  // Access voters
  private final ClientAccessVoter accessVoter;

  // Use cases
  private final ClientFindByIdnoUseCase clientFindByIdnoUseCase;

  /**
   * Find by idno.
   */
  @GetMapping("/findByIdno")
  public ClientResponse findByIdno(@RequestParam("idno") @NonNull String idno) {
    accessVoter.assertCanFindByIdno();
    return clientFindByIdnoUseCase.execute(idno);
  }
}