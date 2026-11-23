package banking.entity;

import banking.util.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Savings: 0.05% monthly interest. No withdrawals (it does not implement Withdrawable). */
public class SavingsAccount extends Account implements InterestBearing {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.0005");

    public SavingsAccount(String accountNumber, String customerId, String branch,
                          LocalDateTime dateOpened, BigDecimal balance) {
        super(accountNumber, customerId, branch, dateOpened, balance);
    }

    /** Opens a new savings account. An initial deposit is optional. */
    public static SavingsAccount open(String accountNumber, String customerId, String branch,
                                      LocalDateTime now, BigDecimal initialDeposit) {
        SavingsAccount account = new SavingsAccount(accountNumber, customerId, branch, now, BigDecimal.ZERO);
        if (initialDeposit != null && initialDeposit.signum() > 0) {
            account.deposit(initialDeposit, "Opening deposit");
        }
        return account;
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal calculateInterest() {
        return Money.round(getBalance().multiply(MONTHLY_RATE));
    }

    @Override
    public void payInterest() {
        BigDecimal interest = calculateInterest();
        if (interest.signum() > 0) {
            credit(interest, TransactionType.INTEREST, "Savings monthly interest");
        }
    }
}
