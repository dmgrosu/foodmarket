package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.shared.abstraction.MockedAuthenticationController;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /user/search.
 */
class AdminUserSearchTest extends MockedAuthenticationController {

  private static final String SEARCH_URL = "/user/search";

  @Test
  void should_return_a_page_for_an_admin() throws Exception {
    authenticateAs(Role.ADMIN);

    get(SEARCH_URL)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray());
  }

  @Test
  void should_forbid_a_non_admin() throws Exception {
    authenticateAs(Role.USER);

    get(SEARCH_URL).andExpect(status().isForbidden());
  }

  @Test
  void should_reject_anonymous_requests() throws Exception {
    // No path-based rule for /user/** in SecurityConfig — an unauthenticated request is stopped by
    // the filter chain itself, before any voter runs, and Spring Security's default entry point
    // for that case is 403, not 401.
    authenticateAsAnonymous();

    get(SEARCH_URL).andExpect(status().isForbidden());
  }

  @Test
  void should_filter_by_state() throws Exception {
    authenticateAs(Role.ADMIN);

    get(SEARCH_URL, Map.of("state", "ACTIVE"))
        .andExpect(status().isOk());
  }

  @Test
  void should_reject_an_unknown_sort_column() throws Exception {
    authenticateAs(Role.ADMIN);

    get(SEARCH_URL, Map.of("sortColumn", "passwd"))
        .andExpect(status().isBadRequest());
  }
}
