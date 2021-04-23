package FXMLDialogController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple text output dialog.
 * Displays text written into OutputStream it provides.
 */
public class TextOutputDialog {

    public static TextOutputDialog getDialog(String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextOutputDialog.class.getResource(
                "/fxml/FXMLTextOutputDialog.fxml"));
        Parent parent = fxmlLoader.load();
        TextOutputDialog controller = fxmlLoader.getController();
        controller.initialize(parent, title);
        return controller;
    }

    private @FXML javafx.scene.control.TextArea textArea;
    private Parent root;
    private String title;

    public void initialize(Parent root, String title) {
        this.root = root;
        this.title = title;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.show();
    }

    public OutputStream getOutputStream() {
        class MyOutputStream extends OutputStream {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                String value = Character.toString(c);
                buffer.append(value);
                if (value.equals("\n")) {
                    textArea.appendText(buffer.toString());
                    buffer.delete(0, buffer.length());
                }
            }
        }
        return new MyOutputStream();
    }

    public void closeButtonAction() {
        Stage stage = (Stage) textArea.getScene().getWindow();
        stage.close();
    }

}
