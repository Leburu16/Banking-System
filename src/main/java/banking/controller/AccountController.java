package banking.controller;

import banking.dao.AccountDAO;
import banking.dao.Database;
import banking.entity.Account;
import banking.entity.AccountType;
import banking.entity.ChequeAccount;
import banking.entity.InvestmentAccount;
import banking.entity.SavingsAccount;
import banking.entity.Transaction;
import banking.entity.Withdrawable;
import banking.util.BankingException;
import banking.util.IdGenerator;
import banking.util.Money;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Opening accounts, deposits, withdrawals and history.
 * Every method checks that the account belongs to the logged-in customer (NFR-2).
 */
public final class AccountController {

    private static final String DEFAULT_BRANCH = "Gaborone Branch";

    private final AccountDAO accountDAO = new AccountDAO();

    public List<Account> listAccounts(String customerId) {
        return Database.read(conn -> accountDAO.findAccountsByCustomer(conn, customerId));
    }

    public Account openAccount(String customerId, AccountType type, String initialDepositText,
                               String employerName, String employerAddress) {
        if (type == null) {
            throw new BankingException("Choose an account type.");
        }
        BigDecimal initial = isBlank(initialDepositText) ? BigDecimal.ZERO : Money.parse(initialDepositText);
        if (initial.signum() < 0) {
            throw new BankingException("Initial deposit cannot be negative.");
        }

        String number = IdGenerator.accountNumber(type.getPrefix());
        LocalDateTime now = LocalDateTime.now();
        Account account = switch (type) {
            case SAVINGS -> SavingsAccount.open(number, customerId, DEFAULT_BRANCH, now, initial);
            case INVESTMENT -> InvestmentAccount.open(number, customerId, DEFAULT_BRANCH, now, initial);
            case CHEQUE -> ChequeAccount.open(number, customerId, DEFAULT_BRANCH, now, initial,
                    employerName, employerAddress);
        };

        return Database.inTransaction(conn -> {
            accountDAO.insert(conn, account);
            Transaction opening = account.getLastTransaction();
            if (opening != null) {
                accountDAO.insertTransaction(conn, number, opening);
            }
            return account;
        });
    }

    public Account deposit(String customerId, String accountNumber, String amountText) {
        BigDecimal amount = Money.parse(amountText);
        return Database.inTransaction(conn -> {
            Account account = findOwned(conn, customerId, accountNumber);
            BigDecimal before = account.getBalance();
            account.deposit(amount);
            accountDAO.updateAccount(conn, account, before);
            return account;
        });
    }

    public Account withdraw(String customerId, String accountNumber, String amountText) {
        BigDecimal amount = Money.parse(amountText);
        return Database.inTransaction(conn -> {
            Account account = findOwned(conn, customerId, accountNumber);
            if (!(account instanceof Withdrawable withdrawable)) {
                throw new BankingException("Savings accounts do not allow withdrawals.");
            }
            BigDecimal before = account.getBalance();
            if (!withdrawable.withdraw(amount)) {
                throw new BankingException("Insufficient funds for this withdrawal.");
            }
            accountDAO.updateAccount(conn, account, before);
            return account;
        });
    }

    public List<Transaction> history(String customerId, String accountNumber) {
        return Database.read(conn -> List.copyOf(findOwned(conn, customerId, accountNumber).getTransactions()));
    }

    /** Returns the account only if the customer holds it. Missing and other people's accounts look the same. */
    private Account findOwned(Connection conn, String customerId, String accountNumber) throws SQLException {
        if (isBlank(accountNumber)) {
            throw new BankingException("Choose an account first.");
        }
        Optional<Account> found = accountDAO.findAccount(conn, accountNumber.trim());
        if (found.isEmpty() || !found.get().getCustomerId().equals(customerId)) {
            throw new BankingException("Account not found.");
        }
        return found.get();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
