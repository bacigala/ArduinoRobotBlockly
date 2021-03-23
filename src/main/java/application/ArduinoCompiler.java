package application;

import dialog.FXMLTextOutputDialogController;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ArduinoCompiler {
    private final static String ARDUINO_IDE_PATH = "\"C:\\Program Files (x86)\\Arduino\"";

    // calls Arduino IDE to verify current code, opens dialog showing Arduino ide output
    public static void verify(String filePath) {
        String command = "--verify --board arduino:avr:nano:cpu=atmega328old " + filePath;
        callArduinoIde(command, true);
    }

    // calls Arduino IDE to verify and upload code to selected board, shows output of ide in new dialog
    public static void verifyAndUpload(String portName, String filePath) {
        String command = "--upload --board arduino:avr:nano:cpu=atmega328old --port " + portName + " " + filePath;
        callArduinoIde(command, true);
    }

    // checks whether all required files by robot-version are available for compilation
    public static void checkFiles() {

    }

    private static void callArduinoIde(String command, boolean showOutput) {
        // determine output stream
        PrintWriter printWriter = null;
        FXMLTextOutputDialogController dialog = null;
        if (showOutput) {
            try {
                dialog = FXMLTextOutputDialogController.getDialog("Arduino IDE task");
                dialog.show();
                printWriter = new PrintWriter(dialog.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            printWriter = new PrintWriter(System.out, true);
        }

        try {
            PrintWriter finalPrintWriter = printWriter;
            FXMLTextOutputDialogController finalDialog = dialog;
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
                        if (line == null) {
                            break;
                        }
                        // ignore debug and trace messages
                        if (line.startsWith("DEBUG") || line.startsWith("TRACE") || line.startsWith("INFO")) continue;
                        finalPrintWriter.println(line);
                    }
                    finalPrintWriter.println(
                            p.exitValue() == 0 ? "Success!" : "Fail :(\n(EXIT VALUE: " + p.exitValue() + ")");
                    return null;
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            System.err.println("Cannot operate on command line. :(");
            e.printStackTrace();
        }
    }


    public void hello() {
        try {
            URL resource = ArduinoCompiler.class.getResource("/robot-versions/otto-basic/otto-basic.ino");
            String filepath = Paths.get(resource.toURI()).toFile().getAbsolutePath();

            //ProcessBuilder builder = new ProcessBuilder(
            //        "cmd.exe", "/c", "cd \"C:\\Program Files (x86)\\Arduino\" && arduino --verify " + filepath);


            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"C:\\Program Files (x86)\\Arduino\" && arduino_debug.exe --verify " + filepath);

            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("DEBUG") || line.startsWith("TRACE") || line.startsWith("INFO")) continue;
                System.out.println(line);
            }
            System.out.println("Arduino compiler exited with EXIT_VALUE " + p.exitValue());
        } catch (Exception e) {
            System.err.println("Cannot operate on command line. :(");
            e.printStackTrace();
        }
    }

    public void concatFiles(ArrayList<String> files, String outputFile) throws FileNotFoundException, IOException {
        OutputStream out = new FileOutputStream(outputFile);
        byte[] buf = new byte[10000];
        for (String file : files) {
            InputStream in = new FileInputStream(file);
            int b;
            while ( (b = in.read(buf)) >= 0)
                out.write(buf, 0, b);
            in.close();
        }
        out.close();
    }

}
