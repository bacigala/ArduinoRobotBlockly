package dialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TextAreaInputDialog {
    public static String display(String message, String defaultText) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextAreaInputDialog.class.getResource(
                "/fxml/FXMLTextAreaInputDialog.fxml"));
        Parent root = fxmlLoader.load();
        TextAreaInputDialog controller = fxmlLoader.getController();
        controller.initialize(message, defaultText);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Input");
        stage.showAndWait();
        return controller.result;
    }

    private @FXML javafx.scene.control.Label label;
    private @FXML javafx.scene.control.TextArea textArea;
    private String result;

    private void initialize(String message, String defaultText) {
        result = defaultText;
        label.setText(message);
        textArea.setText(defaultText);
    }

    public void buttonAction() {
        result = textArea.getText();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }
}
