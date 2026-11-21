
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    private String accountNumber;
    protected BigDecimal balance;
    private String branch;
    private LocalDateTime dateOpened;

    private List<Transaction> transactions;

    public Account(String accountNumber, BigDecimal balance,
                   String branch, LocalDateTime dateOpened) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.branch = branch;
        this.dateOpened = dateOpened;
        this.transactions = new ArrayList<>();
    }

    public void deposit(BigDecimal amt) {
        deposit(amt, "Deposit");
    }

    public void deposit(BigDecimal amt, String note) {
        if (amt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit must be positive.");
        }

        balance = balance.add(amt);

        transactions.add(new Transaction(
                java.util.UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                amt,
                LocalDateTime.now(),
                balance,
                note
        ));
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal b) {
        balance = b;
    }

    public String getBranch() {
        return branch;
    }

    public LocalDateTime getDateOpened() {
        return dateOpened;
    }

    public abstract String getAccountType();

    public List<Transaction> getTransactions() {
        return transactions;
    }

    protected void recordTransaction(TransactionType type,
                                     BigDecimal amount,
                                     String description) {
        transactions.add(new Transaction(
                java.util.UUID.randomUUID().toString(),
                type,
                amount,
                LocalDateTime.now(),
                balance,
                description
        ));
    }
}

