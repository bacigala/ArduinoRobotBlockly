package simulation;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.jfx.injfx.JmeToJfxApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main jMonkeyEngine simulation class.
 */
public class Simulation extends JmeToJfxApplication {

    /** Creates material of given color. */
    public static Material createMaterial(AssetManager assetManager, ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", ColorRGBA.Gray);
        material.setColor("Diffuse", color);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 1f);
        return material;
    }


    private RobotOtto robotOtto;
    private BulletAppState bulletAppState;
    private final AtomicInteger simulationSpeed;

    public Simulation(AtomicInteger simulationSpeed) {
        super();
        this.simulationSpeed = simulationSpeed;
    }

    // called on application startup
    @Override
    public void simpleInitApp() {
        // scene light
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, -1));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    // set simulation to "waiting" state
    public void idle() {
        waitForMainLoopPause();
        if (bulletAppState != null) {
            bulletAppState.stopPhysics();
            bulletAppState.cleanup();
            stateManager.detach(bulletAppState);
        }
        rootNode.detachAllChildren();
    }

    // reset scene objects, prepare simulation to run
    public void reset() {
        // physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // floor
        final Box floor = new Box(400f, .1f, 400f);
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(createMaterial(assetManager, ColorRGBA.LightGray));
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        HullCollisionShape shape = new HullCollisionShape(floor_geo.getMesh());
        rootNode.attachChild(floor_geo);
        RigidBodyControl floor_phy = new RigidBodyControl(shape, 0.0f);
        floor_geo.addControl(floor_phy);
        floor_phy.setKinematic(true);
        bulletAppState.getPhysicsSpace().add(floor_phy);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));

        // robot Otto model
        robotOtto = new RobotOttoJoints(assetManager, bulletAppState);
        rootNode.attachChild(robotOtto.getRootNode());

        // camera
        cam.setLocation(new Vector3f(0,50, 230));
        cameraRotationX = 0;
        cameraRotationY = FastMath.PI;
        cameraRotationZ = 0;
        rotateCamera(Axis.Y, 0); // set rotation

        // simulation playback
        time = 0;
        absoluteTime = 0;
        lastUpdate = 0;
        newMoment = true;
        oldMoment = 0;

        // RobotOttoSimple -> replaced by RobotOttoJoints
//        robotOtto = new RobotOttoSimple(assetManager);
//        Node ottoRootNode = robotOtto.getRootNode();
//        ottoRootNode.move(0, 67, 87);
//        CollisionShape ottoShape = CollisionShapeFactory.createMeshShape(ottoRootNode);
//        rootNode.attachChild(ottoRootNode);
//        RigidBodyControl otto_phy = new RigidBodyControl(ottoShape, 1000f);
//        otto_phy.setKinematic(true);
//        ottoRootNode.addControl(otto_phy);
//        bulletAppState.getPhysicsSpace().add(otto_phy);
    }

    public void waitForMainLoopPause() {
        synchronized (shouldRun) {
            shouldRun.set(false);
            while (running.get()) {
                try {
                    shouldRun.wait(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void waitForMainLoopResume() {
        synchronized (shouldRun) {
            shouldRun.set(true);
            while (!running.get()) {
                try {
                    shouldRun.notifyAll();
                    shouldRun.wait(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    final AtomicBoolean shouldRun = new AtomicBoolean(false);
    final AtomicBoolean running = new AtomicBoolean(true);

    float time = 0f, absoluteTime = 0;
    float lastUpdate = 0;
    boolean newMoment = true;
    int oldMoment = 0;

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

    /**
     * Main simulation update "loop".
     * Called repeatedly by update thread.
     */
    @Override
    public void simpleUpdate(float tpf) {
        // simulation pause = wait of update thread
        synchronized (shouldRun) {
            while (!shouldRun.get()) {
                running.set(false);
                try {
                    shouldRun.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        running.set(true);

        // manipulate Otto model according to program
        absoluteTime += (simulationSpeed.get() / 100f);
        time += (simulationSpeed.get() / 100f);
        int moment = (int) Math.floor(time);
        if (moment >= motor.size()) {
            time = 0;
            lastUpdate = 0;
            oldMoment = 0;
            newMoment = true;
            return;
        }
        if (moment != oldMoment) {
            newMoment = true;
            oldMoment = moment;
        }

        switch (motor.get(moment)) {
            case LEFT_HAND:
                if (newMoment) lArmPosInit = lArmPos;
                lArmPos += (time - lastUpdate) * (position.get(moment) - lArmPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_HAND, lArmPos);
                break;
            case RIGHT_HAND:
                if (newMoment) rArmPosInit = rArmPos;
                rArmPos += (time - lastUpdate) * (position.get(moment) - rArmPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_HAND, rArmPos);
                break;
            case LEFT_LEG:
                if (newMoment) lLegPosInit = lLegPos;
                lLegPos += (time - lastUpdate) * (position.get(moment) - lLegPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_LEG, lLegPos);
                break;
            case RIGHT_LEG:
                if (newMoment) rLegPosInit = rLegPos;
                rLegPos += (time - lastUpdate) * (position.get(moment) - rLegPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_LEG, rLegPos);
                break;
            case LEFT_FOOT:
                if (newMoment) lFootPosInit = lFootPos;
                lFootPos += (time - lastUpdate) * (position.get(moment) - lFootPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.LEFT_FOOT, lFootPos);
                break;
            case RIGHT_FOOT:
                if (newMoment) rFootPosInit = rFootPos;
                rFootPos += (time - lastUpdate) * (position.get(moment) - rFootPosInit);
                robotOtto.setMotorPosition(RobotOttoSimple.OttoMotor.RIGHT_FOOT, rFootPos);
                break;
        }

        lastUpdate = time;
        newMoment = false;
    }



    /**
     * Robot Otto @ program parser
     */

    private final ArrayList<RobotOtto.OttoMotor> motor = new ArrayList<>();
    private final ArrayList<Float> position = new ArrayList<>();

    // creates simulation instructions based on "@ Otto program"
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
        waitForMainLoopResume();
    }



    /**
     * Camera control
     */

    public enum Axis { X, Y, Z }
    private float cameraRotationX = 0, cameraRotationY = 0, cameraRotationZ = 0;
    private final Quaternion cameraRotation = new Quaternion();

    public void rotateCamera(Axis axis, float delta) {
        switch (axis) {
            case X:
                cameraRotationX += delta;
                break;
            case Y:
                cameraRotationY += delta;
                break;
            case Z:
                cameraRotationZ += delta;
                break;
        }
        cameraRotation.fromAngles(cameraRotationX, cameraRotationY, cameraRotationZ);
        cam.setRotation(cameraRotation);
        cam.updateViewProjection();
    }

    public void moveCamera(Axis axis, float delta) {
        Vector3f move = cam.getDirection();
        switch (axis) {
            case X:
                move.crossLocal(Vector3f.UNIT_Y);
                break;
            case Y:
                move.crossLocal(Vector3f.UNIT_Y);
                move.crossLocal(cam.getDirection());
                break;
            case Z:
                break;
        }
        cam.setLocation(cam.getLocation().add((move.normalize()).mult(delta)));
        cam.updateViewProjection();
    }

}