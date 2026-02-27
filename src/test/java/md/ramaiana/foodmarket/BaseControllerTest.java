package md.ramaiana.foodmarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import md.ramaiana.foodmarket.domain.auth.core.usecase.JwtCreateTokenUseCase;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public abstract class BaseControllerTest extends BaseTest {

  private static final String AUTH_HEADER = "Authorization";

  protected static MockMvc mockMvc;

  protected ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private JwtCreateTokenUseCase jwtCreateTokenUseCase;

  @BeforeAll
  public static void initBaseControllerTest(@Autowired WebApplicationContext webApplicationContext) {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();
  }


  protected String authToken(AppUserEntity user) {
    return jwtCreateTokenUseCase.execute(user);
  }

  protected ResultActions doGet(String url) throws Exception {
    return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
  }

  protected ResultActions doGet(String url, AppUserEntity user) throws Exception {
    return mockMvc.perform(get(url)
        .header(AUTH_HEADER, authToken(user))
        .contentType(MediaType.APPLICATION_JSON));
  }

  protected ResultActions doPost(String url, Object body) throws Exception {
    return mockMvc.perform(post(url)
        .content(toJson(body))
        .contentType(MediaType.APPLICATION_JSON));
  }

  protected ResultActions doPost(String url, Object body, AppUserEntity user) throws Exception {
    return mockMvc.perform(post(url)
        .header(AUTH_HEADER, authToken(user))
        .content(toJson(body))
        .contentType(MediaType.APPLICATION_JSON));
  }

  protected ResultActions doPut(String url, Object body, AppUserEntity user) throws Exception {
    MockHttpServletRequestBuilder builder = put(url)
        .header(AUTH_HEADER, authToken(user))
        .contentType(MediaType.APPLICATION_JSON);
    if (body != null) {
      builder.content(toJson(body));
    }
    return mockMvc.perform(builder);
  }

  protected ResultActions doDelete(String url, AppUserEntity user) throws Exception {
    return mockMvc.perform(delete(url)
        .header(AUTH_HEADER, authToken(user))
        .contentType(MediaType.APPLICATION_JSON));
  }

  protected ResultActions doRequest(MockHttpServletRequestBuilder builder, AppUserEntity user) throws Exception {
    return mockMvc.perform(builder
        .header(AUTH_HEADER, authToken(user))
        .contentType(MediaType.APPLICATION_JSON));
  }

  protected String toJson(Object obj) throws Exception {
    return objectMapper.writeValueAsString(obj);
  }

}
