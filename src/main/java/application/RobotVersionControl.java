package application;

import javafx.scene.control.Alert;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Class responsible for RobotVersion properties loading and parsing.
 */
public class RobotVersionControl {

    // todo: maybe move versions outside app...
    private final String pathToVersionsFolder = "src\\main\\resources\\robot-versions";
    private final ArrayList<RobotVersion> robotVersions = new ArrayList<>();
    private Properties loadedVersion;

    public ArrayList<RobotVersion> getAvailableVersions() {
        return robotVersions;
    }

    public void refreshAvailableVersionsList() {
        robotVersions.clear();
        File folder = new File(pathToVersionsFolder);
        File[] folderItems = folder.listFiles((directory, name) -> name.endsWith(".robot"));
        if (folderItems == null) {
            FXMLMainWindowController.showDialog(
                    Alert.AlertType.ERROR, "ERROR", "Could not access versions folder.");
            return;
        }
        for (File folderItem : folderItems) {
            if (folderItem.isFile()) {
                try {
                    InputStream input = new FileInputStream(folderItem);
                    Properties prop = new Properties();
                    prop.load(input);
                    RobotVersion rv = new RobotVersion(prop.getProperty("name"), folderItem.getName());
                    robotVersions.add(rv);
                } catch (Exception ex) {
                    FXMLMainWindowController.showDialog("Some .robot files could not be loaded");
                }
            }
        }
    }

    public void loadVersion(RobotVersion version) throws IOException {
        if (!robotVersions.contains(version))
            throw new FileNotFoundException("Such version does not exist.");

        File file = new File(pathToVersionsFolder + "\\" + version.fileName);
        InputStream input = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(input);
        loadedVersion = prop;
    }

    public String getProperty(String propertyName, String ifNotSet) {
        if (loadedVersion == null) return ifNotSet;
        String propertyValue;
        try {
            propertyValue = loadedVersion.getProperty(propertyName, ifNotSet);
        } catch (Exception e) {
            System.err.println("Cannot read property " + propertyName);
            return "";
        }
        return propertyValue;
    }

    public String getProperty(String propertyName) {
        return getProperty(propertyName, null);
    }

    public String getModuleProperty(int moduleNumber, String property) {
        return getProperty("module" + moduleNumber + "." + property);
    }

    public String getExampleProperty(int exampleNumber, String property) {
        return getProperty("example" + exampleNumber + "." + property);
    }

    public ArrayList<String> getExampleModules(int exampleNo) {
        String modules = getExampleProperty(exampleNo, "modules");
        if (modules.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(modules.split(",[ ]*")));
    }

    public ArrayList<String> getAllCategories() {
        String categories = getProperty("categories");
        return new ArrayList<>(Arrays.asList(categories.split(",[ ]*")));
    }

    public ArrayList<String> getModuleCategories(int moduleNumber) {
        String categories = getModuleProperty(moduleNumber, "categories");
        if (categories.isEmpty()) return null;
        return new ArrayList<>(Arrays.asList(categories.split(",[ ]*")));
    }


    /**
     * Represents one .robot file.
     */
    public static class RobotVersion {
        private final String name, fileName;

        public RobotVersion(String name, String fileName) {
            this.name = name;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {return name;}

        public String getFileName() {return fileName;}
    }

}
