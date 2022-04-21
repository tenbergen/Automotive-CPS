package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public final class BehaviorPacket extends Packet {

    private final BehaviorCode behaviorCode;
    private final int channel;
    private String[] params = new String[]{};

    public BehaviorPacket(Class<?> behavior, CPSCar car) {
        super(Opcode.BEHAVIOR);
        this.behaviorCode = BehaviorCode.getBehaviorCode(behavior);
        this.srcMacAddr = car.getVehicle().getAddress();
        this.channel = 0;
    }

    public BehaviorPacket(Class<?> behavior, CPSCar car, String[] params) {
        super(Opcode.BEHAVIOR);
        this.behaviorCode = BehaviorCode.getBehaviorCode(behavior);
        this.srcMacAddr = car.getVehicle().getAddress();
        this.channel = 0;
        this.params = params;
    }

    public BehaviorPacket(int behaviorCode, String srcMacAddr, int channel, String[] params) {
        super(Opcode.BEHAVIOR);
        this.behaviorCode = BehaviorCode.getBehaviorCode(behaviorCode);
        this.srcMacAddr = srcMacAddr;
        this.channel = channel;
        this.params = params;
    }

    @Override
    public byte[] getBytes() {
        ArrayList<Byte> bytes = new ArrayList<>();
        Collections.addAll(bytes, (byte) 0, (byte) opcode.getInt()); // opcode
        Collections.addAll(bytes, (byte) 0, (byte) channel); // channel
        Collections.addAll(bytes, byteKlassConversion(srcMacAddr.getBytes())); // address
        Collections.addAll(bytes, (byte) 0, (byte) behaviorCode.getInt()); // behavior code

        for (String param : params) {
            Collections.addAll(bytes, byteKlassConversion(param.getBytes()));
            Collections.addAll(bytes, byteKlassConversion("!".getBytes()));
        }

        return ByteKlassConversion(bytes);
    }

    public BehaviorCode getBehaviorCode() {
        return behaviorCode;
    }

    public String getSrcMacAddr() {
        return srcMacAddr;
    }

    public int getChannel() {
        return channel;
    }

    public String[] getParams() {
        return params;
    }
}
