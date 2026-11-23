
import java.math.BigDecimal;

public class SavingsAccount extends Account implements InterestBearing {

    private static final BigDecimal RATE = new BigDecimal("0.0005");

    public SavingsAccount(String accountNumber, BigDecimal balance,
                          String branch, java.time.LocalDateTime dateOpened) {
        super(accountNumber, balance, branch, dateOpened);
    }

    @Override
    public BigDecimal calculateInterest() {
        return balance.multiply(RATE);
    }

    @Override
    public void payInterest() {
        BigDecimal interest = calculateInterest();

        balance = balance.add(interest);

        recordTransaction(
                TransactionType.INTEREST,
                interest,
                "Savings account interest"
        );
    }

    public String getAccountType() {
        return "Savings Account";
    }
}

