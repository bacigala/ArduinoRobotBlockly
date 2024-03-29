package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {
    private FXMLMainWindowController mainWindowController = null;

    @Override
    public void start(Stage stage) throws Exception {
        // load and show main window
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/fxml/FXMLMainWindow.fxml"));
        Parent root = fxmlLoader.load();
        mainWindowController = fxmlLoader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Arduino Robot Blockly");
        stage.show();
    }

    @Override
    public void stop(){
        // notify main window controller (stop simulation, serial...)
        if (mainWindowController != null) mainWindowController.applicationClose();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
