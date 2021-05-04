package FXMLDialogController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Simple text input dialog.
 * Display message and retrieve single line text from user (TextInput)
 */
public class TextInputDialog {
    public static String display(String message, String defaultText) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextInputDialog.class.getResource(
                "/fxml/FXMLTextInputDialog.fxml"));
        Parent root = fxmlLoader.load();
        TextInputDialog controller = fxmlLoader.getController();
        controller.initialize(message, defaultText);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Input");
        stage.showAndWait();
        return controller.result;
    }

    private @FXML javafx.scene.control.Label label;
    private @FXML javafx.scene.control.TextField textField;
    private String result = "";

    private void initialize(String message, String defaultText) {
        label.setText(message);
        textField.setText(defaultText);
    }

    public void buttonAction() {
        result = textField.getText();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }
}
