// Shared between SignIn, SignUp, Profile and ResetPassword. Validation errors are stored in component state as
// {field, code} — never as a pre-resolved message — so that getErrorForField() can translate
// them fresh on every render. Resolving the message once at validateInput() time would freeze
// it in whatever language was active at submit, and it would stay stale after the user
// switches languages without touching the field again.
export const VALIDATION_MESSAGE_KEYS = {
    EMAIL_EMPTY: 'auth.validation.emailRequired',
    EMAIL_INVALID: 'auth.validation.emailInvalid',
    PASSWORD_EMPTY: 'auth.validation.passwordRequired',
    CURRENT_PASSWORD_EMPTY: 'auth.validation.currentPasswordRequired',
    PASSWORD_NOT_MATCH: 'auth.validation.passwordNotMatch',
    PASSWORD_NOT_STRONG: 'auth.validation.passwordNotStrong',
};
