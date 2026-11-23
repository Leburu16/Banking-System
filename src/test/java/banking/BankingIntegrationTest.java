package banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import banking.controller.AccountController;
import banking.controller.InterestController;
import banking.controller.LoginController;
import banking.dao.Database;
import banking.entity.Account;
import banking.entity.AccountType;
import banking.entity.Customer;
import banking.util.BankingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration test (Part B, Module Integration and Testing): one full customer journey through
 * the controllers, the DAO classes and the database. Runs on an in-memory H2 database.
 */
class BankingIntegrationTest {

    private static final LoginController LOGIN = new LoginController();
    private static final AccountController ACCOUNTS = new AccountController();
    private static final InterestController INTEREST = new InterestController();

    @BeforeAll
    static void setUp() {
        Database.initSchema();
    }

    @Test
    void customerJourneyFromRegistrationToInterestAndIsolation() {
        // Registration and login (FR: register, authenticate; NFR-1 hashing)
        Customer alice = LOGIN.register("Alice", "Mokgosi", "Francistown", "74111222",
                "alice@example.com", "alice", "secret123");
        assertThrows(BankingException.class, () -> LOGIN.register("Alice", "Other", "Maun", "74111333",
                "other@example.com", "alice", "secret456"));
        assertThrows(BankingException.class, () -> LOGIN.login("alice", "wrong-password"));
        Customer loggedIn = LOGIN.login("alice", "secret123");
        assertEquals(alice.getCustomerId(), loggedIn.getCustomerId());

        String id = alice.getCustomerId();

        // Savings: deposit allowed, withdrawal rejected (FR: reject withdrawals from Savings)
        Account savings = ACCOUNTS.openAccount(id, AccountType.SAVINGS, "1000.00", null, null);
        ACCOUNTS.deposit(id, savings.getAccountNumber(), "250.00");
        assertThrows(BankingException.class,
                () -> ACCOUNTS.withdraw(id, savings.getAccountNumber(), "10"));

        // Cheque: needs employer details, allows withdrawals, no overdraft
        assertThrows(BankingException.class,
                () -> ACCOUNTS.openAccount(id, AccountType.CHEQUE, "300", null, null));
        Account cheque = ACCOUNTS.openAccount(id, AccountType.CHEQUE, "300", "Alice Co", "Francistown");
        ACCOUNTS.withdraw(id, cheque.getAccountNumber(), "100");
        assertThrows(BankingException.class,
                () -> ACCOUNTS.withdraw(id, cheque.getAccountNumber(), "1000"));

        // Investment: BWP 500 minimum to open
        assertThrows(BankingException.class,
                () -> ACCOUNTS.openAccount(id, AccountType.INVESTMENT, "100", null, null));

        // Monthly interest job: savings 1250.00 x 0.05% = 0.625, rounded HALF_UP to 0.63
        INTEREST.runMonthlyInterest();
        Account afterInterest = ACCOUNTS.listAccounts(id).stream()
                .filter(a -> a.getAccountNumber().equals(savings.getAccountNumber()))
                .findFirst().orElseThrow();
        BigDecimal expected = new BigDecimal("1250.00").multiply(new BigDecimal("0.0005"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1250.00").add(expected), afterInterest.getBalance());

        // History: opening deposit, deposit, interest (NFR: every change is recorded)
        assertEquals(3, ACCOUNTS.history(id, savings.getAccountNumber()).size());

        // Isolation: another customer cannot see or touch Alice's accounts (NFR-2)
        Customer bob = LOGIN.register("Bob", "Kgosi", "Gaborone", "71999888",
                "bob@example.com", "bob", "bobpass99");
        assertTrue(ACCOUNTS.listAccounts(bob.getCustomerId()).isEmpty());
        assertThrows(BankingException.class,
                () -> ACCOUNTS.deposit(bob.getCustomerId(), savings.getAccountNumber(), "1"));
    }
}
