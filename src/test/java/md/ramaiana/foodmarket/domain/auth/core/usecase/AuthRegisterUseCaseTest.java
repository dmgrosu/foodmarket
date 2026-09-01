package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthRegisterUseCaseTest {

    @Mock
    AppUserRepository appUserRepository;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    ClientFindByIdUseCase clientFindByIdUseCase;
    @Mock
    RegistrationTokenIssueUseCase registrationTokenIssueUseCase;
    @Mock
    RegistrationConfirmationMailUseCase registrationConfirmationMailUseCase;

    @InjectMocks
    AuthRegisterUseCase useCase;

    @Captor
    ArgumentCaptor<AppUserEntity> savedUserCaptor;

    private static RegisterRequest request(Integer clientId) {
        return request(clientId, null);
    }

    private static RegisterRequest request(Integer clientId, String language) {
        return new RegisterRequest("user@example.com", "raw-password", null, null, clientId, language);
    }

    @Test
    void preExecute_should_reject_a_duplicate_email() {
        when(appUserRepository.findByEmail("user@example.com"))
            .thenReturn(Optional.of(mockExistingUser()));

        assertThatThrownBy(() -> useCase.preExecute(request(null)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("user@example.com");
    }

    @Test
    void preExecute_should_pass_when_email_is_free() {
        when(appUserRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        useCase.preExecute(request(null));
    }

    @Test
    void executeTransactionalEffect_should_save_pending_confirmation_user_and_issue_a_token() {
        when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        TransactionalEffectResult pending = new TransactionalEffectResult(
            mockExistingUser(), "https://app.example.com/confirmEmail?confirmationToken=abc", 24);
        when(registrationTokenIssueUseCase.execute(any())).thenReturn(pending);

        TransactionalEffectResult result = useCase.executeTransactionalEffect(request(null));

        verify(appUserRepository).save(savedUserCaptor.capture());
        AppUserEntity savedUser = savedUserCaptor.getValue();
        assertThat(savedUser.getState()).isEqualTo(UserState.PENDING_CONFIRMATION);
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
        assertThat(savedUser.hasClient()).isFalse();
        assertThat(result).isEqualTo(pending);
        verifyNoInteractions(clientFindByIdUseCase);
    }

    @Test
    void executeTransactionalEffect_should_ignore_a_zero_clientId() {
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrationTokenIssueUseCase.execute(any()))
            .thenReturn(new TransactionalEffectResult(mockExistingUser(), "url", 24));

        useCase.executeTransactionalEffect(request(0));

        verifyNoInteractions(clientFindByIdUseCase);
        verify(appUserRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().hasClient()).isFalse();
    }

    @Test
    void executeTransactionalEffect_should_resolve_a_positive_clientId() {
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(clientFindByIdUseCase.execute(any())).thenReturn(org.mockito.Mockito.mock(ClientEntity.class));
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrationTokenIssueUseCase.execute(any()))
            .thenReturn(new TransactionalEffectResult(mockExistingUser(), "url", 24));

        useCase.executeTransactionalEffect(request(42));

        verify(clientFindByIdUseCase).execute(any());
        verify(appUserRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().hasClient()).isTrue();
    }

    @Test
    void executeTransactionalEffect_should_store_the_requested_language() {
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrationTokenIssueUseCase.execute(any()))
            .thenReturn(new TransactionalEffectResult(mockExistingUser(), "url", 24));

        useCase.executeTransactionalEffect(request(null, "ro"));

        verify(appUserRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getLanguage()).isEqualTo(Language.RO);
    }

    @Test
    void executeTransactionalEffect_should_fall_back_to_russian_without_a_language() {
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrationTokenIssueUseCase.execute(any()))
            .thenReturn(new TransactionalEffectResult(mockExistingUser(), "url", 24));

        useCase.executeTransactionalEffect(request(null, null));

        verify(appUserRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getLanguage()).isEqualTo(Language.RU);
    }

    @Test
    void executeSideEffects_should_delegate_to_the_mail_use_case() {
        TransactionalEffectResult pending = new TransactionalEffectResult(mockExistingUser(), "url", 24);
        when(registrationConfirmationMailUseCase.execute(pending)).thenReturn(true);

        boolean sent = useCase.executeSideEffects(pending);

        assertThat(sent).isTrue();
        verify(registrationConfirmationMailUseCase).execute(pending);
    }

    private static AppUserEntity mockExistingUser() {
        return new AppUserEntity(1, "user@example.com", "hash", null, null,
            Instant.now(), UserState.PENDING_CONFIRMATION, Language.RU, new HashSet<>(), null);
    }
}
