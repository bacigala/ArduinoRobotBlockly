package application;

import blockly.Blockly;
import com.jme3.jfx.injfx.JmeToJfxIntegrator;
import com.jme3.system.AppSettings;
import dialog.DialogFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import simulation.Simulation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class FXMLMainWindowController implements Initializable {

    @FXML javafx.scene.web.WebView blocklyWebView;
    @FXML javafx.scene.image.ImageView imageView;
    @FXML javafx.scene.layout.AnchorPane simulationAnchorPane;
    @FXML javafx.scene.control.SplitPane rightSplitPane;
    @FXML javafx.scene.control.TextArea codeTextArea;
    @FXML javafx.scene.control.TextArea consoleTextArea;
    @FXML javafx.scene.control.TextField consoleTextField;
    @FXML javafx.scene.control.Button consoleSendButton;

    private Blockly blockly = null;
    private Simulation simulation = null;
    private final SerialCommunicator serialCommunicator = new SerialCommunicator();
    private boolean lastConsoleWriteBySerial = false;
    private final RobotVersionControl robotVersionControl = new RobotVersionControl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // start blockly
        blockly = new Blockly(blocklyWebView.getEngine());

        // start simulation - jMonkeyEngine
        var settings = JmeToJfxIntegrator.prepareSettings(new AppSettings(true), 60);
        simulation = new Simulation();
        simulation.setSettings(settings);
        simulation.setShowSettings(false);
        JmeToJfxIntegrator.startAndBindMainViewPort(simulation, imageView, Thread::new);
        Logger.getLogger("com.jme3").setLevel(SEVERE);

        // simulation ImageView auto resize
        ChangeListener<Number> sizeChangeListener = (observableValue, number, t1) -> {
            imageView.setFitWidth(simulationAnchorPane.getWidth());
            imageView.setFitHeight(simulationAnchorPane.getHeight());
        };
        simulationAnchorPane.heightProperty().addListener(sizeChangeListener);
        simulationAnchorPane.widthProperty().addListener(sizeChangeListener);

        // console setup
        serialCommunicator.setOutputStream( new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                String value = Character.toString((char) b);
                buffer.append(value);
                if (value.equals("\n")) {
                    consoleTextArea.appendText(buffer.toString());
                    buffer.delete(0, buffer.length());
                    lastConsoleWriteBySerial = true;
                }
            }
        });
        consoleTextArea.setText("No connection.");
        consoleTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) consoleSendButtonAction();
        });

        // gui elements default
        robotVersionLoadButton.setDisable(true);
        robotVersionModulesButton.setDisable(true);
    }

    // called after last application window has been closed
    public void applicationClose() {
        // stop simulation (jMonkeyEngine)
        if (simulation != null) simulation.stop();
        // stop serial communication
        serialCommunicator.disconnect();
    }

    public void dummyButtonAction() {
        codeTextArea.clear();
        codeTextArea.setText(blockly.getCode());
    }


    /**
     * Console - communication with Arduino.
     */
    public void consoleSendButtonAction() {
        try {
            serialCommunicator.send(consoleTextField.getText());
        } catch (IOException e) {
            // todo: alert - cannot send to COM
            consoleTextArea.appendText("\n* * * communication error * * *");
            return;
        }
        if (lastConsoleWriteBySerial) {
            consoleTextArea.appendText("\n");
            lastConsoleWriteBySerial = false;
        }
        consoleTextArea.appendText("> " + consoleTextField.getText() + "\n");
        consoleTextField.clear();
    }

    public void consoleClearButtonAction() {
        consoleTextArea.setText("");
    }


    /**
     * Generated code section
     */
    public void codeSendToConsoleButtonAction() {
        String blocklyCode = blockly.getCode();
        if (!blocklyCode.isEmpty() && isConnected) {
            try {
                serialCommunicator.send(blocklyCode);
                consoleTextArea.appendText(blocklyCode + "\n");
            } catch (IOException e) {
                // todo: alert - cannot send to COM
                consoleTextArea.appendText("\n* * * communication error * * *");
            }
        }
    }


    /**
     * Serial connection management toolbox.
     */
    @FXML javafx.scene.control.Button connectionConnectButton;
    @FXML javafx.scene.control.Button connectionSearchButton;
    @FXML javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> connectionChoiceBox;

    private boolean isConnected = false;

    public void connectionSearchButtonAction() {
        connectionSearchButton.setDisable(true);
        ArrayList<SerialCommunicator.ComPort> availablePorts = serialCommunicator.getAvailablePorts();
        connectionChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
        connectionSearchButton.setDisable(false);
    }

    public void connectionConnectButtonAction() {
        if (isConnected) {
            serialCommunicator.disconnect();
        } else {
            SerialCommunicator.ComPort selectedComPort = connectionChoiceBox.getSelectionModel().getSelectedItem();
            if (selectedComPort == null) {
                //todo: error - choose port
                System.err.println("No port chosen.");
                return;
            }
            if (!serialCommunicator.connectToPort(selectedComPort)) {
                //todo: error - connection not established
                System.err.println("Connection was not established.");
                return;
            }
        }
        isConnected = !isConnected;

        // update GUI
        connectionSearchButton.setDisable(isConnected);
        connectionChoiceBox.setDisable(isConnected);
        connectionConnectButton.setText(isConnected ? "Disconnect" : "Connect");
        consoleTextArea.setText("");
        consoleTextArea.appendText(isConnected ? " * * * Connection established * * * \n" : "No connection.\n");
        consoleSendButton.setDisable(!isConnected);
        consoleTextField.setDisable(!isConnected);
        if (isConnected) Platform.runLater(() -> consoleTextField.requestFocus());
    }

    /**
     * Robot version toolbox.
     */
    @FXML javafx.scene.control.Button robotVersionLoadButton;
    @FXML javafx.scene.control.Button robotVersionSearchButton;
    @FXML javafx.scene.control.Button robotVersionModulesButton;
    @FXML javafx.scene.control.ChoiceBox<RobotVersionControl.RobotVersion> robotVersionChoiceBox;

    public void robotVersionSearchButtonAction() {
        robotVersionModulesButton.setDisable(true);
        robotVersionLoadButton.setDisable(true);
        robotVersionSearchButton.setDisable(true);
        robotVersionControl.refreshAvailableVersionsList();
        ArrayList<RobotVersionControl.RobotVersion> availableVersions = robotVersionControl.getAvailableVersions();
        robotVersionChoiceBox.setItems(FXCollections.observableArrayList(availableVersions));
        robotVersionLoadButton.setDisable(false);
        robotVersionSearchButton.setDisable(false);
    }

    public void robotVersionLoadButtonAction() {
        try {
            robotVersionControl.loadVersion(robotVersionChoiceBox.getSelectionModel().getSelectedItem());
            String prop = robotVersionControl.getProperty("toolbox");
            System.out.println("prop read: " + prop);
            blockly.setToolbox(prop);
            blockly.setWorkspace(robotVersionControl.getProperty("workspace"));
            robotVersionModulesButton.setDisable(false);
        } catch (Exception e) {
            System.out.println("Error while loading property file of selected version.");
            e.printStackTrace();
        }
    }

    public void robotVersionModulesButtonAction() {
        if (!robotVersionControl.hasLoadedVersion()) {
            System.out.println("No robot-version loaded.");
            return;
        }

        ArrayList<Integer> chosenModules = new ArrayList<>(); // IDs of chosen modules
        try {
            DialogFactory.getInstance().openModuleSelectDialog(robotVersionControl, chosenModules);
        } catch (Exception e) {
            System.err.println("Unable to open module choice dialog.");
            e.printStackTrace();
        }
    }

}
