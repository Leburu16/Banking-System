package banking.gui;

import banking.controller.AccountController;
import banking.controller.DemoData;
import banking.controller.InterestController;
import banking.controller.LoginController;
import banking.dao.Database;
import banking.entity.Customer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point. Creates the controllers and switches between screens.
 * The views (boundary layer) hold no business logic; they call the controllers.
 */
public class BankingApp extends Application {

    private final LoginController loginController = new LoginController();
    private final AccountController accountController = new AccountController();
    private final InterestController interestController = new InterestController();
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        Database.initSchema();
        DemoData.seedIfEmpty();
        stage.setTitle("Banking System");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView view = new LoginView(loginController, this::showDashboard);
        stage.setScene(new Scene(view.getRoot(), 480, 640));
    }

    private void showDashboard(Customer customer) {
        AccountView view = new AccountView(customer, accountController, interestController, this::showLogin);
        stage.setScene(new Scene(view.getRoot(), 820, 680));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
