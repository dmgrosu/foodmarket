package md.ramaiana.foodmarket.service;

public class ResetPasswordTokenNotFoundException extends RuntimeException {
    public ResetPasswordTokenNotFoundException(String message) {
        super(message);
    }
}
