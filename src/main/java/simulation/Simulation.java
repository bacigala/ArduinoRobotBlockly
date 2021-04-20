package simulation;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.jfx.injfx.JmeToJfxApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;

public class Simulation extends JmeToJfxApplication {
    private RobotOtto robotOtto;
    private static final Box floor;

    static {
        floor = new Box(400f, .1f, 400f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
    }

    // start and run simulation in new window
//    public static void startInNewWindow() {
//        Simulation simulation = new Simulation();
//        AppSettings settings = new AppSettings(true);
//        simulation.setSettings(settings);
//        simulation.setShowSettings(false);
//        simulation.start();
//    }

    @Override
    public void simpleInitApp() {
        // physics
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);
        stateManager.attach(bulletAppState);

        // camera
        cam.setLocation(new Vector3f(0, 50f, 213f));
        cam.lookAt(new Vector3f(0, 77, 0), Vector3f.UNIT_Y);
        cam.update();
        cam.updateViewProjection();

        // light
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, -1));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        // floor setup
        Geometry floor_geo = new Geometry("Floor", floor);
        Material floor_mat = createMaterial(assetManager, ColorRGBA.LightGray);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        HullCollisionShape shape = new HullCollisionShape(floor_geo.getMesh());
        this.rootNode.attachChild(floor_geo);
        RigidBodyControl floor_phy = new RigidBodyControl(shape, 0.0f);
        floor_geo.addControl(floor_phy);
        floor_phy.setKinematic(true);
        bulletAppState.getPhysicsSpace().add(floor_phy);

        // robot Otto model SIMPLE
//        robotOtto = new RobotOttoSimple(assetManager);
//        Node ottoRootNode = robotOtto.getRootNode();
//        ottoRootNode.move(0, 67, 87);
//        CollisionShape ottoShape = CollisionShapeFactory.createMeshShape(ottoRootNode);
//        rootNode.attachChild(ottoRootNode);
//        RigidBodyControl otto_phy = new RigidBodyControl(ottoShape, 1000f);
//        otto_phy.setKinematic(true);
//        ottoRootNode.addControl(otto_phy);
//        bulletAppState.getPhysicsSpace().add(otto_phy);

        // robot Otto model with joints
        robotOtto = new RobotOttoJoints(assetManager, bulletAppState);
        rootNode.attachChild(robotOtto.getRootNode());
    }

    float time = 0f, continualTime = 0;
    float diff = 0.01f;
    float lastUpdate = 0;
    boolean newMoment = true;
    int oldMoment = 0;

    @Override
    public void simpleUpdate(float tpf) {
        continualTime += diff;
        time += diff;

        int moment = (int) Math.floor(time);
        if (moment >= motor.size()) {
            time = 0;
            lastUpdate = 0;
            System.err.println("REPLAY");
            return;
        }
        if (moment != oldMoment) {
            newMoment = true;
            oldMoment = moment;
        }

        switch (motor.get(moment)) {
            case LEFT_HAND:
                //float previousMomentPos = moment > 0 ? position.get(moment-1) : position.get(position.size()-1);
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.LEFT_HAND;
                    lArmPosInit = lArmPos;
                }
                lArmPos += (time - lastUpdate) * (position.get(moment) - lArmPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_HAND, lArmPos);
                break;
            case RIGHT_HAND:
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.RIGHT_HAND;
                    rArmPosInit = rArmPos;
                }
                rArmPos += (time - lastUpdate) * (position.get(moment) - rArmPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_HAND, rArmPos);
                break;
            case LEFT_LEG:
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.LEFT_LEG;
                    lLegPosInit = lLegPos;
                }
                lLegPos += (time - lastUpdate) * (position.get(moment) - lLegPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_LEG, lLegPos);
                break;
            case RIGHT_LEG:
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.RIGHT_LEG;
                    rLegPosInit = rLegPos;
                }
                rLegPos += (time - lastUpdate) * (position.get(moment) - rLegPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_LEG, rLegPos);
                break;
            case LEFT_FOOT:
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.LEFT_FOOT;
                    lFootPosInit = lFootPos;
                }
                lFootPos += (time - lastUpdate) * (position.get(moment) - lFootPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_FOOT, lFootPos);
                break;
            case RIGHT_FOOT:
                if (newMoment) {
                    latsMovedLimb = RobotOttoSimple.OttoMotor.RIGHT_FOOT;
                    rFootPosInit = rFootPos;
                }
                rFootPos += (time - lastUpdate) * (position.get(moment) - rFootPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_FOOT, rFootPos);
                break;
        }

        lastUpdate = time;
        newMoment = false;
    }

    private final ArrayList<RobotOttoSimple.OttoMotor> motor = new ArrayList<>();
    private final ArrayList<Float> position = new ArrayList<>();

    float lArmPos = 90;
    float rArmPos = 90;
    float lLegPos = 90;
    float rLegPos = 90;
    float lFootPos = 90;
    float rFootPos = 90;

    float lArmPosInit = 90;
    float rArmPosInit = 90;
    float lLegPosInit = 90;
    float rLegPosInit = 90;
    float lFootPosInit = 90;
    float rFootPosInit = 90;

    RobotOttoSimple.OttoMotor latsMovedLimb = RobotOttoSimple.OttoMotor.RIGHT_FOOT;

    public void loadOttoProgram(String program) {
        motor.clear();
        position.clear();

        String[] programLines = program.split("[\r\n]+");

        for (int lineNo = 0; lineNo < programLines.length; lineNo++) {
            programLines[lineNo] = programLines[lineNo].replaceFirst("\\s*@\\s*]", "");
            if (programLines[lineNo].isEmpty()) continue; // ignore starting @
            if (programLines[lineNo].contains("#")) continue; // ignore comments

            String[] lineParts = programLines[lineNo].split("\\s+");
            int motorNo = Integer.parseInt(lineParts[1]);
            if (motorNo >= 1 && motorNo <= 6) {
                switch (motorNo) {
                    case 1:
                        motor.add(RobotOttoSimple.OttoMotor.RIGHT_FOOT);
                        break;
                    case 2:
                        motor.add(RobotOttoSimple.OttoMotor.LEFT_FOOT);
                        break;
                    case 3:
                        motor.add(RobotOttoSimple.OttoMotor.RIGHT_LEG);
                        break;
                    case 4:
                        motor.add(RobotOttoSimple.OttoMotor.LEFT_LEG);
                        break;
                    case 5:
                        motor.add(RobotOttoSimple.OttoMotor.LEFT_HAND);
                        break;
                    case 6:
                        motor.add(RobotOttoSimple.OttoMotor.RIGHT_HAND);
                        break;
                }
                float pos = Float.parseFloat(lineParts[2]);
                position.add(pos);
            }
        }
    }

    public static Material createMaterial(AssetManager assetManager, ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", ColorRGBA.Gray);
        material.setColor("Diffuse", color);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 1f);
        return material;
    }

}