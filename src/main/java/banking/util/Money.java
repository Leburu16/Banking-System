package banking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** NFR-4: every BWP amount is a BigDecimal with exactly 2 decimal places. */
public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Parses user-entered text such as "250" or "250.50". Rejects non-numbers and more than 2 decimals. */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            throw new BankingException("Please enter an amount.");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new BankingException("Amount must be a number, for example 250.00.");
        }
        if (value.stripTrailingZeros().scale() > SCALE) {
            throw new BankingException("Amount can have at most 2 decimal places.");
        }
        return round(value);
    }
}
