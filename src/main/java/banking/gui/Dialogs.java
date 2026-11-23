package banking.gui;

import banking.util.BankingException;
import javafx.scene.control.Alert;

/** Shows messages. Business errors appear as dialogs instead of crashing the app. */
final class Dialogs {

    private Dialogs() {
    }

    static void run(Runnable action) {
        try {
            action.run();
        } catch (BankingException e) {
            show(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    static void info(String message) {
        show(Alert.AlertType.INFORMATION, message);
    }

    private static void show(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
