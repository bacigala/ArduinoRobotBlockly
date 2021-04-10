package simulation;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Simple model of robot Otto.
 */
public class RobotOttoSimple implements RobotOtto {
    // Node = point of rotation
    private final Node ottoNode, ottoArmRightNode, ottoArmLeftNode,
            ottoLegRightNode, ottoLegLeftNode, ottoFootRightNode, ottoFootLeftNode;

    public RobotOttoSimple(AssetManager assetManager) {
        // Spatial = model of robot part
        Spatial ottoBottomSpatial, ottoTopSpatial, ottoArmRightSpatial, ottoArmLeftSpatial,
                ottoLegRightSpatial, ottoLegLeftSpatial, ottoFootRightSpatial, ottoFootLeftSpatial;

        // load models
        ottoTopSpatial = assetManager.loadModel("models/otto-top.obj");
        ottoBottomSpatial = assetManager.loadModel("models/otto-bottom.obj");
        ottoArmRightSpatial = assetManager.loadModel("models/otto-arm-right.obj");
        ottoArmLeftSpatial = assetManager.loadModel("models/otto-arm-left.obj");
        ottoLegRightSpatial = assetManager.loadModel("models/otto-leg-right.obj");
        ottoFootRightSpatial = assetManager.loadModel("models/otto-foot-right.obj");
        ottoLegLeftSpatial = assetManager.loadModel("models/otto-leg-left.obj");
        ottoFootLeftSpatial = assetManager.loadModel("models/otto-foot-left.obj");

        // setup materials
        Material blueMaterial = createMaterial(assetManager, ColorRGBA.Blue);
        Material redMaterial = createMaterial(assetManager, ColorRGBA.Red);
        Material cyanMaterial = createMaterial(assetManager, ColorRGBA.Cyan);

        // assign materials to models
        ottoTopSpatial.setMaterial(cyanMaterial);
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

        // move model origin relative to corresponding node (axis of rotation)
        ottoTopSpatial.move(0.54f,39.8555f, -2.21f);
        ottoArmRightSpatial.move(0,-16.5874f, 0f);
        ottoArmLeftSpatial.move(0,-16.5874f, 0f);
        ottoFootRightSpatial.move(0,7f, -12.0665f);
        ottoFootLeftSpatial.move(0f, 7.81f, -12.0665f);

        // nodes = 'hinges' = axes of rotation
        ottoNode = new Node("ottoNode");

        //top
        ottoNode.attachChild(ottoTopSpatial);

        //bottom
        ottoNode.attachChild(ottoBottomSpatial);

        //right arm
        ottoArmRightNode = new Node("ottoArmRightNode");
        ottoArmRightNode.move(new Vector3f(-49.6626f,10.206f, 0.31f));
        ottoArmRightNode.attachChild(ottoArmRightSpatial);
        ottoNode.attachChild(ottoArmRightNode);

        //left arm
        ottoArmLeftNode = new Node("ottoArmLeftNode");
        ottoArmLeftNode.move(new Vector3f(50.53f,10.206f, 0.31f));
        ottoArmLeftNode.attachChild(ottoArmLeftSpatial);
        ottoNode.attachChild(ottoArmLeftNode);

        //right leg
        ottoLegRightNode = new Node("ottoLegRightNode");
        ottoLegRightNode.move(-25.5969f,-37.5807f, -1.22899f);
        ottoLegRightNode.attachChild(ottoLegRightSpatial);
        ottoNode.attachChild(ottoLegRightNode);

        //right foot
        ottoFootRightNode = new Node("ottoFootRightNode");
        ottoFootRightNode.move(-1.2f,-12.4261f, 0);
        ottoFootRightNode.attachChild(ottoFootRightSpatial);
        ottoLegRightNode.attachChild(ottoFootRightNode);

        //left leg
        ottoLegLeftNode = new Node("ottoLegLeftNode");
        ottoLegLeftNode.move(26.4073f,-37.5556f, -1.24322f);
        ottoLegLeftNode.attachChild(ottoLegLeftSpatial);
        ottoNode.attachChild(ottoLegLeftNode);

        //left foot
        ottoFootLeftNode = new Node("ottoFootLeftNode");
        ottoFootLeftNode.move(0,-12.4261f, 1.2f);
        ottoFootLeftNode.attachChild(ottoFootLeftSpatial);
        ottoLegLeftNode.attachChild(ottoFootLeftNode);

        // move motors to calibrated position (90 degrees for each motor)
        for (OttoMotor motor : OttoMotor.values()) setMotorPosition(motor, 90);
    }


    /*
     PUBLIC METHODS
     */

    // returns root node so model can be placed in scene
    public Node getRootNode() {
        return ottoNode;
    }

    // updates model during simulation
    public void update(Quaternion tpf) {
        ottoLegLeftNode.setLocalRotation(tpf);
    }

    // move separate motor to given position
    public void setMotorPosition(OttoMotor part, float degrees) {
        if (degrees < 0 || degrees > 180) return;
        Quaternion q = new Quaternion();
        switch (part) {
            case LEFT_HAND:
                q.fromAngles(0,0, FastMath.PI - FastMath.DEG_TO_RAD * degrees);
                ottoArmLeftNode.setLocalRotation(q);
                break;
            case RIGHT_HAND:
                q.fromAngles(0,0,FastMath.TWO_PI - FastMath.DEG_TO_RAD * degrees);
                ottoArmRightNode.setLocalRotation(q);
                break;
            case LEFT_LEG:
                q.fromAngles(0, FastMath.TWO_PI - FastMath.DEG_TO_RAD * degrees, 0);
                ottoLegLeftNode.setLocalRotation(q);
                break;
            case RIGHT_LEG:
                q.fromAngles(0, FastMath.PI - FastMath.DEG_TO_RAD * degrees, 0);
                ottoLegRightNode.setLocalRotation(q);
                break;
            case LEFT_FOOT:
                q.fromAngles(FastMath.TWO_PI - FastMath.DEG_TO_RAD * degrees,0,0);
                ottoFootLeftNode.setLocalRotation(q);
                break;
            case RIGHT_FOOT:
                q.fromAngles(FastMath.PI + FastMath.DEG_TO_RAD * degrees,0,0);
                ottoFootRightNode.setLocalRotation(q);
                break;
        }
    }

    // Creates material of provided color.
    private Material createMaterial(AssetManager assetManager, ColorRGBA color) {
        return Simulation.createMaterial(assetManager, color);
    }

}