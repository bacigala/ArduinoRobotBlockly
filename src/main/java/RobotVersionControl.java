import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class RobotVersionControl {
    private static ArrayList<RobotVersion> robotVersions = new ArrayList<>();
    private static final String pathToVersionsFolder = "src\\main\\resources\\robot-versions";
    private Properties loadedVersion;

    public RobotVersionControl() {}

    public ArrayList<RobotVersion> getAvailableVersions() {
        return robotVersions;
    }

    public void refreshAvailableVersionsList() {
        robotVersions = new ArrayList<>();

        File folder = new File(pathToVersionsFolder);
        File[] folderItems = folder.listFiles((directory, name) -> name.endsWith(".robotversion"));
        if (folderItems == null) throw new NullPointerException("Could not access versions folder.");
        for (File folderItem : folderItems) {
            if (folderItem.isFile()) {
                try {
                    InputStream input = new FileInputStream(folderItem);
                    Properties prop = new Properties();
                    prop.load(input);

                    RobotVersion rv = new RobotVersion(prop.getProperty("name"), folderItem.getName());
                    robotVersions.add(rv);
                } catch (Exception ex) {
                    System.err.println("some .robotversion files could not be loaded");
                }
            }
        }
    }

    public void loadVersion(RobotVersion version) throws FileNotFoundException {
        if (!robotVersions.contains(version))
            throw new FileNotFoundException("Such version does not exist.");

        File file = new File(pathToVersionsFolder + "\\" + version.fileName);
        try {
            InputStream input = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(input);
            loadedVersion = prop;
        } catch (Exception ex) {
            System.err.println("Unable to to load RobotVersion.");
        }
    }

    public String getProperty(String propertyName) {
        if (loadedVersion == null) throw new NullPointerException("No RobotVersion set.");
        String propertyValue;
        try {
            propertyValue = loadedVersion.getProperty(propertyName);
        } catch (Exception e) {
            System.err.println("Cannot read property " + propertyName);
            return "";
        }
        return propertyValue;
    }

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
