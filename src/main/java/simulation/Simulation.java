package simulation;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.jfx.injfx.JmeToJfxApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

public class Simulation extends JmeToJfxApplication {

    // todo: run in separate window OR inject to given element
    // start and run simulation in new window
    public static void startInNewWindow() {
        Simulation simulation = new Simulation();
        AppSettings settings = new AppSettings(true);
        simulation.setSettings(settings);
        simulation.setShowSettings(false);
        simulation.start();
    }

    // robot Otto
    private Spatial ottoBottomSpatial, ottoTopSpatial, ottoArmRightSpatial, ottoArmLeftSpatial,
                ottoLegRightSpatial, ottoLegLeftSpatial, ottoFootRightSpatial, ottoFootLeftSpatial;
    private Node ottoNode, ottoArmRightNode, ottoArmLeftNode,
                ottoLegRightNode, ottoLegLeftNode, ottoFootRightNode, ottoFootLeftNode;

    Quaternion startQuaternion = new Quaternion();
    Quaternion endQuaternion = new Quaternion();
    Quaternion startQuaternion2 = new Quaternion();
    Quaternion endQuaternion2 = new Quaternion();
    BulletAppState bulletAppState = new BulletAppState();

    RobotOtto robotOtto;

    @Override
    public void simpleInitApp() {
//        robotOtto = new RobotOtto(assetManager);
//        Node ottoRootNode = robotOtto.getModelRootNode();
//        ottoRootNode.move(25, 0, 10);
//        rootNode.attachChild(ottoRootNode);


        cam.setLocation(new Vector3f(0,0f,200f));
        cam.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);
        cam.update();
        cam.updateViewProjection();

        stateManager.attach(bulletAppState);

        // light
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, -1));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        // materials for models
        Material blueMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        blueMaterial.setBoolean("UseMaterialColors", true);
        blueMaterial.setColor("Ambient", ColorRGBA.Gray );
        blueMaterial.setColor("Diffuse", ColorRGBA.Blue );
        blueMaterial.setColor("Specular", ColorRGBA.White );
        blueMaterial.setFloat("Shininess", 1f);
        Material redMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        redMaterial.setBoolean("UseMaterialColors", true);
        redMaterial.setColor("Ambient", ColorRGBA.Gray );
        redMaterial.setColor("Diffuse", ColorRGBA.Red);
        redMaterial.setColor("Specular", ColorRGBA.White );
        redMaterial.setFloat("Shininess", 1f);
        Material cyanMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        cyanMaterial.setBoolean("UseMaterialColors", true);
        cyanMaterial.setColor("Ambient", ColorRGBA.Gray );
        cyanMaterial.setColor("Diffuse", ColorRGBA.Cyan);
        cyanMaterial.setColor("Specular", ColorRGBA.White );
        cyanMaterial.setFloat("Shininess", 1f);
        Material greenMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        greenMaterial.setBoolean("UseMaterialColors", true);
        greenMaterial.setColor("Ambient", ColorRGBA.Gray );
        greenMaterial.setColor("Diffuse", ColorRGBA.Green);
        greenMaterial.setColor("Specular", ColorRGBA.White );
        greenMaterial.setFloat("Shininess", 1f);

        // model load
        ottoTopSpatial = assetManager.loadModel("models/otto-top.obj");
        ottoBottomSpatial = assetManager.loadModel("models/otto-bottom.obj");
        ottoArmRightSpatial = assetManager.loadModel("models/otto-arm-right.obj");
        ottoArmLeftSpatial = assetManager.loadModel("models/otto-arm-left.obj");
        ottoLegRightSpatial = assetManager.loadModel("models/otto-leg-right.obj");
        ottoFootRightSpatial = assetManager.loadModel("models/otto-foot-right.obj");
        ottoLegLeftSpatial = assetManager.loadModel("models/otto-leg-left.obj");
        ottoFootLeftSpatial = assetManager.loadModel("models/otto-foot-left.obj");

        // model material
        ottoTopSpatial.setMaterial(cyanMaterial);
        ottoBottomSpatial.setMaterial(blueMaterial);
        ottoArmRightSpatial.setMaterial(cyanMaterial);
        ottoArmLeftSpatial.setMaterial(cyanMaterial);
        ottoLegRightSpatial.setMaterial(cyanMaterial);
        ottoFootRightSpatial.setMaterial(blueMaterial);
        ottoLegLeftSpatial.setMaterial(cyanMaterial);
        ottoFootLeftSpatial.setMaterial(redMaterial);

        //todo fix rotation
        ottoTopSpatial.rotate(0, -FastMath.HALF_PI, 0);
        ottoBottomSpatial.rotate(-FastMath.HALF_PI, FastMath.PI, 0);

        // ASSEMBLE OTTO
        // move model origin relative to corresponding node (axis of rotation)
        ottoTopSpatial.move(0.54f,39.8555f, -2.21f);
        ottoArmRightSpatial.move(0,-16.5874f, 0f);
        ottoArmLeftSpatial.move(0,-16.5874f, 0f);
        ottoFootRightSpatial.move(0,7f, -12.0665f);
        ottoFootLeftSpatial.move(0f, 7.81f, -12.0665f);

        // nodes = 'hinges'
        ottoNode = new Node("ottoNode");
        rootNode.attachChild(ottoNode);

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


        startQuaternion.fromAngleAxis(FastMath.HALF_PI, new Vector3f(0, 0, 1));
        endQuaternion.fromAngleAxis(FastMath.HALF_PI, new Vector3f(0,0,1));

        startQuaternion2.fromAngleAxis(FastMath.PI, Vector3f.UNIT_X);
        endQuaternion2.fromAngleAxis(0, Vector3f.UNIT_X);


        // move to calibrated position
        ottoLegRightNode.rotate(0, FastMath.HALF_PI, 0);
        ottoFootRightNode.rotate(-FastMath.HALF_PI, 0, 0);
        ottoLegLeftNode.rotate(0, -FastMath.HALF_PI, 0);
        ottoFootLeftNode.rotate(-FastMath.HALF_PI, 0, 0);
    }

    float time = 0f, continualTime = 0;
    float diff = 0.001f;

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf;
        continualTime += tpf;
        time %= 1;

        Quaternion tmp = new Quaternion();
        tmp.slerp(startQuaternion, endQuaternion, time);
        //ottoArmRightNode.setLocalRotation(tmp);

        tmp.slerp(startQuaternion2, endQuaternion2, time);
        //ottoFootRightNode.setLocalRotation(tmp);

        tmp.fromAngleAxis(-FastMath.PI * Math.round(continualTime) / 36, Vector3f.UNIT_X);
        ottoFootRightNode.setLocalRotation(tmp);

        //robotOtto.setMotorPosition(RobotOtto.OttoMotor.LEFT_FOOT, Math.round(continualTime));
    }

}
