package md.ramaiana.foodmarket.service;

public class OrderAlreadyProcessedException extends Exception {
    public OrderAlreadyProcessedException(String message) {
        super(message);
    }
}
