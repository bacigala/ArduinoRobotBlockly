package dialog;

import application.RobotVersionControl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.util.ArrayList;

public class FXMLModuleSelectDialogController {
    @FXML javafx.scene.control.Label mainLabel;
    @FXML javafx.scene.control.ListView<Module> moduleListView;
    @FXML javafx.scene.control.TextArea moduleInfoTextArea;
    @FXML javafx.scene.control.ProgressBar memoryProgressBar;

    private final ArrayList<Integer> selectedModuleIds = new ArrayList<>();
    private int memory = 0;

    public void initialize(RobotVersionControl robotVersionControl) {
        int moduleCount = Integer.parseInt(robotVersionControl.getProperty("moduleCount"));
        for (int moduleNo = 1; moduleNo <= moduleCount ; moduleNo++) {
            Module module = new Module();
            module.name = robotVersionControl.getModuleProperty(moduleNo, "name");
            module.description = robotVersionControl.getModuleProperty(moduleNo, "description");
            module.id = moduleNo;
            module.size = Integer.parseInt(robotVersionControl.getModuleProperty(moduleNo, "size"));
            module.required = Boolean.parseBoolean(robotVersionControl.getModuleProperty(moduleNo, "required"));
            moduleListView.getItems().add(module);
        }

        int maxMemory = Integer.parseInt(robotVersionControl.getProperty("robotMaxMemory"));
        moduleListView.setCellFactory(CheckBoxListCell.forListView(module -> {
            BooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, oldProperty, newProperty) -> {
                if (newProperty) {
                    // module was checked
                    selectedModuleIds.add(module.id);
                    memory += module.size;
                } else {
                    // module was unchecked
                    selectedModuleIds.remove(module.id);
                    memory -= module.size;
                }
                memoryProgressBar.setProgress(memory / (double)maxMemory);
            });
            return observable;
        }));
    }

    public void cancelButtonAction() {
        Stage stage = (Stage) mainLabel.getScene().getWindow();
        stage.close();
    }

    public void confirmButtonAction() {
        //todo: notify robotVersionControl AND / OR Blockly AND / OR ArduinoCompiler
    }

    public void moduleListViewClickAction() {
        Module selected = moduleListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            moduleInfoTextArea.setText("Select module to view details.");
            return;
        }
        moduleInfoTextArea.setText("Name:\t" + selected.name + "\n");
        moduleInfoTextArea.appendText("Size:\t\t" + selected.size + "\n");
        moduleInfoTextArea.appendText("Required:\t" + (selected.required ? "yes" : "no") + "\n\n");
        moduleInfoTextArea.appendText("Description:\n" + selected.description);
    }

    private static class Module {
        public String name, description;
        public int size;
        public boolean required;
        public Integer id;

        @Override
        public String toString() {
            return name + " (" + size + ")" + (required ? " R" : "");
        }
    }
}
