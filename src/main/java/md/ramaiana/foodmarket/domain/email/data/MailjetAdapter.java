package md.ramaiana.foodmarket.domain.email.data;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.MessageResult;
import com.mailjet.client.transactional.response.SendEmailError;
import com.mailjet.client.transactional.response.SendEmailsResponse;
import com.mailjet.client.transactional.response.SentMessageStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.config.MailjetProperties;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.EmailRecipient;
import md.ramaiana.foodmarket.domain.email.core.request.EmailTemplateVariables;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mailjet Send API v3.1 adapter, built on the official com.mailjet:mailjet-client SDK.
 * Encapsulates all Mailjet-specific logic: client construction, template resolution, error handling.
 */
@Slf4j
@Component
@EnableConfigurationProperties(MailjetProperties.class)
public class MailjetAdapter {

    private static final String DISABLED_MESSAGE_UUID = "disabled";
    private static final int ERROR_MESSAGE_MAX_LENGTH = 100;

    private final MailjetProperties properties;
    private final MailjetClient client;

    public MailjetAdapter(MailjetProperties properties) {
        this.properties = properties;
        this.client = properties.enabled() ? buildClient(properties) : null;
    }

    /**
     * Send a templated email to a recipient.
     *
     * @param recipient the email recipient (email, optional display name)
     * @param variables template-specific variables (e.g., confirmation link, expiry)
     * @return the result including Mailjet's message UUID
     * @throws MailException if sending fails
     */
    @NonNull
    public EmailSendResponse send(@NonNull EmailRecipient recipient, @NonNull EmailTemplateVariables variables) {
        EmailTemplate template = variables.template();
        long templateId = Long.parseLong(template.getId());

        // If Mailjet is disabled (local dev, CI), log and return synthetic success
        if (!properties.enabled()) {
            log.info("Mailjet disabled - skipping template {} (id {}), recipient {}",
                template, templateId, recipient.email());
            return new EmailSendResponse(DISABLED_MESSAGE_UUID, recipient.email());
        }

        TransactionalEmail message = TransactionalEmail.builder()
            .from(new SendContact(properties.sender().email(), properties.sender().name()))
            .to(new SendContact(recipient.email(), recipient.name()))
            .templateID(templateId)
            .templateLanguage(true)
            .variables(variables.variables())
            .build();

        log.info("Sending email via Mailjet - template {}, id {}", template, templateId);
        SendEmailsResponse response;
        try {
            response = SendEmailsRequest.builder().message(message).build().sendWith(client);
        } catch (MailjetException ex) {
            throw new MailException("Mailjet call failed for template " + template, ex);
        }

        MessageResult[] messages = response.getMessages();
        if (messages == null || messages.length == 0) {
            throw new MailException("Mailjet returned empty response");
        }

        MessageResult result = messages[0];

        // Check per-message status: HTTP 200 with a per-message error is common and silent
        if (result.getStatus() != SentMessageStatus.SUCCESS) {
            throw new MailException("Mailjet rejected message: " + extractErrorDetail(result));
        }

        // The message UUID lives on the recipient entry, not on the message itself
        if (result.getTo() == null || result.getTo().isEmpty()) {
            throw new MailException("Mailjet response missing recipient result");
        }
        String messageUuid = result.getTo().getFirst().getMessageUUID();
        if (messageUuid == null || messageUuid.isBlank()) {
            throw new MailException("Mailjet response missing MessageUUID");
        }

        log.info("Email sent successfully - template {}, uuid {}", template, messageUuid);
        return new EmailSendResponse(messageUuid, recipient.email());
    }

    /**
     * Build the MailjetClient once. Called only when mailjet.enabled=true, so credentials
     * are guaranteed non-blank by MailjetProperties' compact constructor.
     */
    private static MailjetClient buildClient(MailjetProperties properties) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
            .readTimeout(Duration.ofMillis(properties.readTimeoutMs()))
            .build();

        return new MailjetClient(ClientOptions.builder()
            .apiKey(properties.apiKey())
            .apiSecretKey(properties.secretKey())
            .baseUrl(properties.baseUrl())
            .okHttpClient(httpClient)
            .build());
    }

    /**
     * Extract the first error message from Mailjet's response, truncated.
     */
    private String extractErrorDetail(MessageResult result) {
        SendEmailError[] errors = result.getErrors();
        if (errors != null && errors.length > 0) {
            String msg = errors[0].getErrorMessage();
            if (msg != null) {
                return msg.length() > ERROR_MESSAGE_MAX_LENGTH
                    ? msg.substring(0, ERROR_MESSAGE_MAX_LENGTH) + "…"
                    : msg;
            }
        }
        return "Status: " + result.getStatus();
    }
}
