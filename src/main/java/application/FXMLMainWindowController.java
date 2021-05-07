package application;

import blockly.Blockly;
import com.jme3.jfx.injfx.JmeToJfxIntegrator;
import com.jme3.math.FastMath;
import com.jme3.system.AppSettings;
import FXMLDialogController.ExampleSelectDialog;
import FXMLDialogController.ModuleSelectDialog;
import FXMLDialogController.UploadToArduinoDialog;
import FXMLDialogController.TextAreaInputDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCode;
import netscape.javascript.JSException;
import simulation.Simulation;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Main window controller.
 */
public class FXMLMainWindowController implements Initializable {

    // called once on application startup
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeSerialConnection();
        initializeBlockly();
        initializeSimulation();
        initializeConsole();
        initializeRobotVersion();
    }

    // called once, after last application window has been closed
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
        blocklyWebView.setVisible(false);
    }

    private void reloadBlocklyCode() {
        codeTextArea.clear();
        codeTextArea.setText(blockly.getCode());
    }

    // called by GUI (on button pressed, od drag... in blocklyWebView)
    public void reloadBlockly() {
        reloadBlocklyCode();

        // Blockly code update fix.
        // Code being generated by blockly changes right after some GUI action.
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) { }
            Platform.runLater(this::reloadBlocklyCode);
        });
        thread.start();
    }



    /**
     * Serial connection.
     */

    @FXML javafx.scene.control.Button connectionConnectButton;
    @FXML javafx.scene.control.Button connectionSearchButton;
    @FXML javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> connectionChoiceBox;

    private final SerialCommunicator serialCommunicator = new SerialCommunicator();

    private void initializeSerialConnection() {
        connectionConnectButton.setDisable(true);

        // listen to choice made -> enable to connect and upload
        connectionChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (observableValue, oldIndex, newIndex) -> {
                    boolean portChosen = (newIndex.intValue() >= 0);
                    connectionConnectButton.setDisable(!portChosen);
                    boolean codeToConsole = Boolean.parseBoolean(
                            robotVersionControl.getProperty("codeToConsole", "false"));
                    codeVerifyButton.setDisable(codeToConsole);
                    codeUploadButton.setDisable(!portChosen || codeToConsole);
                });
    }

    private void terminateSerialCommunication() {
        serialCommunicator.disconnect();
    }

    public void connectionSearchButtonAction() {
        if (serialCommunicator.isConnected()) connectionConnectButtonAction(); // disconnect
        connectionSearchButton.setDisable(true);
        ArrayList<SerialCommunicator.ComPort> availablePorts = serialCommunicator.getAvailablePorts();
        connectionChoiceBox.setItems(FXCollections.observableArrayList(availablePorts));
        connectionSearchButton.setDisable(false);
    }

    public void connectionConnectButtonAction() {
        if (serialCommunicator.isConnected()) {
            serialCommunicator.disconnect();
        } else {
            SerialCommunicator.ComPort selectedComPort = connectionChoiceBox.getSelectionModel().getSelectedItem();
            if (selectedComPort == null) {
                showDialog("Please choose port first.");
                return;
            }
            try {
                serialCommunicator.connectPort(selectedComPort);
            } catch (IOException e) {
                String message = "Try refreshing ports and connecting again.";
                showDialog(Alert.AlertType.ERROR, e.getMessage(), message);
                return;
            }
        }

        boolean isConnected = serialCommunicator.isConnected();
        consoleSetDisable(!isConnected);

        // update GUI
        connectionSearchButton.setDisable(isConnected);
        connectionChoiceBox.setDisable(isConnected);
        connectionConnectButton.setText((isConnected ? "Disconnect" : "Connect") + "SERIAL");
        consoleTextArea.setText("");
        consoleTextArea.appendText(isConnected ? " * * * Connection established * * * \n" : "No connection.\n");
        consoleSendButton.setDisable(!isConnected);
        consoleTextField.setDisable(!isConnected);
        if (isConnected) Platform.runLater(() -> consoleTextField.requestFocus());

        boolean codeToConsole = Boolean.parseBoolean(
                robotVersionControl.getProperty("codeToConsole", "false"));
        codeSendToConsoleButton.setDisable(!codeToConsole || !isConnected);
        codeVerifyButton.setDisable(codeToConsole);

        boolean portSelected = connectionChoiceBox.getSelectionModel().getSelectedItem() != null;
        codeUploadButton.setDisable(codeToConsole || !portSelected);
    }


    /**
     * Console.
     */

    @FXML javafx.scene.control.TextArea consoleTextArea;
    @FXML javafx.scene.control.TextField consoleTextField;
    @FXML javafx.scene.control.Button consoleSendButton;
    @FXML javafx.scene.control.Button consoleClearButton;
    @FXML javafx.scene.control.CheckMenuItem consoleAutoSendCheckMenuItem;

    private boolean lastConsoleWriteBySerial = false;

    private void initializeConsole() {
        serialCommunicator.setOutputStream(new OutputStream() {
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

        // auto-send 
        consoleTextField.setOnKeyTyped(e -> {
            if (consoleAutoSendCheckMenuItem.isSelected()) consoleSendButtonAction();
        });

        // gui defaults
        consoleSetDisable(true);
    }

    public void consoleSendButtonAction() {
        consoleSendButton.setDisable(true);
        boolean codeSendToConsoleButtonDisable = codeSendToConsoleButton.isDisable();
        codeSendToConsoleButton.setDisable(true);
        sendToConsole(consoleTextField.getText());
        consoleTextField.clear();
        consoleSendButton.setDisable(false);
        codeSendToConsoleButton.setDisable(codeSendToConsoleButtonDisable);
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
        boolean codeSendToConsoleButtonDisable = codeSendToConsoleButton.isDisable();
        codeSendToConsoleButton.setDisable(true);
        String blocklyCode = blockly.getCode();
        if (!blocklyCode.isEmpty()) sendToConsole(blocklyCode);
        consoleSendButton.setDisable(false);
        codeSendToConsoleButton.setDisable(codeSendToConsoleButtonDisable);
    }

    public void codeVerifyButtonAction() {
        try {
            assembleFileToCompile();
            ArduinoCompiler.verify(new File("generated-code.ino").getAbsolutePath());
        } catch (Exception e) {
            showDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    public void codeUploadButtonAction() {
        String portName = connectionChoiceBox.getSelectionModel().getSelectedItem().getComName();
        if (portName == null) {
            showDialog("Please chose port.");
            return;
        }
        if (serialCommunicator.isConnected()) connectionConnectButtonAction(); // disconnect, make serial available
        try {
            assembleFileToCompile();
            ArduinoCompiler.verifyAndUpload(portName, new File("generated-code.ino").getAbsolutePath());
        } catch (Exception e) {
            showDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    private void assembleFileToCompile() throws IOException {
        // create output file
        FileWriter writer = new FileWriter("generated-code.ino");

        // assemble & append header part of modules
        appendModuleParts(writer, "header");

        // assemble & append setup part of modules
        writer.append("\r\nvoid setup() {\r\n");
        appendModuleParts(writer, "setup");
        writer.append("\r\n}\r\n\r\n");

        // append BLOCKLY CODE (main loop section and user-defined functions)
        writer.append(blockly.getCode());
        writer.append("\r\n\r\n");

        // assemble & append footer part of modules
        appendModuleParts(writer, "footer");

        writer.flush();
        writer.close();
    }

    private void appendModuleParts(FileWriter writer, String partName) throws IOException {
        Collections.sort(chosenModules);

        // get paths
        ArrayList<String> modulePartPaths = new ArrayList<>();
        for (int moduleNo : chosenModules) {
            String modulePartPath = robotVersionControl.getModuleProperty(moduleNo, partName);
            if (modulePartPath != null && !modulePartPath.isEmpty())
                modulePartPaths.add("/robot-versions/" + modulePartPath + ".ino");
        }

        // append to writer
        for (String file : modulePartPaths) {
            URL resource = ArduinoCompiler.class.getResource(file);
            if (resource == null) {
                throw new IOException("Error: module part \"" + file + "\" not found.");
            }
            BufferedReader br = new BufferedReader(new FileReader(new File(resource.getFile())));
            String line;
            while ((line = br.readLine()) != null)
                writer.append(line).append("\r\n");
            br.close();
        }
    }



    /**
     * Robot version.
     */

    @FXML javafx.scene.control.Button robotVersionLoadButton;
    @FXML javafx.scene.control.Button robotVersionSearchButton;
    @FXML javafx.scene.control.ChoiceBox<RobotVersionControl.RobotVersion> robotVersionChoiceBox;

    private final RobotVersionControl robotVersionControl = new RobotVersionControl();
    private final ArrayList<Integer> chosenModules = new ArrayList<>();

    private void initializeRobotVersion() {
        robotVersionLoadButton.setDisable(true);
    }

    public void robotVersionSearchButtonAction() {
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
            blockly.setGenerator(robotVersionControl.getProperty("generator"));
            chosenModules.clear();
            openModuleChoiceDialog();
            blocklyWebView.setVisible(true);

            boolean simulationEnabled = Boolean.parseBoolean(
                    robotVersionControl.getProperty("simulation", "false"));
            simulationStartStopButton.setDisable(!simulationEnabled);
            rightSplitPane.setDividerPositions(simulationEnabled ? 0.66 : 1);

            boolean codeToConsole = Boolean.parseBoolean(
                    robotVersionControl.getProperty("codeToConsole", "false"));
            codeSendToConsoleButton.setDisable(!codeToConsole || !serialCommunicator.isConnected());
            codeVerifyButton.setDisable(codeToConsole);

            boolean portSelected = connectionChoiceBox.getSelectionModel().getSelectedItem() != null;
            codeUploadButton.setDisable(codeToConsole || !portSelected);
        } catch (Exception e) {
            blocklyWebView.setVisible(false);
            consoleSetDisable(true);
            codeVerifyButton.setDisable(true);
            codeUploadButton.setDisable(true);
            simulationStartStopButton.setDisable(true);
            showDialog("Error while loading property file of selected version.");
        }
        consoleTextArea.clear();
        consoleTextField.clear();

        simulation.idle();
        simulationStartStopButton.setText("Load & Play");
        simulationSpeedSlider.setVisible(false);
        simulationCameraControlButton.setVisible(false);
        simulationState = SimulationState.STOP;
    }



    /**
     * Modules.
     */

    private void openModuleChoiceDialog() {
        try {
            ArrayList<String> toolboxCategories = new ArrayList<>();
            boolean programInRam = Boolean.parseBoolean(robotVersionControl.getProperty("programInRam", "false"));
            if (programInRam) {
                // offer control program upload
                ArrayList<SerialCommunicator.ComPort> availablePorts = serialCommunicator.getAvailablePorts();
                SingleSelectionModel<SerialCommunicator.ComPort> selectedComPort = connectionChoiceBox.getSelectionModel();
                String message = "This version requires you to upload program to robot.";
                String resourcePath = "/robot-versions/" + robotVersionControl.getProperty("programLocation");
                URL resource = ArduinoCompiler.class.getResource(resourcePath);
                UploadToArduinoDialog.getDialog(message, availablePorts, selectedComPort, resource).show();
                toolboxCategories.addAll(robotVersionControl.getAllCategories());
            } else {
                // open module selection
                ModuleSelectDialog.display(robotVersionControl, chosenModules);
                for (Integer moduleNumber : chosenModules) {
                    ArrayList<String> moduleCategories = robotVersionControl.getModuleCategories(moduleNumber);
                    if (moduleCategories != null) toolboxCategories.addAll(moduleCategories);
                }
            }

            // update Blockly GUI
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

    private void setToolboxCategories() {
        ArrayList<String> toolboxCategories = new ArrayList<>();
        for (Integer moduleNumber : chosenModules) {
            ArrayList<String> moduleCategories = robotVersionControl.getModuleCategories(moduleNumber);
            if (moduleCategories != null) toolboxCategories.addAll(moduleCategories);
        }
        blockly.hideCategories(robotVersionControl.getAllCategories());
        blockly.showCategories(toolboxCategories);
    }



    /**
     * Simulation.
     */

    @FXML javafx.scene.layout.AnchorPane simulationAnchorPane;
    @FXML javafx.scene.control.SplitPane rightSplitPane;
    @FXML javafx.scene.image.ImageView simulationImageView;
    @FXML javafx.scene.control.Button simulationStartStopButton;
    @FXML javafx.scene.control.Button simulationCameraControlButton;
    @FXML javafx.scene.control.Slider simulationSpeedSlider;

    private Simulation simulation = null;
    private enum SimulationState { INIT, STOP, PLAY }
    private SimulationState simulationState;

    private final AtomicInteger simulationSpeed = new AtomicInteger(3);

    // called on application startup
    private void initializeSimulation() {
        // state
        simulationState = SimulationState.STOP;
        simulationStartStopButton.setText("Initializing");
        simulationStartStopButton.setDisable(true);

        // simulation ImageView auto resize
        ChangeListener<Number> sizeChangeListener = (observableValue, number, t1) -> {
            simulationImageView.setFitWidth(simulationAnchorPane.getWidth());
            simulationImageView.setFitHeight(simulationAnchorPane.getHeight());
        };
        simulationAnchorPane.heightProperty().addListener(sizeChangeListener);
        simulationAnchorPane.widthProperty().addListener(sizeChangeListener);
        rightSplitPane.setDividerPositions(0.5);

        // camera control
        simulationCameraControlButton.setVisible(false);
        final float rotationStep = 5 * FastMath.DEG_TO_RAD;
        final float moveStep = 10;
        simulationCameraControlButton.setOnKeyPressed(e -> {
            switch (e.getCode().getChar().charAt(0)) {
                case 'A':
                    simulation.rotateCamera(Simulation.Axis.Y, rotationStep);
                    break;
                case 'D':
                    simulation.rotateCamera(Simulation.Axis.Y, -rotationStep);
                    break;
                case 'W':
                    simulation.rotateCamera(Simulation.Axis.X, -rotationStep);
                    break;
                case 'S':
                    simulation.rotateCamera(Simulation.Axis.X, rotationStep);
                    break;
                case 'U':
                    simulation.moveCamera(Simulation.Axis.Y, -moveStep);
                    break;
                case 'O':
                    simulation.moveCamera(Simulation.Axis.Y, moveStep);
                    break;
                case 'I':
                    simulation.moveCamera(Simulation.Axis.Z, moveStep);
                    break;
                case 'K':
                    simulation.moveCamera(Simulation.Axis.Z, -moveStep);
                    break;
                case 'L':
                    simulation.moveCamera(Simulation.Axis.X, moveStep);
                    break;
                case 'J':
                    simulation.moveCamera(Simulation.Axis.X, -moveStep);
                    break;
            }
        });

        // slider for simulation speed
        simulationSpeedSlider.setVisible(false);
        simulationSpeedSlider.setOnMouseDragged(
                e -> simulationSpeed.set((int) Math.round(simulationSpeedSlider.getValue())));
        simulationSpeedSlider.setOnKeyTyped(
                e -> simulationSpeed.set((int) Math.round(simulationSpeedSlider.getValue())));
        simulationSpeedSlider.setOnMouseClicked(
                e -> simulationSpeed.set((int) Math.round(simulationSpeedSlider.getValue())));

        // jMonkey instance, start and bind with imageView
        AppSettings settings = JmeToJfxIntegrator.prepareSettings(new AppSettings(true), 60);
        simulationSpeed.set(3);
        simulation = new Simulation(simulationSpeed);
        simulation.setSettings(settings);
        simulation.setShowSettings(false);
        JmeToJfxIntegrator.startAndBindMainViewPort(simulation, simulationImageView, Thread::new);
        Logger.getLogger("com.jme3").setLevel(SEVERE);

        // let jMonkey notify GUI when ready
        simulation.enqueue(() -> Platform.runLater(() -> {
            simulation.idle();
            simulationState = SimulationState.STOP;
            simulationStartStopButton.setText("Load & Play");
        }));
    }

    public void simulationStartStopButtonAction() {
        simulationStartStopButton.setDisable(true);
        switch (simulationState) {
            case STOP: // -> START SIMULATION
                // try to get blockly code
                String blocklyCode = "";
                try {
                    blocklyCode = blockly.getCode();
                } catch (JSException e) {
                    showDialog("Unable to load Blockly program.");
                }
                if (blocklyCode.isEmpty()) return;

                simulation.reset();
                simulation.loadOttoProgram(blocklyCode);

                // update GUI
                simulationStartStopButton.setText("Stop");
                simulationSpeedSlider.setValue(3);
                simulationSpeedSlider.setVisible(true);
                simulationCameraControlButton.setVisible(true);

                simulationState = SimulationState.PLAY;
                break;
            case PLAY: // -> STOP SIMULATION
                simulation.idle();

                // update GUI
                simulationStartStopButton.setText("Load & Play");
                simulationSpeedSlider.setVisible(false);
                simulationCameraControlButton.setVisible(false);

                simulationState = SimulationState.STOP;
                break;
        }
        simulationStartStopButton.setDisable(false);
    }

    // on application close
    private void terminateSimulation() {
        simulation.waitForMainLoopResume();
        if (simulation != null) simulation.stop(true);
    }



    /**
     * Parser for @ Otto program from user.
     * Available for 2 RobotVersions - ottoBasic and ottoProcedural
     */

    @FXML  javafx.scene.control.MenuItem loadAtProgramButton;

    public void parseProgramButtonAction() {
        String codeLoader = robotVersionControl.getProperty("codeLoader");
        if (codeLoader == null || (!codeLoader.equals("ottoProcedural") && !codeLoader.equals("ottoBasic"))) {
            showDialog("Cannot parse @ program for selected RobotVersion");
            return;
        }
        try {
            // get code from user
            String defaultChoreography = "@\n1000 3 60\n1000 3 120\n1000 3 90\n0 0 0\n";
            String inputCode = TextAreaInputDialog.display("Insert your @ choreography", defaultChoreography);

            // parse code
            inputCode = inputCode.replaceFirst("^@[ ]*", ""); // remove @
            ArrayList<String> codeLines = new ArrayList<>(Arrays.asList(inputCode.split("[\r\n]+")));
            if (codeLines.isEmpty()) throw new IllegalArgumentException("No code to parse.");

            StringBuilder workspace = new StringBuilder();
            workspace.append("<xml><block type='otto_basic_loop' deletable='false' movable='false'><statement name='PROGRAM'>"); // main block
            int relativeOrder = 0;
            int absoluteOrder = 0;
            int[] triplet = new int[3];

            for (String codeLine : codeLines) {
                if (codeLine.startsWith("#")) {
                    if (absoluteOrder > 0) workspace.append("<next>");
                    workspace.append("<block type='comment_block'><field name='COMMENT'>")
                             .append(codeLine.replaceFirst("^[\r\n ]*#", ""))
                             .append("</field>");
                    absoluteOrder++;
                    continue;
                }
                ArrayList<String> commands = new ArrayList<>(Arrays.asList(codeLine.split("[ ]+")));
                for (String command : commands) {
                    if (command.isEmpty()) continue;
                    triplet[relativeOrder] = Integer.parseInt(command);
                    if (relativeOrder == 0 && triplet[0] == 0) break; // end of code

                    if (relativeOrder == 2) { // end of triplet -> create block
                        if (absoluteOrder > 0) workspace.append("<next>");
                        if (codeLoader.equals("ottoProcedural"))
                            workspace.append(createOttoProceduralMoveBlock(triplet));
                        if (codeLoader.equals("ottoBasic"))
                            workspace.append(createOttoBasicMoveBlock(triplet));

                        absoluteOrder++;
                    }

                    relativeOrder++;
                    relativeOrder %= 3;
                }
            }

            for (int i = absoluteOrder; i > 1; i--)
                workspace.append("</block></next>");
            if (absoluteOrder > 0) workspace.append("</block>");
            workspace.append("</statement></block></xml>");

            // load code to blockly
            blockly.setWorkspace(workspace.toString());

        } catch (Exception e) {
            showDialog(Alert.AlertType.ERROR, "Parser error", "Cannot parse @ code :(");
            e.printStackTrace();
        }
    }

    private String createOttoBasicMoveBlock(int[] triplet) {
        String block = null;
        if (triplet[1] < 7) {
            block = "<block type='motor_move'>" +
                    "<field name='WAIT_TIME'>" + triplet[0] + "</field>" +
                    "<field name='MOTOR_NUMBER'>" + triplet[1] + "</field>" +
                    "<field name='MOTOR_POSITION'>" + triplet[2] + "</field>";
        } else switch (triplet[1]) {
            case 8:
                block = "<block type='block_dance_total_time'>" +
                        "<field name='DURATION'>" + triplet[2] + "</field>";
                break;
            case 9:
                block = "<block type='block_jump_to_line'>" +
                        "<field name='LINE_NO'>" + triplet[2] + "</field>";
                break;
            case 10:
                block = "<block type='block_set_slowdown'>" +
                        "<field name='SLOWDOWN'>" + triplet[2] + "</field>";
                break;
            case 11:
                block = "<block type='block_play_melody'>" +
                        "<field name='MELODY_NO'>" + triplet[2] + "</field>";
                break;
            case 13:
                block = "<block type='block_stop_melody'>";
                break;
            case 12:
                block = "<block type='block_play_sound_effect'>" +
                        "<field name='EFFECT_NO'>" + triplet[2] + "</field>";
                break;
        }
        return block;
    }

    private String createOttoProceduralMoveBlock(int[] triplet) {
        String block = null;
        if (triplet[1] < 7) {
            block = "<block type='otto_procedural_motor_move'>" +
                    "<field name='MOTOR_NUMBER'>" +
                    triplet[1] +
                    "</field>" +
                    "<value name='MOTOR_POSITION'>" +
                    "<shadow type='math_number'>" +
                    "<field name='NUM'>" + triplet[2] + "</field>" +
                    "</shadow>" +
                    "</value>";
        } else switch (triplet[1]) {
            case 8:
                    block = "<block type='comment_block'>" +
                        "<field name='COMMENT'>UNSUPPORTED BLOCK: Dance total time: " + triplet[2] + "</field>";
                break;
            case 9:
                block = "<block type='comment_block'>" +
                        "<field name='COMMENT'>UNSUPPORTED BLOCK: Jump to code line: " + triplet[2] + "</field>";
                break;
            case 10:
                block = "<block type='comment_block'>" +
                        "<field name='COMMENT'>UNSUPPORTED BLOCK: Set slowdown to: " + triplet[2] + "</field>";
                break;
            case 11:
                block = "<block type='melody_play'>" +
                        "<value name='MELODY_NO'><shadow type='math_number'><field name='NUM'>" +
                            triplet[2] + "</field></shadow></value>";
                break;
            case 13:
                block = "<block type='melody_stop'>";
                break;
            case 12:
                block = "<block type='play_sound_effect'>" +
                        "<value name='EFFECT_NO'><shadow type='math_number'>" +
                            "<field name='NUM'>" + triplet[2] + "</field></shadow></value>";
                break;
        }
        return block;
    }



    /**
     *  Load example program.
     */

    public void loadExampleButtonAction() {
        try {
            String workspace = ExampleSelectDialog.display(robotVersionControl, chosenModules);
            if (workspace == null || workspace.isEmpty()) return;
            setToolboxCategories();
            blockly.setWorkspace(workspace);
            reloadBlocklyCode();
        } catch (IOException e) {
            showDialog("Unable to open examples dialog.");
            e.printStackTrace();
        }
    }



    /**
     * Show simple alert dialog. (error, warning, info)
     */

    public static void showDialog() {
        showDialog(Alert.AlertType.ERROR, "ERROR", "Unexpected error occurred.");
    }

    public static void showDialog(String message) {
        showDialog(Alert.AlertType.WARNING, null, message);
    }

    public static void showDialog(Alert.AlertType alertType, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }



    /**
     * Test buttons.
     */

    public void testButton1Action() { // T1

    }

    public void testButton2Action() { // T2

    }

}