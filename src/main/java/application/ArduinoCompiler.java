package application;

import FXMLDialogController.TextInputDialog;
import FXMLDialogController.TextOutputDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.FutureTask;

/**
 * Starts and communicates with CLI Arduino IDE.
 */
public class ArduinoCompiler {
    private static String ARDUINO_IDE_PATH = "";

    // calls Arduino IDE to verify current code, opens dialog showing Arduino ide output
    public static void verify(String filePath) {
        String command = "--verify --board arduino:avr:nano:cpu=atmega328old " + filePath;
        callArduinoIde(command);
    }

    // calls Arduino IDE to verify and upload code to selected COM, shows output of ide in new dialog
    public static void verifyAndUpload(String portName, String filePath) {
        String command = "--upload --board arduino:avr:nano:cpu=atmega328old --port " + portName + " " + filePath;
        callArduinoIde(command);
    }

    private static void callArduinoIde(String command) {
        // check if path to arduino IDE is set, request if not
        if (ARDUINO_IDE_PATH.isEmpty() && !loadArduinoIdePath()) return;

        // determine output stream
        PrintWriter printWriter = null;
        TextOutputDialog dialog;
        try {
            dialog = TextOutputDialog.getDialog("Arduino IDE task");
            dialog.show();
            printWriter = new PrintWriter(dialog.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            PrintWriter finalPrintWriter = printWriter;
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ProcessBuilder builder = new ProcessBuilder(
                            "cmd.exe", "/c", "cd \"" + ARDUINO_IDE_PATH + "\" && arduino_debug.exe " + command);
                    builder.redirectErrorStream(true);
                    Process p = builder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while (true) {
                        line = r.readLine();
                        if (line == null) break;
                        // ignore debug and trace messages
                        if (line.startsWith("DEBUG") || line.startsWith("TRACE") || line.startsWith("INFO")) continue;
                        finalPrintWriter.println(line);
                    }
                    finalPrintWriter.println(
                            p.exitValue() == 0 ? "Success!" : "Fail :(\n(EXIT VALUE: " + p.exitValue() + ")");
                    if (p.exitValue() == 0) {
                        Platform.runLater(() ->
                                FXMLMainWindowController.showDialog(
                                Alert.AlertType.INFORMATION, "Arduino IDE", "Task completed! :)")
                        );
                    } else {
                        Platform.runLater(() ->
                                FXMLMainWindowController.showDialog(
                                Alert.AlertType.ERROR, "Arduino IDE", "Unable to complete task :(")
                        );
                        checkArduinoIdePath();
                    }
                    return null;
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            Platform.runLater(() ->
                    FXMLMainWindowController.showDialog(
                    Alert.AlertType.ERROR, "ERROR", "Cannot operate on command line. :(")
            );
            checkArduinoIdePath();
            e.printStackTrace();
        }
    }

    /**
     * Application-level properties store path to Arduino IDE
     */

    // load Arduino IDE path from properties, prompt user if no properties are found
    private static boolean loadArduinoIdePath() {
        // load properties
        try {
            Properties appProps = new Properties();
            Path PropertyFile = Paths.get("ArduinoRobotBlockly.properties");
            Reader PropReader = Files.newBufferedReader(PropertyFile);
            appProps.load(PropReader);

            ARDUINO_IDE_PATH = appProps.getProperty("arduinoIdePath", "");
            PropReader.close();
        } catch (IOException e) {
            System.err.println("PropertiesFileNotFoundException: " + e.getMessage());
        }

        // if no properties are found -> prompt user to provide Arduino IDE path
        return !ARDUINO_IDE_PATH.isEmpty() || checkArduinoIdePath();
    }

    // prompt user to check currently set Arduino IDE path
    private static boolean checkArduinoIdePath() {
        final FutureTask<String> getArduinoPath = new FutureTask<>(() -> {
            String message = "Please check Arduino IDE PATH:";
            return TextInputDialog.display(message, ARDUINO_IDE_PATH);
        });
        Platform.runLater(getArduinoPath);
        try {
            ARDUINO_IDE_PATH = getArduinoPath.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ARDUINO_IDE_PATH.isEmpty()) {
            String header = "Path to Arduino IDE is not set.";
            String message = "You won't be able to verify and upload code to robot.";
            Platform.runLater(() ->
                    FXMLMainWindowController.showDialog(Alert.AlertType.WARNING, header, message)
            );
        } else {
            storeArduinoIdePath(ARDUINO_IDE_PATH);
        }
        return !ARDUINO_IDE_PATH.isEmpty();
    }

    // store new Arduino IDE path to properties file
    private static void storeArduinoIdePath(String newPath) {
        try {
            Properties appProps = new Properties();
            Path PropertyFile = Paths.get("ArduinoRobotBlockly.properties");
            try {
                Reader PropReader = Files.newBufferedReader(PropertyFile);
                appProps.load(PropReader);
            } catch (Exception e) {
                System.out.println("no recent property file found, creating new");
            }
            appProps.setProperty("arduinoIdePath", newPath);

            Writer PropWriter = Files.newBufferedWriter(PropertyFile);
            appProps.store(PropWriter, "application properties for ArduinoRobotBlockly app");
            PropWriter.close();
        } catch (Exception e) {
            System.err.println("Properties write exception: " + e.getMessage());
        }
    }

}
