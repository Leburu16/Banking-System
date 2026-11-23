package banking.entity;

import banking.util.BankingException;
import banking.util.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base account. Holds the shared data and the rules that every account follows.
 * The balance is only changed through credit() and tryDebit(), so every change is recorded as a transaction.
 */
public abstract class Account {

    private final String accountNumber;
    private final String customerId;
    private final String branch;
    private final LocalDateTime dateOpened;
    protected BigDecimal balance;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, String customerId, String branch,
                      LocalDateTime dateOpened, BigDecimal balance) {
        this.accountNumber = requireText(accountNumber, "Account number");
        this.customerId = requireText(customerId, "Customer");
        this.branch = requireText(branch, "Branch");
        this.dateOpened = Objects.requireNonNull(dateOpened, "Date opened is required.");
        this.balance = Money.round(Objects.requireNonNull(balance, "Balance is required."));
        if (this.balance.signum() < 0) {
            throw new BankingException("Balance cannot be negative.");
        }
    }

    public void deposit(BigDecimal amt) {
        deposit(amt, "Deposit");
    }

    public void deposit(BigDecimal amt, String note) {
        credit(validateAmount(amt), TransactionType.DEPOSIT, note);
    }

    public abstract AccountType getType();

    public final String getAccountType() {
        return getType().getDisplayName();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getBranch() {
        return branch;
    }

    public LocalDateTime getDateOpened() {
        return dateOpened;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /** The most recent transaction, or null if there are none. Used by the DAO to persist it. */
    public Transaction getLastTransaction() {
        return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1);
    }

    /** Replaces the in-memory history with the rows loaded from the database. */
    public void loadTransactions(List<Transaction> stored) {
        transactions.clear();
        transactions.addAll(stored);
    }

    protected void credit(BigDecimal amount, TransactionType type, String description) {
        balance = balance.add(amount);
        transactions.add(Transaction.create(type, amount, balance, description));
    }

    /** Debits only if funds are sufficient. Returns false and changes nothing otherwise. */
    protected boolean tryDebit(BigDecimal amt, String description) {
        BigDecimal amount = validateAmount(amt);
        if (balance.compareTo(amount) < 0) {
            return false;
        }
        balance = balance.subtract(amount);
        transactions.add(Transaction.create(TransactionType.WITHDRAWAL, amount, balance, description));
        return true;
    }

    protected static BigDecimal validateAmount(BigDecimal amt) {
        if (amt == null) {
            throw new BankingException("Amount is required.");
        }
        if (amt.signum() <= 0) {
            throw new BankingException("Amount must be greater than zero.");
        }
        if (amt.stripTrailingZeros().scale() > Money.SCALE) {
            throw new BankingException("Amount can have at most 2 decimal places.");
        }
        return Money.round(amt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BankingException(field + " is required.");
        }
        return value.trim();
    }
}
