
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private String transactionID;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime date;
    private BigDecimal balanceAfter;
    private String description;

    public Transaction(String transactionID, TransactionType type,
                       BigDecimal amount, LocalDateTime date,
                       BigDecimal balanceAfter, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }
}

