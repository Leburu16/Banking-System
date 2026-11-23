package banking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Immutable record of one deposit, withdrawal or interest payment (type, amount, date, balance after). */
public final class Transaction {

    private final String transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime date;
    private final BigDecimal balanceAfter;
    private final String description;

    public Transaction(String transactionId, TransactionType type, BigDecimal amount,
                       LocalDateTime date, BigDecimal balanceAfter, String description) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.type = Objects.requireNonNull(type);
        this.amount = Objects.requireNonNull(amount);
        this.date = Objects.requireNonNull(date);
        this.balanceAfter = Objects.requireNonNull(balanceAfter);
        this.description = description == null ? "" : description;
    }

    static Transaction create(TransactionType type, BigDecimal amount, BigDecimal balanceAfter, String description) {
        return new Transaction(UUID.randomUUID().toString(), type, amount,
                LocalDateTime.now(), balanceAfter, description);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }
}
