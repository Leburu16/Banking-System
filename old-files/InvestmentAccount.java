
import java.math.BigDecimal;

public class InvestmentAccount extends Account
        implements InterestBearing, Withdrawable {

    private static final BigDecimal RATE = new BigDecimal("0.05");
    private static final BigDecimal MIN_OPENING =
            new BigDecimal("500.00");

    public InvestmentAccount(String accountNumber, BigDecimal balance,
                             String branch, java.time.LocalDateTime dateOpened) {
        super(accountNumber, balance, branch, dateOpened);

        if (balance.compareTo(MIN_OPENING) < 0) {
            throw new IllegalArgumentException(
                    "Minimum opening balance is 500.00");
        }
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
                "Investment account interest"
        );
    }

    @Override
    public boolean withdraw(BigDecimal amt) {
        if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (balance.compareTo(amt) >= 0) {
            balance = balance.subtract(amt);

            recordTransaction(
                    TransactionType.WITHDRAWAL,
                    amt,
                    "Investment account withdrawal"
            );

            return true;
        }

        return false;
    }

    @Override
    public String getAccountType() {
        return "Investment Account";
    }
}

