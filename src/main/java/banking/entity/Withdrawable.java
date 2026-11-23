package banking.entity;

import java.math.BigDecimal;

public interface Withdrawable {
    /** Returns true if the withdrawal succeeded, false if funds are insufficient. Never leaves a negative balance. */
    boolean withdraw(BigDecimal amt);
}
