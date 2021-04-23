package simulation;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Model of robot Otto with joints -> enables physics simulation.
 */
public class RobotOttoJoints implements RobotOtto {

    // Node = point of rotation
    public final Node ottoNode, ottoBottomNode, ottoArmRightNode, ottoArmLeftNode,
            ottoLegRightNode, ottoLegLeftNode, ottoFootRightNode, ottoFootLeftNode;

    // joint constrains movement to some axes and angles
    private final HingeJoint legLeftJoint, armLeftJoint, armRightJoint, legRightJoint,
            footLeftJoint, footRightJoint;

    // RigidBodyControl enables physics application (mass and shape -> collision)
    private final RigidBodyControl armLeftControl, armRightControl, legLeftControl, legRightControl,
            footLeftControl, footRightControl;


    /**
     * Load parts of Otto model and assemble robot.
     */
    public RobotOttoJoints(AssetManager assetManager, BulletAppState bulletAppState) {

        // load 3D models
        Spatial ottoTopSpatial = assetManager.loadModel("models/otto-top.obj");
        Spatial ottoEyesSpatial = assetManager.loadModel("models/otto-eyes.obj");
        Spatial ottoBottomSpatial = assetManager.loadModel("models/otto-bottom.obj");
        Spatial ottoArmRightSpatial = assetManager.loadModel("models/otto-arm-right.obj");
        Spatial ottoArmLeftSpatial = assetManager.loadModel("models/otto-arm-left.obj");
        Spatial ottoLegRightSpatial = assetManager.loadModel("models/otto-leg-right.obj");
        Spatial ottoFootRightSpatial = assetManager.loadModel("models/otto-foot-right.obj");
        Spatial ottoLegLeftSpatial = assetManager.loadModel("models/otto-leg-left.obj");
        Spatial ottoFootLeftSpatial = assetManager.loadModel("models/otto-foot-left.obj");

        // setup materials
        Material blueMaterial = createMaterial(assetManager, ColorRGBA.Blue);
        Material redMaterial = createMaterial(assetManager, ColorRGBA.Red);
        Material cyanMaterial = createMaterial(assetManager, ColorRGBA.Cyan);
        Material grayMaterial = createMaterial(assetManager, ColorRGBA.Gray);

        // assign materials to models
        ottoTopSpatial.setMaterial(cyanMaterial);
        ottoEyesSpatial.setMaterial(grayMaterial);
        ottoBottomSpatial.setMaterial(blueMaterial);
        ottoArmRightSpatial.setMaterial(cyanMaterial);
        ottoArmLeftSpatial.setMaterial(cyanMaterial);
        ottoLegRightSpatial.setMaterial(cyanMaterial);
        ottoFootRightSpatial.setMaterial(blueMaterial);
        ottoLegLeftSpatial.setMaterial(cyanMaterial);
        ottoFootLeftSpatial.setMaterial(redMaterial);

        // fix rotation
        ottoTopSpatial.rotate(0, -FastMath.HALF_PI, 0);
        ottoBottomSpatial.rotate(-FastMath.HALF_PI, FastMath.PI, 0);

        // ASSEMBLE OTTO
        Quaternion q = new Quaternion();

        // move model origin relative to corresponding node (axis of rotation)
        ottoTopSpatial.move(0.54f,39.8555f, -2.21f);
        ottoArmRightSpatial.move(0,-16.5874f, 0f);
        ottoArmLeftSpatial.move(0,-16.5874f, 0f);
        ottoFootRightSpatial.move(0,7f, -12.0665f);
        ottoFootLeftSpatial.move(0f, 7.81f, -12.0665f);

    // CENTRAL NODE
        ottoNode = new Node("ottoNode");

    // BOTTOM
        ottoBottomNode = new Node("ottoBottomNode");
        ottoBottomNode.attachChild(ottoBottomSpatial); // attach visible model
        bulletAppState.setDebugEnabled(true);
        CollisionShape bottomShape = CollisionShapeFactory.createBoxShape(ottoBottomNode);
        RigidBodyControl bottomControl = new RigidBodyControl(bottomShape, 1000);
        bottomControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        ottoBottomNode.addControl(bottomControl);
        bottomControl.setPhysicsLocation(new Vector3f(0f,100, 0f));
        ottoNode.attachChild(ottoBottomNode);
        bulletAppState.getPhysicsSpace().add(ottoBottomNode);

    // TOP
        ottoBottomNode.attachChild(ottoTopSpatial);

    // EYES
        ottoBottomNode.attachChild(ottoEyesSpatial);

    // ARM RIGHT
        ottoArmRightNode = new Node("ottoArmRightNode");
        ottoArmRightNode.attachChild(ottoArmRightSpatial);
        CollisionShape armRightShape = CollisionShapeFactory.createBoxShape(ottoArmRightNode);
        armRightControl = new RigidBodyControl(armRightShape,10);
        ottoArmRightNode.addControl(armRightControl);
        armRightControl.setPhysicsLocation(new Vector3f(50.39f,111f,0f));
        armRightControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        ottoBottomNode.attachChild(ottoArmRightNode);
        bulletAppState.getPhysicsSpace().add(ottoArmRightNode);

        armRightJoint = new HingeJoint(ottoBottomNode.getControl(RigidBodyControl.class), //A
                ottoArmRightNode.getControl(RigidBodyControl.class),                      //B
                new Vector3f(-49.6626f,10.206f, 0.31f), // pivot point A
                new Vector3f(0, 0f, 0f),                // pivot point B
                Vector3f.UNIT_Z,  // DOF A
                Vector3f.UNIT_Z); // DOF B
        bulletAppState.getPhysicsSpace().add(armRightJoint);

    // ARM LEFT
        ottoArmLeftNode = new Node("ottoArmLeftNode");
        ottoArmLeftNode.attachChild(ottoArmLeftSpatial);

        CollisionShape armLeftShape = CollisionShapeFactory.createBoxShape(ottoArmLeftNode);
        armLeftControl = new RigidBodyControl(armLeftShape,10);
        ottoArmLeftNode.addControl(armLeftControl);
        armLeftControl.setPhysicsLocation(new Vector3f(50.39f,111f,0f));
        armLeftControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        ottoBottomNode.attachChild(ottoArmLeftNode);
        bulletAppState.getPhysicsSpace().add(ottoArmLeftNode);

        armLeftJoint = new HingeJoint(ottoBottomNode.getControl(RigidBodyControl.class),
                ottoArmLeftNode.getControl(RigidBodyControl.class),
                new Vector3f(50.39f, 11f, 0f),
                new Vector3f(0, 0f, 0f),
                Vector3f.UNIT_Z,
                Vector3f.UNIT_Z);
        bulletAppState.getPhysicsSpace().add(armLeftJoint);

    // LEG RIGHT
        ottoLegRightNode = new Node("ottoLegRightNode");
        ottoLegRightNode.attachChild(ottoLegRightSpatial);

        CollisionShape legRightShape = CollisionShapeFactory.createBoxShape(ottoLegRightNode);
        legRightControl = new RigidBodyControl(legRightShape,200);
        legRightControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_04);
        ottoLegRightNode.addControl(legRightControl);
        legRightControl.setPhysicsLocation(new Vector3f(-26.7f,63,0f));
        ottoNode.attachChild(ottoLegRightNode);
        bulletAppState.getPhysicsSpace().add(ottoLegRightNode);

