package FXMLDialogController;

import application.ArduinoCompiler;
import application.FXMLMainWindowController;
import application.SerialCommunicator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Dialog for COM port choice and Arduino upload, used when uploading 'static' program to Arduino
 * (e.g. Otto Basic - compile & upload 'static' program - blockly-produced program is then being send via console)
 */
public class UploadToArduinoDialog {

    public static UploadToArduinoDialog getDialog(
            String message,
            ArrayList<SerialCommunicator.ComPort> availablePorts,
            SerialCommunicator.ComPort selectedComPort,
            URL resource) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(UploadToArduinoDialog.class.getResource(
                "/fxml/FXMLUploadToArduino.fxml"));
        Parent parent = fxmlLoader.load();
        UploadToArduinoDialog controller = fxmlLoader.getController();
        controller.initialize(parent, message, availablePorts, selectedComPort, resource);
        return controller;
    }

    private @FXML javafx.scene.control.Label mainLabel;
    private @FXML javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> serialChoiceBox;
    private @FXML javafx.scene.control.Button serialSearchButton;

    private Parent root;
    private String message;
    private ArrayList<SerialCommunicator.ComPort> availablePorts;
    private SerialCommunicator.ComPort selectedComPort;
    private final SerialCommunicator serialCommunicator= new SerialCommunicator();
    private URL resource;

    public void initialize(Parent root, String message, ArrayList<SerialCommunicator.ComPort> availablePorts,
                           SerialCommunicator.ComPort selectedComPort, URL resource) {
        this.root = root;
        this.message = message;
        this.availablePorts = availablePorts;
        this.selectedComPort = selectedComPort;
        this.resource = resource;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Upload to Arduino");
        stage.show();
        mainLabel.setText(message);
        if (availablePorts != null) {
            serialChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
            if (selectedComPort != null) serialChoiceBox.setValue(selectedComPort);
        }
    }

    public void serialSearchButtonAction() {
        serialSearchButton.setDisable(true);
        availablePorts = serialCommunicator.getAvailablePorts();
        serialChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
        serialSearchButton.setDisable(false);
    }

    public void skipButtonAction() {
        closeDialog();
    }

    public void uploadButtonAction() {
        SerialCommunicator.ComPort selectedPort = serialChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedPort == null) {
            FXMLMainWindowController.showDialog("Please choose port.");
            return;
        }

        String portName = selectedPort.getComName();
        ArduinoCompiler.verifyAndUpload(portName, resource.getPath());
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) mainLabel.getScene().getWindow();
        stage.close();
    }

}
