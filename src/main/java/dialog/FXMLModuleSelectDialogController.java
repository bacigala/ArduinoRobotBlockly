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
    @FXML javafx.scene.control.Label usedMemoryLabel;
    @FXML javafx.scene.control.ListView<Module> compulsoryModuleListView;
    @FXML javafx.scene.control.ListView<Module> availableModuleListView;
    @FXML javafx.scene.control.TextArea moduleInfoTextArea;
    @FXML javafx.scene.control.ProgressBar memoryProgressBar;

    private final ArrayList<Module> compulsoryModules = new ArrayList<>();
    private final ArrayList<Module> availableModules = new ArrayList<>();
    private int maxMemory = 0;
    private int usedMemory = 0;
    private ArrayList<Integer> chosenModules;

    /**
     * Sets default values, populates listviews with module info.
     * @param robotVersionControl provides information about modules of robot-version
     * @param chosenModules contains currently used modules, acts as return value
     */
    public void initialize(RobotVersionControl robotVersionControl, ArrayList<Integer> chosenModules) {
        this.chosenModules = chosenModules;
        mainLabel.setText(robotVersionControl.getProperty("name"));

        // get module information
        ArrayList<Module> allModules = new ArrayList<>();
        maxMemory = Integer.parseInt(robotVersionControl.getProperty("robotMaxMemory"));
        int moduleCount = Integer.parseInt(robotVersionControl.getProperty("moduleCount"));
        for (int moduleNo = 1; moduleNo <= moduleCount ; moduleNo++) {
            Module module = new Module();
            module.name = robotVersionControl.getModuleProperty(moduleNo, "name");
            module.description = robotVersionControl.getModuleProperty(moduleNo, "description");
            module.id = moduleNo;
            module.size = Integer.parseInt(robotVersionControl.getModuleProperty(moduleNo, "size"));
            module.required = Boolean.parseBoolean(robotVersionControl.getModuleProperty(moduleNo, "required"));
            if (module.required) {
                compulsoryModules.add(module);
                usedMemory += module.size;
            } else {
                availableModules.add(module);
            }
            allModules.add(module);
        }

        // set previously chosen as chosen
        for (Integer i : chosenModules) allModules.get(i-1).chosen.set(true);

        // listen to change of checkboxes in listview (selection of module)
        availableModuleListView.setCellFactory(CheckBoxListCell.forListView(module -> {
            BooleanProperty observable = new SimpleBooleanProperty();
            observable.addListener((obs, oldProperty, newProperty) -> {
                usedMemory += newProperty ? module.size : -module.size;
                viewUsedMemory();
            });
            observable.setValue(module.chosen.get());
            module.chosen = observable;
            return observable;
        }));

        // update gui
        compulsoryModuleListView.getItems().addAll(compulsoryModules);
        availableModuleListView.getItems().addAll(availableModules);
        viewUsedMemory();
    }

    /**
     * Closes the dialog without any change to selected modules.
     */
    public void cancelButtonAction() {
        Stage stage = (Stage) mainLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Updates arraylist of selected modules, then closes the dialog.
     */
    public void confirmButtonAction() {
        chosenModules.clear();
        for (Module module : compulsoryModules) chosenModules.add(module.id);
        for (Module module : availableModules) if (module.chosen.get()) chosenModules.add(module.id);
        cancelButtonAction();
    }

    public void compulsoryModuleListViewClickAction() {
        viewModuleInfo(compulsoryModuleListView.getSelectionModel().getSelectedItem());
    }

    public void availableModuleListViewClickAction() {
        viewModuleInfo(availableModuleListView.getSelectionModel().getSelectedItem());
    }

    private void viewModuleInfo(Module module) {
        if (module == null) {
            moduleInfoTextArea.setText("Select module to view details.");
            return;
        }
        moduleInfoTextArea.setText("Name:\t" + module.name + "\n");
        moduleInfoTextArea.appendText("Size:\t\t" + module.size + "\n");
        moduleInfoTextArea.appendText("Required:\t" + (module.required ? "yes" : "no") + "\n\n");
        moduleInfoTextArea.appendText("Description:\n" + module.description);
    }

    private void viewUsedMemory() {
        memoryProgressBar.setProgress(usedMemory / (double)maxMemory);
        usedMemoryLabel.setText(
                "Used memory: " + usedMemory + " / " + maxMemory + " (" + usedMemory*100 / maxMemory + "%)");
    }

    private static class Module {
        public String name, description;
        public int size;
        public boolean required;
        public BooleanProperty chosen = new SimpleBooleanProperty(false);
        public Integer id;

        @Override
        public String toString() {
            return name + " (" + size + ")";
        }
    }
}