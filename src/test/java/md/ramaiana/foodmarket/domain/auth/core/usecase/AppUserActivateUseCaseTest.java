package md.ramaiana.foodmarket.domain.auth.core.usecase;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppUserActivateUseCaseTest {

  @Mock
  AppUserFindByIdUseCase appUserFindByIdUseCase;
  @Mock
  AppUserRepository appUserRepository;
  @Mock
  AccountActivatedMailUseCase accountActivatedMailUseCase;

  @InjectMocks
  AppUserActivateUseCase useCase;

  private static AppUserEntity user(UserState state) {
    return new AppUserEntity(1, "user@example.com", "hash", null, null,
        Instant.now(), state, Language.RU, new HashSet<>(), null);
  }

  @Test
  void executeTransactionalEffect_should_activate_a_confirmed_user() {
    when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.CONFIRMED));
    when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AppUserEntity result = useCase.executeTransactionalEffect(1);

    assertThat(result.getState()).isEqualTo(UserState.ACTIVE);
    verify(appUserRepository).save(any());
  }

  @ParameterizedTest
  @EnumSource(value = UserState.class, names = "CONFIRMED", mode = EnumSource.Mode.EXCLUDE)
  void executeTransactionalEffect_should_reject_every_state_other_than_confirmed(UserState state) {
    when(appUserFindByIdUseCase.execute(1)).thenReturn(user(state));

    assertThatThrownBy(() -> useCase.executeTransactionalEffect(1))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining(state.name());

    verify(appUserRepository, never()).save(any());
  }

  @Test
  void executeSideEffects_should_delegate_to_the_mail_use_case() {
    AppUserEntity activated = user(UserState.ACTIVE);
    when(accountActivatedMailUseCase.execute(activated)).thenReturn(true);

    boolean sent = useCase.executeSideEffects(activated);

    assertThat(sent).isTrue();
    verify(accountActivatedMailUseCase).execute(activated);
  }
}
