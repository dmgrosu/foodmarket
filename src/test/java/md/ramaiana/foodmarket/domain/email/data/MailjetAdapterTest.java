package md.ramaiana.foodmarket.domain.email.data;

import com.jayway.jsonpath.JsonPath;
import md.ramaiana.foodmarket.config.MailjetProperties;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.EmailRecipient;
import md.ramaiana.foodmarket.domain.email.core.request.LoginConfirmationVariables;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class MailjetAdapterTest {

    private MockWebServer server;
    private MailjetAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        MailjetProperties.Sender sender = new MailjetProperties.Sender("noreply@example.com", "Foodmarket");
        MailjetProperties properties = new MailjetProperties(
          true,
          server.url("/").toString(),
          "test-key",
          "test-secret",
          5000,
          10000,
          sender
        );
        adapter = new MailjetAdapter(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void send_should_post_to_mailjet_and_return_message_uuid() throws InterruptedException {
        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "Messages": [
                    {
                      "Status": "success",
                      "To": [
                        {
                          "Email": "user@example.com",
                          "MessageUUID": "msg-uuid-12345"
                        }
                      ]
                    }
                  ]
                }
                """));

        // Act
        EmailSendResponse result = adapter.send(recipient, variables);

        // Assert: the result
        assertThat(result.messageUuid()).isEqualTo("msg-uuid-12345");
        assertThat(result.recipientEmail()).isEqualTo("user@example.com");

        // Assert: what was actually sent on the wire
        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v3.1/send");
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString("test-key:test-secret".getBytes());
        assertThat(request.getHeader("Authorization")).isEqualTo(expectedAuth);

        String body = request.getBody().readUtf8();
        assertThat(((Number) JsonPath.read(body, "$.Messages[0].TemplateID")).longValue())
            .isEqualTo(Long.parseLong(EmailTemplate.LOGIN_CONFIRMATION.getId()));
        assertThat((Boolean) JsonPath.read(body, "$.Messages[0].TemplateLanguage")).isTrue();
        assertThat((String) JsonPath.read(body, "$.Messages[0].Variables.confirmation_url"))
            .isEqualTo("https://app.example.com/confirm?token=abc123");
        assertThat(((Number) JsonPath.read(body, "$.Messages[0].Variables.expires_in_minutes")).intValue())
            .isEqualTo(15);
    }

    @Test
    void send_should_throw_on_http_200_with_error_status() {
        // Arrange
        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "Messages": [
                    {
                      "Status": "error",
                      "Errors": [
                        {
                          "ErrorMessage": "Invalid template ID",
                          "ErrorCode": "mj-0001",
                          "StatusCode": 400
                        }
                      ]
                    }
                  ]
                }
                """));

        // Act & Assert
        assertThatThrownBy(() -> adapter.send(recipient, variables))
            .isInstanceOf(MailException.class)
            .hasMessageContaining("Mailjet rejected message")
            .hasMessageContaining("Invalid template ID");
    }

    @Test
    void send_should_throw_on_http_400() {
        // Arrange
        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        server.enqueue(new MockResponse().setResponseCode(400).setBody("{}"));

        // Act & Assert
        assertThatThrownBy(() -> adapter.send(recipient, variables))
            .isInstanceOf(MailException.class);
    }

    @Test
    void send_should_skip_http_call_when_disabled() {
        // Arrange
        MailjetProperties.Sender sender = new MailjetProperties.Sender("noreply@example.com", "Foodmarket");
        MailjetProperties disabledProps = new MailjetProperties(
            false,
            server.url("/").toString(),
            "test-key",
            "test-secret",
            5000,
            10000,
            sender
        );
        MailjetAdapter disabledAdapter = new MailjetAdapter(disabledProps);

        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        // Act
        EmailSendResponse result = disabledAdapter.send(recipient, variables);

        // Assert
        assertThat(result.messageUuid()).isEqualTo("disabled");
        assertThat(result.recipientEmail()).isEqualTo("user@example.com");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void send_should_throw_when_recipient_result_is_missing() {
        // Arrange: Status success but no To array at all
        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "Messages": [
                    {
                      "Status": "success"
                    }
                  ]
                }
                """));

        // Act & Assert
        assertThatThrownBy(() -> adapter.send(recipient, variables))
            .isInstanceOf(MailException.class)
            .hasMessageContaining("missing recipient result");
    }

    @Test
    void send_should_throw_when_message_uuid_is_blank() {
        // Arrange: To entry present but its MessageUUID is blank
        EmailRecipient recipient = new EmailRecipient("user@example.com", "Test User");
        LoginConfirmationVariables variables = new LoginConfirmationVariables(
            "https://app.example.com/confirm?token=abc123",
            15
        );

        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "Messages": [
                    {
                      "Status": "success",
                      "To": [
                        {
                          "Email": "user@example.com",
                          "MessageUUID": ""
                        }
                      ]
                    }
                  ]
                }
                """));

        // Act & Assert
        assertThatThrownBy(() -> adapter.send(recipient, variables))
            .isInstanceOf(MailException.class)
            .hasMessageContaining("MessageUUID");
    }
}
