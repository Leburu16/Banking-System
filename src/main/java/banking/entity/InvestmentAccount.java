package banking.entity;

import banking.util.BankingException;
import banking.util.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Investment: 5% monthly interest, withdrawals allowed, opens only with at least BWP 500.00. */
public class InvestmentAccount extends Account implements InterestBearing, Withdrawable {

    public static final BigDecimal MONTHLY_RATE = new BigDecimal("0.05");
    public static final BigDecimal MIN_OPENING = new BigDecimal("500.00");

    public InvestmentAccount(String accountNumber, String customerId, String branch,
                             LocalDateTime dateOpened, BigDecimal balance) {
        super(accountNumber, customerId, branch, dateOpened, balance);
    }

    /** The BWP 500 minimum applies only when opening, not to later balances. */
    public static InvestmentAccount open(String accountNumber, String customerId, String branch,
                                         LocalDateTime now, BigDecimal initialDeposit) {
        if (initialDeposit == null || initialDeposit.compareTo(MIN_OPENING) < 0) {
            throw new BankingException("An Investment account needs an initial deposit of at least BWP 500.00.");
        }
        InvestmentAccount account = new InvestmentAccount(accountNumber, customerId, branch, now, BigDecimal.ZERO);
        account.deposit(initialDeposit, "Opening deposit");
        return account;
    }

    @Override
    public AccountType getType() {
        return AccountType.INVESTMENT;
    }

    @Override
    public BigDecimal calculateInterest() {
        return Money.round(getBalance().multiply(MONTHLY_RATE));
    }

    @Override
    public void payInterest() {
        BigDecimal interest = calculateInterest();
        if (interest.signum() > 0) {
            credit(interest, TransactionType.INTEREST, "Investment monthly interest");
        }
    }

    @Override
    public boolean withdraw(BigDecimal amt) {
        return tryDebit(amt, "Investment withdrawal");
    }
}
