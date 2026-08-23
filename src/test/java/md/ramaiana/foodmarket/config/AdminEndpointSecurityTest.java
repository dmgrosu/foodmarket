package md.ramaiana.foodmarket.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Covers the {@code /admin/**} rule in {@link SecurityConfig}. The filter chain runs before the
 * controller, so it — not the access voter — is what a non-admin actually hits.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminEndpointSecurityTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  @WithAnonymousUser
  void anonymous_user_is_rejected() throws Exception {
    MvcResult result = mockMvc.perform(get("/admin/brand/search")).andReturn();

    assertThat(result.getResponse().getStatus()).isIn(401, 403);
  }

  @Test
  @WithMockUser(roles = "USER")
  void non_admin_is_rejected_by_the_filter_chain() throws Exception {
    MvcResult result = mockMvc.perform(get("/admin/brand/search")).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(403);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void admin_passes_the_filter_chain_and_reaches_the_voter() throws Exception {
    // The mock principal is a Spring Security User, not an AppUserEntity, so the request gets
    // past hasRole("ADMIN") and is then stopped by AccessVoter.getCurrentUser(). Reaching the
    // voter at all is the proof that the filter rule let an admin through.
    MvcResult result = mockMvc.perform(get("/admin/brand/search")).andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
    assertThat(result.getResponse().getContentAsString()).contains("Invalid authentication principal");
  }
}
