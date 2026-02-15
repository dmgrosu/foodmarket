package md.ramaiana.foodmarket.domain.client.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Client", description = "Client management endpoints")
public class ClientController {

  // Access voters
  private final ClientAccessVoter accessVoter;

  // Use cases
  private final ClientFindByIdnoUseCase clientFindByIdnoUseCase;

  /**
   * Find by idno.
   */
  @GetMapping("/findByIdno")
  @Operation(
      operationId = "findClientByIdno",
      summary = "Find client by ID number",
      description = "Retrieve a client using their identification number"
  )
  public ClientResponse findByIdno(@RequestParam("idno") @NonNull String idno) {
    accessVoter.assertCanFindByIdno();
    return clientFindByIdnoUseCase.execute(idno);
  }
}