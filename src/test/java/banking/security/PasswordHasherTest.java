package banking.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** NFR-1: passwords are hashed, never stored in plain text. */
class PasswordHasherTest {

    @Test
    void hashDoesNotContainThePasswordAndVerifiesCorrectly() {
        String stored = PasswordHasher.hash("password123");
        assertFalse(stored.contains("password123"));
        assertTrue(PasswordHasher.verify("password123", stored));
        assertFalse(PasswordHasher.verify("password124", stored));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        assertNotEquals(PasswordHasher.hash("password123"), PasswordHasher.hash("password123"));
    }
}
