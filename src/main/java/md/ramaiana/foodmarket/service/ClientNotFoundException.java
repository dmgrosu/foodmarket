package md.ramaiana.foodmarket.service;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/7/21
 */
public class ClientNotFoundException extends Exception {
    public ClientNotFoundException(String message) {
        super(message);
    }
}
