package dialog;

import application.ArduinoCompiler;
import application.SerialCommunicator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

public class FXMLUploadToArduinoDialog {

    public static FXMLUploadToArduinoDialog getDialog(
            String message, ArrayList<SerialCommunicator.ComPort> availablePorts,
            SingleSelectionModel<SerialCommunicator.ComPort> selectedComPort, URL resource) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FXMLUploadToArduinoDialog.class.getResource(
                "/fxml/FXMLUploadToArduino.fxml"));
        Parent parent = fxmlLoader.load();
        FXMLUploadToArduinoDialog controller = fxmlLoader.getController();
        controller.initialize(parent, message, availablePorts, selectedComPort, resource);
        return controller;
    }

    private @FXML javafx.scene.control.Label mainLabel;
    private @FXML javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> serialChoiceBox;
    private @FXML javafx.scene.control.Button serialSearchButton;
    private @FXML javafx.scene.control.Button skipButton;
    private @FXML javafx.scene.control.Button uploadButton;

    private Parent root;
    private String message;
    private ArrayList<SerialCommunicator.ComPort> availablePorts;
    private SingleSelectionModel<SerialCommunicator.ComPort> selectedComPort;
    private final SerialCommunicator serialCommunicator= new SerialCommunicator();
    private URL resource;

    public void initialize(Parent root, String message, ArrayList<SerialCommunicator.ComPort> availablePorts,
                           SingleSelectionModel<SerialCommunicator.ComPort> selectedComPort, URL resource) {
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
        serialChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
        if (selectedComPort != null) serialChoiceBox.setSelectionModel(selectedComPort);
    }

    public void serialSearchButtonAction() {
        serialSearchButton.setDisable(true);
        availablePorts = serialCommunicator.getAvailablePorts();
        serialChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
        serialSearchButton.setDisable(false);
    }

    public void skipButtonAction() {
        closeThisDialog();
    }

    public void uploadButtonAction() {
        SerialCommunicator.ComPort selectedPort = serialChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedPort == null) {
            System.err.println("No port chosen.");
            return;
        }

        String portName = selectedPort.getComName();
        ArduinoCompiler.verifyAndUpload(portName, resource.getPath());
    }

    private void closeThisDialog() {
        Stage stage = (Stage) mainLabel.getScene().getWindow();
        stage.close();
    }
}
