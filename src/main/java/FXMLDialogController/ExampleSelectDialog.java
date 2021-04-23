package FXMLDialogController;

import application.FXMLMainWindowController;
import application.RobotVersionControl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Dialog for choice of example code provided by RobotVersion.
 * List examples with associated description.
 */
public class ExampleSelectDialog {
    /**
     * Load and display ExampleSelectDialog.
     * @param robotVersionControl RobotVersionControl to retrieve examples from
     * @param chosenModules ArrayList<Integer> to store modules required by the selected example
     * @return String - Blockly XML workspace associated with the chosen example
     * @throws IOException on FXML load issue
     */
    public static String display(RobotVersionControl robotVersionControl, ArrayList<Integer> chosenModules) throws IOException {
        // check if there are examples for selected RobotVersion
        int exampleCount = Integer.parseInt(robotVersionControl.getProperty("exampleCount", "0"));
        if (exampleCount == 0) {
            FXMLMainWindowController.showDialog("No examples found for this RobotVersion :(");
            return "";
        }

        // show examples dialog
        FXMLLoader fxmlLoader = new FXMLLoader(ExampleSelectDialog.class.getResource(
                "/fxml/FXMLExampleSelectDialog.fxml"));
        Parent root = fxmlLoader.load();
        ExampleSelectDialog controller = fxmlLoader.getController();
        controller.initialize(robotVersionControl, chosenModules);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Examples");
        stage.showAndWait();
        return controller.workspace;
    }

    @FXML javafx.scene.control.ListView<Example> exampleListView;
    @FXML javafx.scene.control.TextArea exampleInfoTextArea;

    private ArrayList<Integer> chosenModules;
    private RobotVersionControl robotVersionControl;
    private String workspace = "";

    private void initialize(RobotVersionControl robotVersionControl, ArrayList<Integer> chosenModules) {
        this.chosenModules = chosenModules;
        this.robotVersionControl = robotVersionControl;

        // get available examples info
        int exampleCount = Integer.parseInt(robotVersionControl.getProperty("exampleCount", "0"));
        for (int exampleNo = 1; exampleNo <= exampleCount ; exampleNo++) {
            Example ex = new Example();
            ex.name = robotVersionControl.getExampleProperty(exampleNo, "name");
            ex.description = robotVersionControl.getExampleProperty(exampleNo, "description");
            ex.number = exampleNo;
            exampleListView.getItems().add(ex);
        }

        exampleInfoTextArea.setText("Select example to view details.");
    }

    // close dialog, no changes applied
    public void cancelButtonAction() {
        Stage stage = (Stage) exampleListView.getScene().getWindow();
        stage.close();
    }

    // apply changes, close dialog
    public void confirmButtonAction() {
        // get selected example
        Example selected = exampleListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FXMLMainWindowController.showDialog("Please select an example!");
            return;
        }

        // add modules required by example
        chosenModules.clear();
        for (String moduleNo : robotVersionControl.getExampleModules(selected.number))
            chosenModules.add(Integer.parseInt(moduleNo));

        // set workspace
        workspace = robotVersionControl.getExampleProperty(selected.number, "workspace");

        // close dialog
        cancelButtonAction();
    }

    // example list clicked -> view example details
    public void exampleListViewClickAction() {
        Example example = exampleListView.getSelectionModel().getSelectedItem();
        if (example == null) {
            exampleInfoTextArea.setText("Select example to view details.");
            return;
        }
        exampleInfoTextArea.setText(example.name + "\n\n");
        exampleInfoTextArea.appendText(example.description);
    }

    // example instance for listView
    private static class Example {
        public String name, description;
        int number;

        @Override
        public String toString() {
            return name;
        }
    }

}
