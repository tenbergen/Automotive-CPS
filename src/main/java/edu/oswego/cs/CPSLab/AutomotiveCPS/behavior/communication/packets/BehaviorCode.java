package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets;

import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Behavior;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.BrakeLight;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.ChangeSpeed;

public enum BehaviorCode {
    UNDEF(-1),
    BRAKELIGHT(0),
    CHANGESPEED(1);

    private int behaviorCode;

    BehaviorCode(int behaviorCode) {
        this.behaviorCode = behaviorCode;
    }

    public int getInt() { return behaviorCode; }

    public static BehaviorCode getBehaviorCode(Class<?> behaviorKlass) {
        if (behaviorKlass == BrakeLight.class) return BRAKELIGHT;
        if (behaviorKlass == ChangeSpeed.class) return CHANGESPEED;
        return UNDEF;
    }

    public static BehaviorCode getBehaviorCode(int behaviorCode) {
        switch (behaviorCode) {
            case 0 : return BRAKELIGHT;
            case 1 : return CHANGESPEED;
            default: return UNDEF;
        }
    }

}
