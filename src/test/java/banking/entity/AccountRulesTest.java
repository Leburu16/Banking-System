package banking.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import banking.util.BankingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Business rules from the Part A requirements, tested on the entity classes (no database). */
class AccountRulesTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void depositIncreasesBalanceAndRecordsATransaction() {
        SavingsAccount s = SavingsAccount.open("SA1", "C1", "Gaborone", NOW, new BigDecimal("100"));
        s.deposit(new BigDecimal("50.5"));
        assertEquals(new BigDecimal("150.50"), s.getBalance());
        assertEquals(TransactionType.DEPOSIT, s.getLastTransaction().getType());
        assertEquals(2, s.getTransactions().size());
    }

    @Test
    void rejectsZeroNegativeAndMoreThanTwoDecimals() {
        SavingsAccount s = SavingsAccount.open("SA2", "C1", "Gaborone", NOW, BigDecimal.ZERO);
        assertThrows(BankingException.class, () -> s.deposit(BigDecimal.ZERO));
        assertThrows(BankingException.class, () -> s.deposit(new BigDecimal("-5")));
        assertThrows(BankingException.class, () -> s.deposit(new BigDecimal("10.123")));
    }

    @Test
    void savingsAccountCannotWithdraw() {
        SavingsAccount s = SavingsAccount.open("SA3", "C1", "Gaborone", NOW, new BigDecimal("100"));
        assertFalse(((Object) s) instanceof Withdrawable);
    }

    @Test
    void investmentAccountNeedsAtLeastBwp500ToOpen() {
        assertThrows(BankingException.class,
                () -> InvestmentAccount.open("IA1", "C1", "Gaborone", NOW, new BigDecimal("499.99")));
        assertDoesNotThrow(
                () -> InvestmentAccount.open("IA2", "C1", "Gaborone", NOW, new BigDecimal("500.00")));
    }

    @Test
    void withdrawalCannotOverdraw() {
        ChequeAccount c = ChequeAccount.open("CA1", "C1", "Gaborone", NOW,
                new BigDecimal("100"), "ABC Company", "Gaborone");
        assertFalse(c.withdraw(new BigDecimal("150")));
        assertEquals(new BigDecimal("100.00"), c.getBalance());
        assertTrue(c.withdraw(new BigDecimal("40")));
        assertEquals(new BigDecimal("60.00"), c.getBalance());
    }

    @Test
    void chequeAccountNeedsEmployerDetails() {
        assertThrows(BankingException.class,
                () -> ChequeAccount.open("CA2", "C1", "Gaborone", NOW, BigDecimal.ZERO, "", ""));
    }

    @Test
    void savingsInterestIsZeroPointZeroFivePercentMonthly() {
        SavingsAccount s = SavingsAccount.open("SA4", "C1", "Gaborone", NOW, new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("0.50"), s.calculateInterest());
        s.payInterest();
        assertEquals(new BigDecimal("1000.50"), s.getBalance());
    }

    @Test
    void investmentInterestIsFivePercentMonthly() {
        InvestmentAccount i = InvestmentAccount.open("IA3", "C1", "Gaborone", NOW, new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("50.00"), i.calculateInterest());
        i.payInterest();
        assertEquals(new BigDecimal("1050.00"), i.getBalance());
    }

    @Test
    void chequeAccountsEarnNoInterest() {
        ChequeAccount c = ChequeAccount.open("CA3", "C1", "Gaborone", NOW, new BigDecimal("100"),
                "ABC Company", "Gaborone");
        assertFalse(((Object) c) instanceof InterestBearing);
    }
}
