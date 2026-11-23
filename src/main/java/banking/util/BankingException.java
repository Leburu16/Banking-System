package banking.util;

/** A business-rule or user-input problem. The message is safe to show to the customer. */
public class BankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
