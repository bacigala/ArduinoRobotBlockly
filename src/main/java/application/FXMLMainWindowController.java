package application;

import blockly.Blockly;
import com.jme3.jfx.injfx.JmeToJfxIntegrator;
import com.jme3.system.AppSettings;
import dialog.ExampleSelectDialog;
import dialog.FXMLModuleSelectDialogController;
import dialog.FXMLUploadToArduinoDialog;
import dialog.TextAreaInputDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCode;
import netscape.javascript.JSException;
import simulation.Simulation;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
    @FXML
    javafx.scene.web.WebView blocklyWebView;

    private Blockly blockly = null;

    private void initializeBlockly() {
        blockly = new Blockly(blocklyWebView.getEngine());
        blocklyWebView.setVisible(false);
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
    @FXML
    javafx.scene.control.TextArea consoleTextArea;
    @FXML
    javafx.scene.control.TextField consoleTextField;
    @FXML
    javafx.scene.control.Button consoleSendButton;
    @FXML
    javafx.scene.control.Button consoleClearButton;

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
    @FXML
    javafx.scene.control.TextArea codeTextArea;
    @FXML
    javafx.scene.control.Button codeSendToConsoleButton;
    @FXML
    javafx.scene.control.Button codeVerifyButton;
    @FXML
    javafx.scene.control.Button codeUploadButton;

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
            String portName = connectionChoiceBox.getSelectionModel().getSelectedItem().getComName(); //todo: NULL -> error no pot chosen
            assembleFileToCompile();
            ArduinoCompiler.verifyAndUpload(portName, new File("generated-code.ino").getAbsolutePath());
        } catch (Exception e) {
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
    @FXML
    javafx.scene.control.Button connectionConnectButton;
    @FXML
    javafx.scene.control.Button connectionSearchButton;
    @FXML
    javafx.scene.control.ChoiceBox<SerialCommunicator.ComPort> connectionChoiceBox;
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
    @FXML
    javafx.scene.control.Button robotVersionLoadButton;
    @FXML
    javafx.scene.control.Button robotVersionSearchButton;
    @FXML
    javafx.scene.control.ChoiceBox<RobotVersionControl.RobotVersion> robotVersionChoiceBox;

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
            robotVersionModulesButtonAction();
            simulationButton.setDisable(!Boolean.parseBoolean(
                    robotVersionControl.getProperty("simulation", "false")));
            blocklyWebView.setVisible(true);
        } catch (Exception e) {
            blocklyWebView.setVisible(false);
            System.err.println("Error while loading property file of selected version. OR no version chosen");
            System.err.println("EXCEPTION INFO: " + e.getMessage());
        }
    }


    /**
     * Modules.
     */
    private void robotVersionModulesButtonAction() {

        try {
            ArrayList<String> toolboxCategories = new ArrayList<>();

            boolean programInRam = Boolean.parseBoolean(robotVersionControl.getProperty("programInRam"));
            if (programInRam) {
                ArrayList<SerialCommunicator.ComPort> availablePorts = serialCommunicator.getAvailablePorts();
                SingleSelectionModel<SerialCommunicator.ComPort> selectedComPort = connectionChoiceBox.getSelectionModel();
                String message = "This version requires you to upload program to robot.";

                String resourcePath = "/robot-versions/" + robotVersionControl.getProperty("programLocation");
                URL resource = ArduinoCompiler.class.getResource(resourcePath);
                FXMLUploadToArduinoDialog.getDialog(message, availablePorts, selectedComPort, resource).show();
                toolboxCategories.addAll(robotVersionControl.getAllCategories());
            } else {
                //open module selection
                FXMLModuleSelectDialogController.display(robotVersionControl, chosenModules);
                for (Integer moduleNumber : chosenModules) {
                    ArrayList<String> moduleCategories = robotVersionControl.getModuleCategories(moduleNumber);
                    if (moduleCategories != null) toolboxCategories.addAll(moduleCategories);
                }
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

    private void setToolboxCategories() {
        ArrayList<String> toolboxCategories = new ArrayList<>();
        for (Integer moduleNumber : chosenModules) {
            ArrayList<String> moduleCategories = robotVersionControl.getModuleCategories(moduleNumber);
            if (moduleCategories != null) toolboxCategories.addAll(moduleCategories);
        }
        blockly.hideCategories(robotVersionControl.getAllCategories());
        blockly.showCategories(toolboxCategories);
    }


    public void testButton1Action() {
        System.out.println(blockly.getWorkspace());
        System.out.println(blockly.getWorkspace().replaceAll("\"", "'"));
        //terminateSimulation();
    }

    public void testButton2Action() {
        terminateSimulation();
    }


    /**
     * Simulation.
     */
    @FXML javafx.scene.layout.AnchorPane simulationAnchorPane;
    @FXML javafx.scene.control.SplitPane rightSplitPane;
    @FXML javafx.scene.image.ImageView imageView;
    @FXML javafx.scene.control.Button simulationButton;
    @FXML javafx.scene.control.Button simulationStopButton;

    private Simulation simulation = null;
    private enum SimulationState { STOP, READY, RUN }
    private SimulationState simulationState;

    // called on application startup
    private void initializeSimulation() {
        // control
        simulationState = SimulationState.STOP;
        simulationButton.setText("Reset");
        simulationButton.setDisable(true);

        // simulation ImageView auto resize
        ChangeListener<Number> sizeChangeListener = (observableValue, number, t1) -> {
            imageView.setFitWidth(simulationAnchorPane.getWidth());
            imageView.setFitHeight(simulationAnchorPane.getHeight());
        };
        simulationAnchorPane.heightProperty().addListener(sizeChangeListener);
        simulationAnchorPane.widthProperty().addListener(sizeChangeListener);
        rightSplitPane.setDividerPositions(0.5);

        Logger.getLogger("com.jme3").setLevel(SEVERE);
    }

    private void prepareSimulation() {
        simulationButton.setDisable(true);
        AppSettings settings = JmeToJfxIntegrator.prepareSettings(new AppSettings(true), 60);
        simulation = new Simulation();
        simulation.setSettings(settings);
        simulation.setShowSettings(false);
        JmeToJfxIntegrator.startAndBindMainViewPort(simulation, imageView, Thread::new);
        Logger.getLogger("com.jme3").setLevel(SEVERE);

        simulation.enqueue(() -> Platform.runLater(() -> {
            simulationState = SimulationState.READY;
            simulationButton.setText("Run");
            simulationButton.setDisable(false);
        }));
    }

    private void loadAndRunSimulation() {
        String blocklyCode = "";
        try {
            blocklyCode = blockly.getCode();
        } catch (JSException e) {
            System.err.println("Unable to load Blockly program.");
        }
        if (blocklyCode.isEmpty()) return;
        simulation.loadOttoProgram(blocklyCode);

        simulationState = SimulationState.RUN;
        simulationButton.setText("Stop");

    }

    private void terminateSimulation() {
        if (simulation != null) simulation.stop();
        simulation = null;

        simulationState = SimulationState.STOP;
        simulationButton.setText("Reset");
    }

    public void simulationButtonAction() {
        switch (simulationState) {
            case STOP:
                prepareSimulation();
                break;
            case READY:
                loadAndRunSimulation();
                break;
            case RUN:
                terminateSimulation();
                break;
        }
    }

    /**
     * Load @ program.
     */
    @FXML
    javafx.scene.control.MenuItem loadAtProgramButton;

    public void loadAtProgramButtonAction() {
        String codeLoader = robotVersionControl.getProperty("codeLoader");
        if (codeLoader == null) {
            // todo: unsupported operation dialog / disable this option
            System.err.println("Unsupported operation");
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
            workspace.append("<xml><block type='otto_basic_loop' deletable='false' movable='false'><statement name='PROGRAM'>");
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
            System.err.println("Cannot parse @ code" + e.getMessage()); //todo error dialog
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
    @FXML javafx.scene.control.MenuItem loadExampleButton;

    public void loadExampleButtonAction() {
        try {
            String workspace = ExampleSelectDialog.display(robotVersionControl, chosenModules);
            if (workspace == null || workspace.isEmpty()) return;
            setToolboxCategories();
            blockly.setWorkspace(workspace);
            reloadBlocklyCode();
        } catch (IOException e) {
            System.err.println("Unable to open examples dialog.");
            e.printStackTrace();
        }
    }

}