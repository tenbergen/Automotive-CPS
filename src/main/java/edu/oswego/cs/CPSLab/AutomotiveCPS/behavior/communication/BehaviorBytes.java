package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication;

public enum BehaviorBytes {
    RECEIVED((byte) 0 ),
    ECHO((byte) 1),
    SAY((byte) 2),
    BREAK_LIGHTS_ON((byte) 3),
    CHANGE_SPEED((byte) 4);

    private final byte behaviorMessage;

    BehaviorBytes(byte behaviorMessage) {
        this.behaviorMessage = behaviorMessage;
    }

    public byte getByte() { return behaviorMessage; }
}
