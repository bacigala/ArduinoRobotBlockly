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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class FXMLMainWindowController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeBlockly();
        initializeSimulation();
        initializeConsole();
        initializeRobotVersion();
    }

    // called after last application window has been closed
    public void applicationClose() {
        terminateSimulation();
        terminateSerialCommunication();
    }


    /**
     * Blockly.
     */
    @FXML javafx.scene.web.WebView blocklyWebView;

    private Blockly blockly = null;

    private void initializeBlockly() {
        blockly = new Blockly(blocklyWebView.getEngine());
    }

    private void reloadBlocklyCode() {
        codeTextArea.clear();
        codeTextArea.setText(blockly.getCode());
    }

    // called by GUI (on button pressed, od drag...)
    public void reloadBlockly() {
        reloadBlocklyCode();
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(this::reloadBlocklyCode);
        });
        thread.start();
    }


    /**
     * Console.
     */
    @FXML javafx.scene.control.TextArea consoleTextArea;
    @FXML javafx.scene.control.TextField consoleTextField;
    @FXML javafx.scene.control.Button consoleSendButton;
    @FXML javafx.scene.control.Button consoleClearButton;

    private boolean lastConsoleWriteBySerial = false;

    private void initializeConsole() {
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

        // pressing enter sends text to serial
        consoleTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) consoleSendButtonAction();
        });

        // gui defaults
        consoleSetDisable(true);
    }

    public void consoleSendButtonAction() {
        consoleSendButton.setDisable(true);
        codeSendToConsoleButton.setDisable(true);
        sendToConsole(consoleTextField.getText());
        consoleTextField.clear();
        consoleSendButton.setDisable(false);
        codeSendToConsoleButton.setDisable(false); // todo set default
    }

    public void consoleClearButtonAction() {
        consoleClearButton.setDisable(true);
        consoleTextArea.setText("");
        consoleClearButton.setDisable(false);
    }

    private void sendToConsole(String message) {
        try {
            serialCommunicator.send(message);
        } catch (IOException e) {
            consoleTextArea.appendText("\n[ERROR] Communication error!");
            consoleTextArea.appendText("\n[ERROR] Try serial restart.");
            consoleSetDisable(true);
            return;
        }
        if (lastConsoleWriteBySerial) {
            consoleTextArea.appendText("\n");
            lastConsoleWriteBySerial = false;
        }
        consoleTextArea.appendText("> " + message.replace("\n", "\n> ") + "\n");
    }

    private void consoleSetDisable(boolean value) {
        consoleTextArea.setDisable(value);
        consoleTextField.setDisable(value);
        consoleSendButton.setDisable(value);
        consoleClearButton.setDisable(value);
        codeSendToConsoleButton.setDisable(value); //section code
        consoleTextArea.setText(value ? "Serial not connected." : "");
    }


    /**
     * Generated code.
     */
    @FXML javafx.scene.control.TextArea codeTextArea;
    @FXML javafx.scene.control.Button codeSendToConsoleButton;
    @FXML javafx.scene.control.Button codeVerifyButton;
    @FXML javafx.scene.control.Button codeUploadButton;

    public void codeSendToConsoleButtonAction() {
        consoleSendButton.setDisable(true);
        codeSendToConsoleButton.setDisable(true);
        String blocklyCode = blockly.getCode();
        if (!blocklyCode.isEmpty()) sendToConsole(blocklyCode);
        consoleSendButton.setDisable(false);
        codeSendToConsoleButton.setDisable(false); // todo default
    }

    public void codeVerifyButtonAction() {
        try {
            assembleFileToCompile();
            ArduinoCompiler.verify(new File("generated-code.ino").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void codeUploadButtonAction() {
        try {
            assembleFileToCompile();
            ArduinoCompiler.verifyAndUpload(new File("generated-code.ino").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assembleFileToCompile() throws IOException {
        // get paths to all required files
        ArrayList<String> modulePaths = new ArrayList<>();
        for (int moduleNo : chosenModules) {
            System.out.println(moduleNo);
            modulePaths.add("/robot-versions/" + robotVersionControl.getModuleProperty(moduleNo, "location"));
        }

        // create output file
        FileWriter writer = new FileWriter("generated-code.ino");

        // output file header
        for (String file : modulePaths) {
            System.out.println("looking for resource: " + file + "_header.ino");
            URL resource = ArduinoCompiler.class.getResource(file + "_header.ino");
            System.out.println(resource.getPath());
            BufferedReader br = new BufferedReader(new FileReader(new File(resource.getFile())));
            String line;
            while ((line = br.readLine()) != null)
                writer.append(line).append("\r\n");
            br.close();
        }

        // output setup section
        writer.append("\r\nvoid setup() {\r\n");
        for (String file : modulePaths) {
            URL resource = ArduinoCompiler.class.getResource(file + "_setup.ino");
            BufferedReader br = new BufferedReader(new FileReader(new File(resource.getFile())));
            String line;
            while ((line = br.readLine()) != null)
                writer.append(line).append("\r\n");
            br.close();
        }
        writer.append("\r\n}\r\n\r\n");

        // get blockly code (the loop part and user-defined functions)
        writer.append(blockly.getCode()).append("\r\n\r\n");

        // output support functions from modules
        for (String file : modulePaths) {
            URL resource = ArduinoCompiler.class.getResource(file + "_footer.ino");
            BufferedReader br = new BufferedReader(new FileReader(new File(resource.getFile())));
            String line;
            while ((line = br.readLine()) != null)
                writer.append(line).append("\r\n");
            br.close();
        }

        writer.flush();
        writer.close();
    }


    /**
     * Serial connection.
     */
    @FXML javafx.scene.control.Button connectionConnectButton;
    @FXML javafx.scene.control.Button connectionSearchButton;
    @FXML javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> connectionChoiceBox;
    private final SerialCommunicator serialCommunicator = new SerialCommunicator();
    private boolean isConnected = false;

    private void terminateSerialCommunication() {
        serialCommunicator.disconnect();
    }

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
        consoleSetDisable(!isConnected);

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
     * Robot version.
     */
    @FXML javafx.scene.control.Button robotVersionLoadButton;
    @FXML javafx.scene.control.Button robotVersionSearchButton;
    @FXML javafx.scene.control.Button robotVersionModulesButton;
    @FXML javafx.scene.control.ChoiceBox<RobotVersionControl.RobotVersion> robotVersionChoiceBox;

    private final RobotVersionControl robotVersionControl = new RobotVersionControl();
    private final ArrayList<Integer> chosenModules = new ArrayList<>();

    private void initializeRobotVersion() {
        robotVersionLoadButton.setDisable(true);
        robotVersionModulesButton.setDisable(true);
    }

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
            robotVersionModulesButton.setDisable(false);
            blockly.setGenerator(robotVersionControl.getProperty("generator"));
            chosenModules.clear();
            robotVersionModulesButtonAction();
        } catch (Exception e) {
            System.out.println("Error while loading property file of selected version.");
            e.printStackTrace();
        }
    }


    /**
     *  Modules.
     */
    public void robotVersionModulesButtonAction() {
        try {
            DialogFactory.getInstance().openModuleSelectDialog(robotVersionControl, chosenModules);

            ArrayList<String> toolboxCategories = new ArrayList<>();
            for (Integer moduleNumber : chosenModules) {
//                int noToolboxCategories = Integer.parseInt(
//                        robotVersionControl.getModuleProperty(moduleNumber, "categoryCount"));
//                for (int categoryNumber = 1; categoryNumber <= noToolboxCategories; categoryNumber++) {
//                    toolboxCategories.add(robotVersionControl.getModuleCategoryName(moduleNumber, categoryNumber));
//                }
                ArrayList<String> moduleCategories = robotVersionControl.getModuleCategories(moduleNumber);
                if (moduleCategories != null) toolboxCategories.addAll(moduleCategories);
            }

            blockly.setToolbox(robotVersionControl.getProperty("toolbox"));
            blockly.hideCategories(robotVersionControl.getAllCategories());
            blockly.showCategories(toolboxCategories);
            blockly.setWorkspace(robotVersionControl.getProperty("workspace"));
            reloadBlocklyCode();
        } catch (Exception e) {
            System.err.println("Unable to load selected modules.");
            e.printStackTrace();
        }
    }


    /**
     * Simulation.
     */
    @FXML javafx.scene.layout.AnchorPane simulationAnchorPane;
    @FXML javafx.scene.image.ImageView imageView;

    private Simulation simulation = null;

    private void initializeSimulation() {
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
    }

    private void terminateSimulation() {
        if (simulation != null) simulation.stop();
    }

}
