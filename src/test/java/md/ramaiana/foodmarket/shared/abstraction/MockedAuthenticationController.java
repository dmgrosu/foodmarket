package md.ramaiana.foodmarket.shared.abstraction;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.auth.core.usecase.JwtCreateTokenUseCase;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


/**
 * Base class for controller integration tests. Subclasses call {@link #post} / {@link #get} and get a
 * real, signed JWT attached automatically once they have called {@link #authenticateAs}.
 * <p>
 * The identity is a genuine {@code app_user} row plus a token minted by {@link JwtCreateTokenUseCase},
 * not a {@code @WithMockUser} stand-in. That matters: {@code @WithMockUser} installs a Spring Security
 * {@code User} as the principal, and every {@code AccessVoter} check does
 * {@code principal instanceof AppUserEntity} — so a mock principal is rejected before any real
 * authorization logic runs, and the test proves nothing about the endpoint.
 * <p>
 * Deliberately NOT {@code @Transactional}: the request must see committed data, and use cases
 * annotated {@code Propagation.NEVER} throw outright if a test-managed transaction is open on the
 * request thread. Users created here are deleted in {@link #cleanUpAuthenticatedUsers()}; subclasses
 * that create their own rows are responsible for their own cleanup.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class MockedAuthenticationController {

  protected static final String TEST_PASSWORD = "Password123";

  @Autowired
  protected MockMvc mockMvc;

  /** No ObjectMapper bean is exposed in this test context, so build a plain one. */
  protected final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtCreateTokenUseCase jwtCreateTokenUseCase;

  private final List<Integer> authenticatedUserIds = new ArrayList<>();

  /**
   * The Authorization header value for the current identity, or null while unauthenticated.
   * JwtCreateTokenUseCase already returns a "Bearer "-prefixed token, so it is sent verbatim.
   */
  private String authorizationHeader;

  @AfterEach
  void cleanUpAuthenticatedUsers() {
    authenticatedUserIds.forEach(id -> appUserRepository.findById(id).ifPresent(appUserRepository::delete));
    authenticatedUserIds.clear();
    authorizationHeader = null;
  }

  /**
   * Create an ACTIVE user with the given roles and authenticate every subsequent request as them.
   *
   * @return the persisted user, for tests that need its id or email.
   */
  protected AppUserEntity authenticateAs(Role... roles) {
    return authenticateAs(null, roles);
  }

  /**
   * As {@link #authenticateAs(Role...)}, but with a client attached to the identity.
   * <p>
   * Registration always attaches one, and every customer-facing endpoint orders on behalf of the
   * authenticated user's client, so a test of one of those needs an identity shaped the way a real
   * one is. The plain overload leaves it null, which is what an administrator looks like.
   *
   * @param clientId the {@code client} row to attach, or null for an identity with no client.
   */
  protected AppUserEntity authenticateAs(Integer clientId, Role... roles) {
    AppUserEntity user = new AppUserEntity(
        "auth-" + UUID.randomUUID() + "@example.com",
        passwordEncoder.encode(TEST_PASSWORD),
        null,
        null,
        UserState.ACTIVE,
        Language.RU
    );
    for (Role role : roles) {
      user.addRole(role);
    }
    if (clientId != null) {
      user = user.withClient(AggregateReference.to(clientId));
    }

    AppUserEntity savedUser = appUserRepository.save(user);
    authenticatedUserIds.add(savedUser.getId());
    authorizationHeader = jwtCreateTokenUseCase.execute(savedUser);
    return savedUser;
  }

  /**
   * Authenticate every subsequent request as an already-persisted user, rather than the fresh one
   * {@link #authenticateAs(Role...)} creates. The caller owns that user's lifecycle and cleanup.
   */
  protected void authenticateAs(@NonNull AppUserEntity user) {
    authorizationHeader = jwtCreateTokenUseCase.execute(user);
  }

  /**
   * Drop the current identity, so subsequent requests are sent anonymously.
   */
  protected void authenticateAsAnonymous() {
    authorizationHeader = null;
  }

  /**
   * POST a JSON body to the given url.
   */
  protected ResultActions post(String url, Object body) throws Exception {
    return mockMvc.perform(authorize(MockMvcRequestBuilders.post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))));
  }

  /**
   * PUT a JSON body to the given url.
   */
  protected ResultActions put(String url, Object body) throws Exception {
    return mockMvc.perform(authorize(MockMvcRequestBuilders.put(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(body))));
  }

  /**
   * PUT to the given url with no body.
   */
  protected ResultActions put(String url) throws Exception {
    return mockMvc.perform(authorize(MockMvcRequestBuilders.put(url)));
  }

  /**
   * GET the given url.
   */
  protected ResultActions get(String url) throws Exception {
    return get(url, Map.of());
  }

  /**
   * DELETE the given url.
   */
  protected ResultActions delete(String url) throws Exception {
    return mockMvc.perform(authorize(MockMvcRequestBuilders.delete(url)));
  }

  /**
   * GET the given url with query parameters.
   */
  protected ResultActions get(String url, Map<String, String> params) throws Exception {
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(url);
    params.forEach(request::param);
    return mockMvc.perform(authorize(request));
  }

  private MockHttpServletRequestBuilder authorize(MockHttpServletRequestBuilder request) {
    return authorizationHeader == null ? request : request.header("Authorization", authorizationHeader);
  }
}
