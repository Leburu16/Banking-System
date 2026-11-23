
import java.util.ArrayList;
import java.util.List;

public class Customer {

    private String customerID;
    private String firstName;
    private String surname;
    private String address;
    private String phone;

    private List<Account> accounts;

    public Customer(String customerID, String firstName,
                    String surname, String address, String phone) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.phone = phone;
        this.accounts = new ArrayList<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setAddress(String addr) {
        this.address = addr;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void addAccount(Account a) {
        if (a != null) {
            accounts.add(a);
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}

