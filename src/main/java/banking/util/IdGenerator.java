package banking.util;

import java.security.SecureRandom;

/** Generates customer IDs and account numbers. Account numbers are also enforced unique by the database. */
public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    public static String customerId() {
        return "C" + randomDigits(8);
    }

    /** prefix comes from AccountType, e.g. "SA" for savings. */
    public static String accountNumber(String prefix) {
        return prefix + randomDigits(10);
    }

    private static String randomDigits(int length) {
        long bound = 1L;
        for (int i = 0; i < length; i++) {
            bound *= 10;
        }
        return String.format("%0" + length + "d", Math.floorMod(RANDOM.nextLong(), bound));
    }
}
