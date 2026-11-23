package banking.dao;

import banking.entity.Account;
import banking.entity.AccountType;
import banking.entity.ChequeAccount;
import banking.entity.InvestmentAccount;
import banking.entity.SavingsAccount;
import banking.entity.Transaction;
import banking.entity.TransactionType;
import banking.util.BankingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All JDBC code for accounts and their transactions. */
public final class AccountDAO {

    private static final String COLUMNS =
            "account_number, customer_id, account_type, branch, date_opened, balance, employer_name, employer_address";

    public void insert(Connection conn, Account a) throws SQLException {
        String sql = "INSERT INTO accounts (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String employerName = null;
        String employerAddress = null;
        if (a instanceof ChequeAccount cheque) {
            employerName = cheque.getEmployerName();
            employerAddress = cheque.getEmployerAddress();
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setString(2, a.getCustomerId());
            ps.setString(3, a.getType().name());
            ps.setString(4, a.getBranch());
            ps.setObject(5, a.getDateOpened());
            ps.setBigDecimal(6, a.getBalance());
            ps.setString(7, employerName);
            ps.setString(8, employerAddress);
            ps.executeUpdate();
        }
    }

    /**
     * updateAccount from the sequence diagram. Saves a new balance and its transaction.
     * NFR-6: the update only succeeds if the balance is still expectedBalance, so two requests
     * can never both apply to the same starting balance. Call inside Database.inTransaction.
     */
    public void updateAccount(Connection conn, Account a, BigDecimal expectedBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ? AND balance = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, a.getBalance());
            ps.setString(2, a.getAccountNumber());
            ps.setBigDecimal(3, expectedBalance);
            if (ps.executeUpdate() != 1) {
                throw new BankingException("This account changed while you were working on it. Please try again.");
            }
        }
        Transaction latest = a.getLastTransaction();
        if (latest != null) {
            insertTransaction(conn, a.getAccountNumber(), latest);
        }
    }

    public void insertTransaction(Connection conn, String accountNumber, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_id, account_number, txn_type, amount, txn_date, "
                + "balance_after, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, accountNumber);
            ps.setString(3, t.getType().name());
            ps.setBigDecimal(4, t.getAmount());
            ps.setObject(5, t.getDate());
            ps.setBigDecimal(6, t.getBalanceAfter());
            ps.setString(7, t.getDescription());
            ps.executeUpdate();
        }
    }

    /** findAccount from the sequence diagram. Returns the account with its full history. */
    public Optional<Account> findAccount(Connection conn, String accountNumber) throws SQLException {
        return query(conn, "SELECT " + COLUMNS + " FROM accounts WHERE account_number = ?", accountNumber)
                .stream().findFirst();
    }

    public List<Account> findAccountsByCustomer(Connection conn, String customerId) throws SQLException {
        return query(conn, "SELECT " + COLUMNS + " FROM accounts WHERE customer_id = ? ORDER BY date_opened",
                customerId);
    }

    public List<String> findAllAccountNumbers(Connection conn) throws SQLException {
        List<String> numbers = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT account_number FROM accounts ORDER BY account_number")) {
            while (rs.next()) {
                numbers.add(rs.getString(1));
            }
        }
        return numbers;
    }

    private List<Account> query(Connection conn, String sql, String param) throws SQLException {
        List<Account> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        for (Account account : result) {
            account.loadTransactions(loadTransactions(conn, account.getAccountNumber()));
        }
        return result;
    }

    private List<Transaction> loadTransactions(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT transaction_id, txn_type, amount, txn_date, balance_after, description "
                + "FROM transactions WHERE account_number = ? ORDER BY txn_seq";
        List<Transaction> history = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new Transaction(
                            rs.getString("transaction_id"),
                            TransactionType.valueOf(rs.getString("txn_type")),
                            rs.getBigDecimal("amount"),
                            rs.getObject("txn_date", LocalDateTime.class),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("description")));
                }
            }
        }
        return history;
    }

    /** NFR-11: adding an account type means adding one case here, not changing existing account classes. */
    private Account map(ResultSet rs) throws SQLException {
        String number = rs.getString("account_number");
        String customerId = rs.getString("customer_id");
        String branch = rs.getString("branch");
        LocalDateTime opened = rs.getObject("date_opened", LocalDateTime.class);
        BigDecimal balance = rs.getBigDecimal("balance");
        AccountType type = AccountType.valueOf(rs.getString("account_type"));
        return switch (type) {
            case SAVINGS -> new SavingsAccount(number, customerId, branch, opened, balance);
            case INVESTMENT -> new InvestmentAccount(number, customerId, branch, opened, balance);
            case CHEQUE -> new ChequeAccount(number, customerId, branch, opened, balance,
                    rs.getString("employer_name"), rs.getString("employer_address"));
        };
    }
}
