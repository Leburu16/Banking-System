package banking.gui;

import banking.controller.LoginController;
import banking.entity.Customer;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Boundary: collects login and registration input and hands it to LoginController. */
public class LoginView {

    private final VBox root = new VBox(10);

    public LoginView(LoginController controller, Consumer<Customer> onLoggedIn) {
        Label title = new Label("BANKING SYSTEM");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> Dialogs.run(() ->
                onLoggedIn.accept(controller.login(username.getText(), password.getText()))));

        TextField firstName = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextField newUsername = new TextField();
        PasswordField newPassword = new PasswordField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("First name"), firstName);
        form.addRow(1, new Label("Surname"), surname);
        form.addRow(2, new Label("Address"), address);
        form.addRow(3, new Label("Phone"), phone);
        form.addRow(4, new Label("Email"), email);
        form.addRow(5, new Label("Username"), newUsername);
        form.addRow(6, new Label("Password (8+)"), newPassword);

        Button createButton = new Button("Create account");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(e -> Dialogs.run(() -> {
            controller.register(firstName.getText(), surname.getText(), address.getText(),
                    phone.getText(), email.getText(), newUsername.getText(), newPassword.getText());
            onLoggedIn.accept(controller.login(newUsername.getText(), newPassword.getText()));
        }));

        VBox registerBox = new VBox(10, form, createButton);
        registerBox.setVisible(false);
        registerBox.setManaged(false);

        Hyperlink toggle = new Hyperlink("New customer? Create an account");
        toggle.setOnAction(e -> {
            boolean show = !registerBox.isVisible();
            registerBox.setVisible(show);
            registerBox.setManaged(show);
        });

        root.setPadding(new Insets(24));
        root.getChildren().addAll(title,
                new Label("Username"), username,
                new Label("Password"), password,
                loginButton, toggle, registerBox);
    }

    public Parent getRoot() {
        return root;
    }
}
