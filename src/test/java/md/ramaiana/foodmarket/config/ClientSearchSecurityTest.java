package md.ramaiana.foodmarket.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Covers authorization on GET /client/search. There is no path-based rule for it in
 * {@link SecurityConfig} — the admin check lives solely in ClientAccessVoter#assertCanSearch.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClientSearchSecurityTest {

  private static final String SEARCH_URL = "/client/search";

  @Autowired
  MockMvc mockMvc;

  @Test
  @WithAnonymousUser
  void anonymous_user_is_rejected() throws Exception {
    MvcResult result = mockMvc.perform(get(SEARCH_URL)).andReturn();

    assertThat(result.getResponse().getStatus()).isIn(401, 403);
  }

  @Test
  @WithMockUser(roles = "USER")
  void non_admin_is_rejected() throws Exception {
    MvcResult result = mockMvc.perform(get(SEARCH_URL)).andReturn();

    assertThat(result.getResponse().getStatus()).isIn(401, 403);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void request_reaches_the_voter() throws Exception {
    // The mock principal is a Spring Security User rather than an AppUserEntity, so the voter
    // stops it at getCurrentUser(). Getting that far proves the filter chain let it through and
    // the voter is what decides.
    MvcResult result = mockMvc.perform(get(SEARCH_URL)).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
    assertThat(result.getResponse().getContentAsString()).contains("Invalid authentication principal");
  }
}
