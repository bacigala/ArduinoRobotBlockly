import com.jme3.jfx.injfx.JmeToJfxIntegrator;
import com.jme3.system.AppSettings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import simulation.Simulation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class FXMLMainWindowController implements Initializable {

    @FXML javafx.scene.web.WebView WebViewEntity;
    @FXML javafx.scene.image.ImageView imageView;
    @FXML javafx.scene.layout.AnchorPane simulationAnchorPane;
    @FXML javafx.scene.control.SplitPane rightSplitPane;
    @FXML javafx.scene.control.TextArea codeTextArea;
    @FXML javafx.scene.control.TextArea consoleTextArea;
    @FXML javafx.scene.control.TextField consoleTextField;
    @FXML javafx.scene.control.Button consoleSendButton;

    private WebEngine webEngine = null;
    private Simulation simulation = null;
    private final SerialCommunicator serialCommunicator = new SerialCommunicator();
    private boolean lastConsoleWriteBySerial = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // start webengine - Blockly
        webEngine = WebViewEntity.getEngine();
        webEngine.load(getClass().getResource("/google-blockly/blockly_main_page.html").toString());

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
        codeTextArea.setText(getBlocklyCode());
    }

    private String getBlocklyCode() {
        return (String) webEngine.executeScript("Blockly.basicOttoGenerator.workspaceToCode(workspace)");
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
        String blocklyCode = getBlocklyCode();
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
        ArrayList<SerialCommunicator.ComPort> availablePorts = serialCommunicator.getAvailablePorts();
        connectionChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
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

}
