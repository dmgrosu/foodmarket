package md.ramaiana.foodmarket.domain.auth.presentation.controller;

import java.util.HashMap;
import java.util.Map;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /auth/profile and PUT /auth/updateProfile — the signed-in user reading and editing their own
 * account.
 */
class AuthProfileTest extends AbstractRegistrationEndpointTest {

  private static final String PROFILE_URL = "/auth/profile";
  private static final String UPDATE_PROFILE_URL = "/auth/updateProfile";

  /** Map.of rejects null values, and the point of several of these cases is sending one. */
  private static Map<String, Object> updateBody(String firstName, String lastName, String language) {
    Map<String, Object> body = new HashMap<>();
    body.put("firstName", firstName);
    body.put("lastName", lastName);
    body.put("language", language);
    return body;
  }

  @Test
  void should_return_the_signed_in_users_own_account() throws Exception {
    AppUserEntity user = authenticateAs(Role.USER);

    get(PROFILE_URL)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.language").value("ru"))
        .andExpect(jsonPath("$.state").value("ACTIVE"))
        .andExpect(jsonPath("$.roles[0]").value("USER"));
  }

  /**
   * 403, not 401: no AuthenticationEntryPoint is configured, so Spring Security's
   * ExceptionTranslationFilter answers an anonymous rejection with Forbidden. What matters here is
   * that the request never reaches the controller — the status itself is app-wide behaviour, not
   * something this endpoint chooses.
   */
  @Test
  void should_reject_an_anonymous_read() throws Exception {
    authenticateAsAnonymous();

    get(PROFILE_URL).andExpect(status().isForbidden());
  }

  @Test
  void should_persist_a_new_language() throws Exception {
    authenticateAs(Role.USER);

    put(UPDATE_PROFILE_URL, updateBody(null, null, "ro"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.language").value("ro"));

    // Read it back rather than trusting the write's own response.
    get(PROFILE_URL).andExpect(jsonPath("$.language").value("ro"));
  }

  @Test
  void should_fall_back_to_russian_for_an_unknown_language_tag() throws Exception {
    authenticateAs(Role.USER);

    put(UPDATE_PROFILE_URL, updateBody(null, null, "xx"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.language").value("ru"));
  }

  @Test
  void should_persist_a_first_and_last_name() throws Exception {
    authenticateAs(Role.USER);

    put(UPDATE_PROFILE_URL, updateBody("Ion", "Popescu", "ru"))
        .andExpect(status().isOk());

    get(PROFILE_URL)
        .andExpect(jsonPath("$.firstName").value("Ion"))
        .andExpect(jsonPath("$.lastName").value("Popescu"));
  }

  @Test
  void should_store_a_blank_name_as_null_rather_than_whitespace() throws Exception {
    authenticateAs(Role.USER);
    put(UPDATE_PROFILE_URL, updateBody("Ion", "Popescu", "ru")).andExpect(status().isOk());

    put(UPDATE_PROFILE_URL, updateBody("   ", "", "ru")).andExpect(status().isOk());

    get(PROFILE_URL)
        .andExpect(jsonPath("$.firstName").doesNotExist())
        .andExpect(jsonPath("$.lastName").doesNotExist());
  }

  @Test
  void should_expose_the_name_captured_at_registration() throws Exception {
    // The whole point of wiring firstName/lastName through: the sign-up form used to discard them.
    String email = uniqueEmail();
    Map<String, Object> body = new HashMap<>(registerBody(email));
    body.put("firstName", "Maria");
    body.put("lastName", "Ionescu");
    post(REGISTER_URL, body).andExpect(status().isOk());

    activate(email);

    get(PROFILE_URL)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Maria"))
        .andExpect(jsonPath("$.lastName").value("Ionescu"));
  }

  @Test
  void should_reject_an_anonymous_update() throws Exception {
    authenticateAsAnonymous();

    put(UPDATE_PROFILE_URL, updateBody(null, null, "ro")).andExpect(status().isForbidden());
  }

  @Test
  void should_reject_a_blank_language() throws Exception {
    authenticateAs(Role.USER);

    put(UPDATE_PROFILE_URL, updateBody(null, null, "  ")).andExpect(status().isBadRequest());
  }
}
