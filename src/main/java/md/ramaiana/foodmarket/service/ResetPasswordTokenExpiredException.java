package md.ramaiana.foodmarket.service;

public class ResetPasswordTokenExpiredException extends RuntimeException {
    public ResetPasswordTokenExpiredException(String message) {
        super(message);
    }
}
