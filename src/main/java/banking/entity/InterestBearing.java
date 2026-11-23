package banking.entity;

import java.math.BigDecimal;

public interface InterestBearing {
    /** Interest for one monthly payment, rounded to 2 decimal places. */
    BigDecimal calculateInterest();

    /** Credits the monthly interest to the balance and records an INTEREST transaction. */
    void payInterest();
}
