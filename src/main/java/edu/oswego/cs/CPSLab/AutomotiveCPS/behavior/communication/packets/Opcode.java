package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets;

public enum Opcode {
    UNDEF(-1),
    BEHAVIOR(0),
    ACK(1);

    private int codeInt;

    Opcode(int codeInt) {
        this.codeInt = codeInt;
    }

    public int getInt() { return this.codeInt; }

    public static Opcode getOpcode(int opcode) {
        switch (opcode) {
            case 0 : return BEHAVIOR;
            case 1 : return ACK;
            default: return UNDEF;
        }
    }
}
