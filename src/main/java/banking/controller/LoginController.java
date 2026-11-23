package banking.controller;

import banking.dao.CustomerDAO;
import banking.dao.Database;
import banking.dao.UserDAO;
import banking.entity.Customer;
import banking.security.PasswordHasher;
import banking.util.BankingException;
import banking.util.IdGenerator;
import java.util.Optional;

/** Registration and login (FR: register, authenticate). Passwords are stored only as hashes. */
public final class LoginController {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final UserDAO userDAO = new UserDAO();

    public Customer login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new BankingException("Enter your username and password.");
        }
        String user = username.trim();
        return Database.read(conn -> {
            Optional<String> storedHash = userDAO.findPasswordHash(conn, user);
            if (storedHash.isEmpty() || !PasswordHasher.verify(password, storedHash.get())) {
                throw new BankingException("Invalid username or password.");
            }
            return customerDAO.findCustomer(conn, user)
                    .orElseThrow(() -> new BankingException("No customer record found for this login."));
        });
    }

    public Customer register(String firstName, String surname, String address, String phone,
                             String email, String username, String password) {
        String first = required(firstName, "First name");
        String last = required(surname, "Surname");
        String addr = required(address, "Address");
        String tel = required(phone, "Phone number");
        String mail = required(email, "Email");
        String user = required(username, "Username");

        if (!tel.matches("\\+?[0-9 ]{7,15}")) {
            throw new BankingException("Phone number must contain 7 to 15 digits.");
        }
        if (!mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BankingException("Enter a valid email address.");
        }
        if (!user.matches("[A-Za-z0-9_.]{3,40}")) {
            throw new BankingException("Username must be 3 to 40 letters, digits, dots or underscores.");
        }
        if (password == null || password.length() < 8) {
            throw new BankingException("Password must be at least 8 characters.");
        }

        Customer customer = new Customer(IdGenerator.customerId(), first, last, addr, tel, mail);
        String hash = PasswordHasher.hash(password);

        Database.inTransaction(conn -> {
            if (userDAO.findPasswordHash(conn, user).isPresent()) {
                throw new BankingException("That username is already taken.");
            }
            customerDAO.insert(conn, customer);
            userDAO.insert(conn, user, hash, customer.getCustomerId());
            return null;
        });
        return customer;
    }

    private static String required(String value, String label) {
        if (isBlank(value)) {
            throw new BankingException(label + " is required.");
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
