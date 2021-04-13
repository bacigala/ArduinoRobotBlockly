package dialog;

import application.RobotVersionControl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class ExampleSelectDialog {
    public static String display(RobotVersionControl robotVersionControl, ArrayList<Integer> chosenModules) throws IOException {
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
        return controller.getWorkspace();
    }

    @FXML javafx.scene.control.ListView<Example> exampleListView;
    @FXML javafx.scene.control.TextArea exampleInfoTextArea;

    private ArrayList<Integer> chosenModules;
    private RobotVersionControl robotVersionControl;
    private String workspace = "";

    public void initialize(RobotVersionControl robotVersionControl, ArrayList<Integer> chosenModules) {
        this.chosenModules = chosenModules;
        this.robotVersionControl = robotVersionControl;

        // get example information
        int exampleCount = Integer.parseInt(robotVersionControl.getProperty("exampleCount"));
        for (int exampleNo = 1; exampleNo <= exampleCount ; exampleNo++) {
            Example ex = new Example();
            ex.name = robotVersionControl.getExampleProperty(exampleNo, "name");
            ex.description = robotVersionControl.getExampleProperty(exampleNo, "description");
            ex.number = exampleNo;
            exampleListView.getItems().add(ex);
        }
    }

    /**
     * Closes the dialog.
     */
    public void cancelButtonAction() {
        Stage stage = (Stage) exampleListView.getScene().getWindow();
        stage.close();
    }

    public void confirmButtonAction() {
        Example selected = exampleListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.err.println("select an example!");
            return;
        }
        // add modules required by example
        chosenModules.clear();
        for (String moduleNo : robotVersionControl.getExampleModules(selected.number))
            chosenModules.add(Integer.parseInt(moduleNo));

        // set workspace
        workspace = robotVersionControl.getExampleProperty(selected.number, "workspace");

        cancelButtonAction();
    }

    public void exampleListViewClickAction() {
        viewExampleInfo(exampleListView.getSelectionModel().getSelectedItem());
    }

    private String getWorkspace() {
        return workspace;
    }

    private void viewExampleInfo(Example example) {
        if (example == null) {
            exampleInfoTextArea.setText("Select example to view details.");
            return;
        }
        exampleInfoTextArea.setText(example.name + "\n\n");
        exampleInfoTextArea.appendText(example.description);
    }

    private static class Example {
        public String name, description;
        int number;

        @Override
        public String toString() {
            return name;
        }
    }
}
