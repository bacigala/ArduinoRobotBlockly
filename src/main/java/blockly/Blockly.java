package blockly;

import application.FXMLMainWindowController;
import FXMLDialogController.TextInputDialog;
import javafx.scene.web.WebEngine;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents Blockly instance.
 * interface for Blockly (JavaScript) <-> Main app (Java)
 */
public class Blockly {

    private final WebEngine webEngine;
    private String generator = "";

    public Blockly(WebEngine webEngine) throws IllegalArgumentException {
        if (webEngine == null) throw new IllegalArgumentException();
        this.webEngine = webEngine;
        this.webEngine.load(getClass().getResource("/google-blockly/index.html").toString());

        // enable JavaScript to open dialog for user input
        webEngine.setPromptHandler(prompt -> {
            try {
                return TextInputDialog.display(prompt.getMessage(), prompt.getDefaultValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        });

        // enable JavaScript to open alert dialog
        webEngine.setOnAlert(alert -> FXMLMainWindowController.showDialog(alert.getData()));

    // enable communication JS -> Java
//        webEngine.getLoadWorker().stateProperty().addListener(
//                new ChangeListener() {
//                    @Override
//                    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                        if (newValue != Worker.State.SUCCEEDED) return;
//                        JSObject window = (JSObject) webEngine.executeScript("window");
//                        window.setMember("javaCall", new JavaCall());
//                    }
//                });
    }

    // communication JS -> Java, JS can call window.javaCall.{methodOfThisClass()}
//    public class JavaCall {
//        public void workspaceChanged() {
//            System.out.println("Workspace has changed.");
//            //if (generatedCodeTextAreaStringProperty == null) System.err.println("NULL VOLE");
//            //generatedCodeTextAreaStringProperty.clear();
//            generatedCodeTextAreaStringProperty.setText(getCode());
//        }
//    }

    // set generator to be used when requesting generated code from Blockly
    public void setGenerator(String generator) {
        this.generator = generator;
    }

    // return code generated by blockly
    public String getCode() {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        if (generator.isEmpty()) throw new NullPointerException("Generator not set.");
        return (String) webEngine.executeScript("Blockly." + generator + ".workspaceToCode(workspace)");
    }

    // return workspace (can be saved and reloaded later)
    public String getWorkspace() throws NullPointerException{
        if (webEngine == null) throw new NullPointerException("No web engine.");
        return (String) webEngine.executeScript(
                "Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Blockly.mainWorkspace))");
    }

    public void setWorkspace(String workspace) {
        clearWorkspace();
        webEngine.executeScript(
                "Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, Blockly.Xml.textToDom(\"" + workspace + "\"));");
    }

    public void clearWorkspace() {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        webEngine.executeScript("Blockly.mainWorkspace.clear()");
    }

    public void setToolbox(String toolbox) {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        webEngine.executeScript("workspace.updateToolbox(\"" + toolbox + "\")");
    }

    public void showCategories(ArrayList<String> categories) {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        for (String category : categories)
            webEngine.executeScript("Blockly.mainWorkspace.toolbox_.getToolboxItemById(\""+ category + "\").show()");
    }

    public void hideCategories(ArrayList<String> categories) {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        for (String category : categories)
            webEngine.executeScript("Blockly.mainWorkspace.toolbox_.getToolboxItemById(\""+ category + "\").hide()");
    }

    public void centerWorkspace() {
        if (webEngine == null) throw new NullPointerException("No web engine.");
        webEngine.executeScript("Blockly.mainWorkspace.scrollCenter()");
    }

}
