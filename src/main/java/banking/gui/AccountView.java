package banking.gui;

import banking.controller.AccountController;
import banking.controller.InterestController;
import banking.entity.Account;
import banking.entity.AccountType;
import banking.entity.Customer;
import banking.entity.Transaction;
import banking.entity.Withdrawable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Boundary for a logged-in customer. Shows the customer's accounts, balances and history.
 * Deposit is on this screen, so a first-time user deposits in 2 screens (NFR-7).
 */
public class AccountView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final Customer customer;
    private final AccountController accounts;
    private final InterestController interest;

    private final ComboBox<Account> accountBox = new ComboBox<>();
    private final Label balanceLabel = new Label();
    private final TextField amountField = new TextField();
    private final Button withdrawButton = new Button("Withdraw");
    private final TextArea historyArea = new TextArea();

    private final ComboBox<AccountType> typeBox = new ComboBox<>();
    private final TextField openDepositField = new TextField();
    private final TextField employerNameField = new TextField();
    private final TextField employerAddressField = new TextField();

    private final ScrollPane root;

    public AccountView(Customer customer, AccountController accounts,
                       InterestController interest, Runnable onLogout) {
        this.customer = customer;
        this.accounts = accounts;
        this.interest = interest;

        Label welcome = new Label("Welcome, " + customer.getFirstName() + " " + customer.getSurname());
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button logout = new Button("Logout");
        logout.setOnAction(e -> onLogout.run());

        accountBox.setMaxWidth(Double.MAX_VALUE);
        accountBox.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account a) {
                return a == null ? "" : a.getAccountType() + " (" + a.getAccountNumber() + ")";
            }

            @Override
            public Account fromString(String text) {
                return null;
            }
        });
        accountBox.valueProperty().addListener((obs, oldValue, newValue) -> showSelected());

        amountField.setPromptText("Amount in BWP, e.g. 250.00");
        Button depositButton = new Button("Deposit");
        depositButton.setOnAction(e -> Dialogs.run(() -> {
            accounts.deposit(customer.getCustomerId(), selectedNumber(), amountField.getText());
            amountField.clear();
            Dialogs.info("Deposit successful.");
            reloadAccounts(selectedNumber());
        }));

        withdrawButton.setOnAction(e -> Dialogs.run(() -> {
            accounts.withdraw(customer.getCustomerId(), selectedNumber(), amountField.getText());
            amountField.clear();
            Dialogs.info("Withdrawal successful.");
            reloadAccounts(selectedNumber());
        }));

        historyArea.setEditable(false);
        historyArea.setPrefRowCount(8);

        typeBox.getItems().setAll(AccountType.values());
        typeBox.setValue(AccountType.SAVINGS);
        typeBox.setConverter(new StringConverter<AccountType>() {
            @Override
            public String toString(AccountType t) {
                return t == null ? "" : t.getDisplayName();
            }

            @Override
            public AccountType fromString(String text) {
                return null;
            }
        });
        openDepositField.setPromptText("Initial deposit in BWP (Investment minimum 500.00)");
        employerNameField.setPromptText("Employer name (Cheque accounts only)");
        employerAddressField.setPromptText("Employer address (Cheque accounts only)");
        Button openButton = new Button("Open account");
        openButton.setOnAction(e -> Dialogs.run(() -> {
            accounts.openAccount(customer.getCustomerId(), typeBox.getValue(), openDepositField.getText(),
                    employerNameField.getText(), employerAddressField.getText());
            openDepositField.clear();
            Dialogs.info("Account opened.");
            reloadAccounts(null);
        }));

        Button interestButton = new Button("Run monthly interest job (scheduler)");
        interestButton.setOnAction(e -> Dialogs.run(() -> {
            InterestController.RunSummary summary = interest.runMonthlyInterest();
            String message = "Interest paid on " + summary.accountsPaid() + " account(s).";
            if (!summary.failures().isEmpty()) {
                message += "\nNot paid (will retry next run):\n" + String.join("\n", summary.failures());
            }
            Dialogs.info(message);
            reloadAccounts(selectedNumber());
        }));

        VBox content = new VBox(10,
                new HBox(12, welcome, logout),
                new Label("Your accounts"), accountBox,
                balanceLabel,
                new Label("Deposit or withdraw"), amountField,
                new HBox(10, depositButton, withdrawButton),
                new Label("Transaction history"), historyArea,
                new Separator(),
                new Label("Open a new account"), typeBox, openDepositField,
                employerNameField, employerAddressField, openButton,
                new Separator(),
                interestButton);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f1f5f9;");

        root = new ScrollPane(content);
        root.setFitToWidth(true);

        reloadAccounts(null);
    }

    public Parent getRoot() {
        return root;
    }

    private String selectedNumber() {
        Account selected = accountBox.getValue();
        return selected == null ? null : selected.getAccountNumber();
    }

    /** Reloads from the controller. Keeps the given account selected, or the newest one if none is given. */
    private void reloadAccounts(String keepNumber) {
        List<Account> list = accounts.listAccounts(customer.getCustomerId());
        accountBox.getItems().setAll(list);
        Account toSelect = null;
        for (Account a : list) {
            if (a.getAccountNumber().equals(keepNumber)) {
                toSelect = a;
            }
        }
        if (toSelect == null && !list.isEmpty()) {
            toSelect = list.get(list.size() - 1);
        }
        accountBox.setValue(toSelect);
        showSelected();
    }

    private void showSelected() {
        Account selected = accountBox.getValue();
        if (selected == null) {
            balanceLabel.setText("You have no accounts yet. Open one below.");
            historyArea.clear();
            withdrawButton.setDisable(true);
            return;
        }
        balanceLabel.setText("Balance: BWP " + selected.getBalance().toPlainString());
        withdrawButton.setDisable(!(selected instanceof Withdrawable));

        StringBuilder text = new StringBuilder();
        for (Transaction t : accounts.history(customer.getCustomerId(), selected.getAccountNumber())) {
            text.append(t.getDate().format(DATE_FORMAT)).append("   ")
                    .append(t.getType()).append("   ")
                    .append(t.getAmount().toPlainString()).append("   balance ")
                    .append(t.getBalanceAfter().toPlainString()).append("   ")
                    .append(t.getDescription()).append('\n');
        }
        historyArea.setText(text.toString());
    }
}
