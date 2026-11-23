package banking.controller;

import banking.dao.AccountDAO;
import banking.dao.Database;
import banking.entity.Account;
import banking.entity.InterestBearing;
import banking.util.BankingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The monthly interest job (the System Scheduler in the use case diagram).
 * Follows the state diagram per account: Calculating, Validating, Crediting, then Completed.
 * A failure is recorded as Failed and left unpaid, so the next run retries it.
 * Each account has its own transaction, so one failure does not affect the others.
 */
public final class InterestController {

    public record RunSummary(int accountsPaid, List<String> failures) {
    }

    private final AccountDAO accountDAO = new AccountDAO();

    public RunSummary runMonthlyInterest() {
        List<String> numbers = Database.read(conn -> accountDAO.findAllAccountNumbers(conn));
        int paid = 0;
        List<String> failures = new ArrayList<>();
        for (String number : numbers) {
            try {
                boolean credited = Database.inTransaction(conn -> payOne(conn, number));
                if (credited) {
                    paid++;
                }
            } catch (BankingException e) {
                failures.add(number + ": " + e.getMessage());
            }
        }
        return new RunSummary(paid, failures);
    }

    private boolean payOne(Connection conn, String number) throws SQLException {
        Optional<Account> found = accountDAO.findAccount(conn, number);
        if (found.isEmpty()) {
            return false;
        }
        Account account = found.get();
        if (!(account instanceof InterestBearing bearing)) {
            return false;
        }

        // Validating: the balance must be sound before any interest is added.
        BigDecimal before = account.getBalance();
        if (before.signum() < 0) {
            throw new BankingException("Balance is negative, so interest was not paid.");
        }

        // Calculating and crediting.
        BigDecimal interest = bearing.calculateInterest();
        if (interest.signum() <= 0) {
            return false;
        }
        bearing.payInterest();
        accountDAO.updateAccount(conn, account, before);
        return true;
    }
}
