package banking.entity;

import banking.util.BankingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cheque: withdrawals allowed, no interest. Only for working customers, so employer details are required. */
public class ChequeAccount extends Account implements Withdrawable {

    private final String employerName;
    private final String employerAddress;

    public ChequeAccount(String accountNumber, String customerId, String branch, LocalDateTime dateOpened,
                         BigDecimal balance, String employerName, String employerAddress) {
        super(accountNumber, customerId, branch, dateOpened, balance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    public static ChequeAccount open(String accountNumber, String customerId, String branch, LocalDateTime now,
                                     BigDecimal initialDeposit, String employerName, String employerAddress) {
        if (isBlank(employerName) || isBlank(employerAddress)) {
            throw new BankingException("Cheque accounts are for working customers. "
                    + "Please enter your employer's name and address.");
        }
        ChequeAccount account = new ChequeAccount(accountNumber, customerId, branch, now, BigDecimal.ZERO,
                employerName.trim(), employerAddress.trim());
        if (initialDeposit != null && initialDeposit.signum() > 0) {
            account.deposit(initialDeposit, "Opening deposit");
        }
        return account;
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    @Override
    public AccountType getType() {
        return AccountType.CHEQUE;
    }

    @Override
    public boolean withdraw(BigDecimal amt) {
        return tryDebit(amt, "Cheque withdrawal");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
