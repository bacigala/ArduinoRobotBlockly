package simulation;

import com.jme3.scene.Node;

public interface RobotOtto {

    enum OttoMotor {
        LEFT_HAND, RIGHT_HAND, LEFT_LEG, RIGHT_LEG, LEFT_FOOT, RIGHT_FOOT
    }

    void setMotorPosition(OttoMotor ottoMotor, float degrees);

    Node getRootNode();

}
