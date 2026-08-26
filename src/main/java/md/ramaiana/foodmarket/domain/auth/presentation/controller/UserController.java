package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.handler.AppUserActivateRequestHandler;
import md.ramaiana.foodmarket.domain.auth.core.response.AppUserResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AppUserSearchCriteria;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AppUserSearchUseCase;
import md.ramaiana.foodmarket.domain.auth.presentation.voter.UserAccessVoter;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.response.PagedResponse;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User controller. All endpoints are administrator-only.
 */
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  // Access voters
  private final UserAccessVoter accessVoter;

  // Use cases
  private final AppUserSearchUseCase appUserSearchUseCase;

  // Request handlers
  private final AppUserActivateRequestHandler appUserActivateRequestHandler;

  /**
   * Search users.
   */
  @GetMapping("/search")
  public PagedResponse<AppUserResponse> search(
      @RequestParam(value = "email", required = false) @Nullable String email,
      @RequestParam(value = "state", required = false) @Nullable UserState state,
      @RequestParam(value = "pageNo", defaultValue = "0") @Min(0) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "25") @Min(1) int pageSize,
      @RequestParam(value = "sortColumn", defaultValue = "createdAt") String sortColumn,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection) {
    accessVoter.assertCanSearch();
    return appUserSearchUseCase.execute(
        new AppUserSearchCriteria(email, state, pageNo, pageSize, sortColumn, sortDirection));
  }

  /**
   * Activate a confirmed user.
   */
  @PutMapping("/activate/{userId}")
  public AppUserResponse activate(@PathVariable("userId") @NonNull Integer userId) {
    accessVoter.assertCanActivate();
    return appUserActivateRequestHandler.handle(userId);
  }
}
