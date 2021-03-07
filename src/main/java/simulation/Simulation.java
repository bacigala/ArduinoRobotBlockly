package simulation;

import com.jme3.jfx.injfx.JmeToJfxApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

public class Simulation extends JmeToJfxApplication {
    RobotOtto robotOtto;

    // todo: run in separate window OR inject to given element
    // start and run simulation in new window
    public static void startInNewWindow() {
        Simulation simulation = new Simulation();
        AppSettings settings = new AppSettings(true);
        simulation.setSettings(settings);
        simulation.setShowSettings(false);
        simulation.start();
    }

    @Override
    public void simpleInitApp() {
        // camera
        cam.setLocation(new Vector3f(0,0f,200f));
        cam.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);
        cam.update();
        cam.updateViewProjection();

        // light
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, -1));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        // instance of robot Otto
        robotOtto = new RobotOtto(assetManager);
        Node ottoRootNode = robotOtto.getModelRootNode();
        rootNode.attachChild(ottoRootNode);
    }

    float time = 0f, continualTime = 0;
    float diff = 0.01f;
    int startAngle = 0;
    int endAngle = 180;

    @Override
    public void simpleUpdate(float tpf) {
        time += diff;
        if (time >= 1) {
            time = 1;
            diff *= -1;
        } else if (time <= 0) {
            time = 0;
            diff *= -1;
        }

        float curAngle = startAngle + time * (endAngle - startAngle);
        robotOtto.setMotorPosition(RobotOtto.OttoMotor.LEFT_HAND, curAngle);
        robotOtto.setMotorPosition(RobotOtto.OttoMotor.RIGHT_HAND, curAngle);
        robotOtto.setMotorPosition(RobotOtto.OttoMotor.LEFT_FOOT, curAngle);
    }

}
