package banking.entity;

import banking.util.BankingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A bank customer. Accounts can only be attached to the customer who holds them (composition). */
public class Customer {

    private final String customerId;
    private final String firstName;
    private final String surname;
    private String address;
    private final String phone;
    private final String email;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String customerId, String firstName, String surname,
                    String address, String phone, String email) {
        this.customerId = requireText(customerId, "Customer ID");
        this.firstName = requireText(firstName, "First name");
        this.surname = requireText(surname, "Surname");
        this.address = requireText(address, "Address");
        this.phone = requireText(phone, "Phone number");
        this.email = requireText(email, "Email");
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String addr) {
        this.address = requireText(addr, "Address");
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void addAccount(Account account) {
        if (account == null) {
            throw new BankingException("Account is required.");
        }
        if (!customerId.equals(account.getCustomerId())) {
            throw new BankingException("That account belongs to a different customer.");
        }
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BankingException(field + " is required.");
        }
        return value.trim();
    }
}
