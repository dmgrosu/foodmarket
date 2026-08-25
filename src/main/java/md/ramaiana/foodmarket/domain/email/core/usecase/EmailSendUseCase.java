package md.ramaiana.foodmarket.domain.email.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.domain.email.data.MailjetAdapter;
import md.ramaiana.foodmarket.shared.annotation.UseCase;

/**
 * Use case for sending transactional emails via Mailjet.
 * Vendor-agnostic: callers depend on this, never on the Mailjet adapter or its wire types.
 */
@UseCase
@RequiredArgsConstructor
public class EmailSendUseCase {

    private final MailjetAdapter mailjetAdapter;

    @NonNull
    public EmailSendResponse execute(@NonNull EmailSendRequest request) {
        return mailjetAdapter.send(request.recipient(), request.variables());
    }
}
