
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        Customer customer = new Customer(
                "C001",
                "John",
                "Smith",
                "Gaborone",
                "71234567"
        );

        SavingsAccount savings = new SavingsAccount(
                "SA001",
                new BigDecimal("1000.00"),
                "Gaborone Branch",
                LocalDateTime.now()
        );

        InvestmentAccount investment = new InvestmentAccount(
                "IA001",
                new BigDecimal("500.00"),
                "Gaborone Branch",
                LocalDateTime.now()
        );

        ChequeAccount cheque = new ChequeAccount(
                "CA001",
                new BigDecimal("2000.00"),
                "Gaborone Branch",
                LocalDateTime.now(),
                "ABC Company",
                "Gaborone"
        );

        // Add accounts to customer
        customer.addAccount(savings);
        customer.addAccount(investment);
        customer.addAccount(cheque);

        // Deposit into savings account
        savings.deposit(new BigDecimal("200.00"));

        // Calculate and pay savings interest
        savings.payInterest();

        // Withdraw from investment account
        investment.withdraw(new BigDecimal("100.00"));

        // Withdraw from cheque account
        cheque.withdraw(new BigDecimal("300.00"));

        // Display customer details
        System.out.println("Customer: "
                + customer.getFirstName() + " "
                + customer.getSurname());

        // Display all accounts
        for (Account account : customer.getAccounts()) {
            System.out.println("\nAccount Type: "
                    + account.getAccountType());

            System.out.println("Account Number: "
                    + account.getAccountNumber());

            System.out.println("Balance: "
                    + account.getBalance());

            System.out.println("Transactions: "
                    + account.getTransactions().size());
        }
    }
}
