package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /auth/confirmEmail.
 */
class AuthConfirmEmailTest extends AbstractRegistrationEndpointTest {

  @Test
  void should_confirm_a_pending_registration() throws Exception {
    String email = uniqueEmail();
    String rawToken = registerAndCaptureToken(email);

    post(CONFIRM_EMAIL_URL, confirmBody(rawToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.state").value("CONFIRMED"));

    AppUserEntity user = appUserRepository.findByEmail(email).orElseThrow();
    assertThat(user.getState()).isEqualTo(UserState.CONFIRMED);
    assertThat(latestTokenFor(email).isConfirmed()).isTrue();
  }

  @Test
  void should_stay_successful_when_the_link_is_opened_twice() throws Exception {
    // Clicking the link twice, or a mail scanner prefetching it, is the common case — not an error.
    String email = uniqueEmail();
    String rawToken = registerAndCaptureToken(email);
    post(CONFIRM_EMAIL_URL, confirmBody(rawToken)).andExpect(status().isOk());
    Instant firstConfirmedAt = latestTokenFor(email).getConfirmedAt();

    post(CONFIRM_EMAIL_URL, confirmBody(rawToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("CONFIRMED"));

    assertThat(latestTokenFor(email).getConfirmedAt())
        .as("replay must not re-stamp the token")
        .isEqualTo(firstConfirmedAt);
  }

  @Test
  void should_return_a_session_that_is_inert_until_an_administrator_approves() throws Exception {
    String email = uniqueEmail();
    String rawToken = registerAndCaptureToken(email);

    String body = post(CONFIRM_EMAIL_URL, confirmBody(rawToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("CONFIRMED"))
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andReturn().getResponse().getContentAsString();
    String sessionToken = objectMapper.readTree(body).get("token").asText();

    // Still only CONFIRMED, so the token must not open an authenticated endpoint yet — otherwise
    // it would walk straight past the administrator approval gate.
    mockMvc.perform(MockMvcRequestBuilders.get("/storage").header("Authorization", sessionToken))
        .andExpect(status().isUnauthorized());

    // Approve, and the very same token starts working — no credentials retyped.
    AppUserEntity user = appUserRepository.findByEmail(email).orElseThrow();
    appUserRepository.save(user.withState(UserState.ACTIVE));

    mockMvc.perform(MockMvcRequestBuilders.get("/storage").header("Authorization", sessionToken))
        .andExpect(status().isOk());
  }

  @Test
  void should_reject_an_unknown_token() throws Exception {
    post(CONFIRM_EMAIL_URL, confirmBody("does-not-exist"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_an_expired_token() throws Exception {
    String email = uniqueEmail();
    String rawToken = registerAndCaptureToken(email);
    RegistrationTokenEntity token = latestTokenFor(email);
    overwriteToken(token, Instant.now().minusSeconds(1), token.getCreatedAt());

    post(CONFIRM_EMAIL_URL, confirmBody(rawToken))
        .andExpect(status().isBadRequest());

    assertThat(appUserRepository.findByEmail(email).orElseThrow().getState())
        .isEqualTo(UserState.PENDING_CONFIRMATION);
  }

  @Test
  void should_reject_a_blank_token() throws Exception {
    post(CONFIRM_EMAIL_URL, Map.of("confirmationToken", ""))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_reject_a_missing_token() throws Exception {
    post(CONFIRM_EMAIL_URL, Map.of())
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_be_reachable_without_authentication() throws Exception {
    // No Authorization header — proves the SecurityConfig permitAll + JwtFilter wiring.
    // Reaching the 400 (rather than a 401/403) is the proof the request got through.
    authenticateAsAnonymous();

    post(CONFIRM_EMAIL_URL, confirmBody("whatever"))
        .andExpect(status().isBadRequest());
  }
}
