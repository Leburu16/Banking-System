package banking.controller;

import banking.dao.Database;
import banking.dao.UserDAO;
import banking.entity.AccountType;
import banking.entity.Customer;

/** On first run, creates a demo customer (john / password123) so the app can be tried straight away. */
public final class DemoData {

    private DemoData() {
    }

    public static void seedIfEmpty() {
        int users = Database.read(conn -> new UserDAO().count(conn));
        if (users > 0) {
            return;
        }
        LoginController login = new LoginController();
        AccountController accounts = new AccountController();
        Customer john = login.register("John", "Smith", "Plot 123, Gaborone", "71234567",
                "john@example.com", "john", "password123");
        accounts.openAccount(john.getCustomerId(), AccountType.SAVINGS, "1000.00", null, null);
        accounts.openAccount(john.getCustomerId(), AccountType.INVESTMENT, "500.00", null, null);
        accounts.openAccount(john.getCustomerId(), AccountType.CHEQUE, "2000.00", "ABC Company", "Gaborone");
    }
}