        legRightJoint = new HingeJoint(ottoBottomNode.getControl(RigidBodyControl.class),
                ottoLegRightNode.getControl(RigidBodyControl.class),
                new Vector3f(-26.07f, -37.27f, 0f),
                new Vector3f(0, 0, 0f),
                Vector3f.UNIT_Y,
                Vector3f.UNIT_Y);
        bulletAppState.getPhysicsSpace().add(legRightJoint);

    // FOOT RIGHT
        ottoFootRightNode = new Node("ottoFootRightNode");
        ottoFootRightNode.attachChild(ottoFootRightSpatial);

        CollisionShape footRightShape = CollisionShapeFactory.createBoxShape(ottoFootRightNode);
        footRightControl = new RigidBodyControl(footRightShape,50);
        footRightControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_05);
        ottoFootRightNode.addControl(footRightControl);
        footRightControl.setPhysicsLocation(new Vector3f(-26f,50.5f,0f));
        q.fromAngles(-FastMath.HALF_PI,-FastMath.HALF_PI,0);
        footRightControl.setPhysicsRotation(q);
        ottoLegRightNode.attachChild(ottoFootRightNode);
        bulletAppState.getPhysicsSpace().add(ottoFootRightNode);

        footRightJoint = new HingeJoint(ottoLegRightNode.getControl(RigidBodyControl.class),
                ottoFootRightNode.getControl(RigidBodyControl.class),
                new Vector3f(0f, -12.4f, 1.2f),
                new Vector3f(0, 0, 0f),
                Vector3f.UNIT_X,
                Vector3f.UNIT_X);
        bulletAppState.getPhysicsSpace().add(footRightJoint);

    // LEG LEFT
        ottoLegLeftNode = new Node("ottoLegLeftNode");
        ottoLegLeftNode.attachChild(ottoLegLeftSpatial);

        CollisionShape legLeftShape = CollisionShapeFactory.createBoxShape(ottoLegLeftNode);
        legLeftControl = new RigidBodyControl(legLeftShape,200);
        legLeftControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_04);
        ottoLegLeftNode.addControl(legLeftControl);
        legLeftControl.setPhysicsLocation(new Vector3f(25f,63,0f));
        q.fromAngles(0,-FastMath.HALF_PI,0);
        legLeftControl.setPhysicsRotation(q);
        ottoNode.attachChild(ottoLegLeftNode);
        bulletAppState.getPhysicsSpace().add(ottoLegLeftNode);

        legLeftJoint = new HingeJoint(ottoBottomNode.getControl(RigidBodyControl.class),
                ottoLegLeftNode.getControl(RigidBodyControl.class),
                new Vector3f(25.06f, -37.27f, 0f),
                new Vector3f(0, 0, 0f),
                Vector3f.UNIT_Y,
                Vector3f.UNIT_Y);
        bulletAppState.getPhysicsSpace().add(legLeftJoint);

    // FOOT LEFT
        ottoFootLeftNode = new Node("ottoFootLeftNode");
        ottoFootLeftNode.attachChild(ottoFootLeftSpatial);
        CollisionShape footLeftShape = CollisionShapeFactory.createBoxShape(ottoFootLeftNode);
        footLeftControl = new RigidBodyControl(footLeftShape,50f);
        footLeftControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_07);
        ottoFootLeftNode.addControl(footLeftControl);
        footLeftControl.setPhysicsLocation(new Vector3f(25f,50.5f,0f));
        footLeftControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_06);
        q.fromAngles(-FastMath.HALF_PI,-FastMath.HALF_PI,0);
        footLeftControl.setPhysicsRotation(q);
        ottoLegLeftNode.attachChild(ottoFootLeftNode);
        bulletAppState.getPhysicsSpace().add(ottoFootLeftNode);

        footLeftJoint = new HingeJoint(ottoLegLeftNode.getControl(RigidBodyControl.class),
                ottoFootLeftNode.getControl(RigidBodyControl.class),
                new Vector3f(0f, -12.4f, 1.2f),
                new Vector3f(0, 0, 0f),
                Vector3f.UNIT_X,
                Vector3f.UNIT_X);
        bulletAppState.getPhysicsSpace().add(footLeftJoint);


        // move all motors to calibrated initial position (90 degrees)
        for (OttoMotor motor : OttoMotor.values()) setMotorPosition(motor, 90);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -100f, 0));
    }


    /*
     PUBLIC METHODS
     */

    // returns root node so model can be placed in scene
    public Node getRootNode() {
        return ottoNode;
    }

    // move separate motor to given position
    public void setMotorPosition(OttoMotor part, float degrees) {
        if (degrees < 0 || degrees > 180) return;
        float limit = FastMath.DEG_TO_RAD * degrees;
        switch (part) {
            case LEFT_HAND:
                limit = FastMath.TWO_PI - limit;
                limit = FastMath.PI - limit;
                armLeftJoint.setLimit(limit, limit);
                armLeftControl.activate();
                break;
            case RIGHT_HAND:
                armRightJoint.setLimit(limit, limit);
                armRightControl.activate();
                break;
            case LEFT_LEG:
                legLeftJoint.setLimit(limit, limit);
                legLeftControl.activate();
                break;
            case RIGHT_LEG:
                limit = FastMath.TWO_PI - limit;
                limit = FastMath.PI - limit;
                legRightJoint.setLimit(limit, limit);
                legRightControl.activate();
                break;
            case LEFT_FOOT:
                footLeftJoint.setLimit(limit, limit);
                footLeftControl.activate();
                break;
            case RIGHT_FOOT:
                limit = FastMath.PI - limit;
                footRightJoint.setLimit(limit, limit);
                footRightControl.activate();
                break;
        }
    }



    /*
     PRIVATE HELPER METHODS
     */

    // Creates material of provided color.
    private Material createMaterial(AssetManager assetManager, ColorRGBA color) {
        return Simulation.createMaterial(assetManager, color);
    }

}