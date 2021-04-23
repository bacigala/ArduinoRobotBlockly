package application;

import FXMLDialogController.TextOutputDialog;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Starts and communicates with CLI Arduino IDE.
 */
public class ArduinoCompiler {
    // todo: move this to some app-level properties
    private final static String ARDUINO_IDE_PATH = "\"C:\\Program Files (x86)\\Arduino\"";

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
                            "cmd.exe", "/c", "cd " + ARDUINO_IDE_PATH + " && arduino_debug.exe " + command);
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
                        FXMLMainWindowController.showDialog(
                                Alert.AlertType.INFORMATION, "Arduino IDE", "Task completed! :)");
                    } else {
                        FXMLMainWindowController.showDialog(
                                Alert.AlertType.ERROR, "Arduino IDE", "Unable to complete task :(");
                    }
                    return null;
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            FXMLMainWindowController.showDialog(
                    Alert.AlertType.ERROR, "ERROR", "Cannot operate on command line. :(");
            e.printStackTrace();
        }
    }

}
