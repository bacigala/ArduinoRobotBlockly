package dialog;

import application.RobotVersionControl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;



public class DialogFactory {

    private static final DialogFactory instance = new DialogFactory();
    private DialogFactory() {};

    public static DialogFactory getInstance() {
        return instance;
    }

    /**
     * Opens dialog with choice of RobotVersion modules.
     */
    public void openModuleSelectDialog(RobotVersionControl robotVersionControl) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DialogFactory.class.getResource(
                "/fxml/FXMLModuleSelectDialog.fxml"));
        Parent root = fxmlLoader.load();
        FXMLModuleSelectDialogController controller = fxmlLoader.getController();
        controller.initialize(robotVersionControl);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Modules");
        stage.showAndWait();
    }
}
