
import java.math.BigDecimal;

public class ChequeAccount extends Account implements Withdrawable {

    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, BigDecimal balance,
                         String branch, java.time.LocalDateTime dateOpened,
                         String employerName, String employerAddress) {
        super(accountNumber, balance, branch, dateOpened);

        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    @Override
    public boolean withdraw(BigDecimal amt) {
        if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (balance.compareTo(amt) >= 0) {
            balance = balance.subtract(amt);

            recordTransaction(
                    TransactionType.WITHDRAWAL,
                    amt,
                    "Cheque account withdrawal"
            );

            return true;
        }

        return false;
    }

    public String getEmployerName() {
        return employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    @Override
    public String getAccountType() {
        return "Cheque Account";
    }
}

