package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Behavior;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.BrakeLight;

import java.util.ArrayList;
import java.util.Collections;

public final class AckPacket extends Packet {

    private final BehaviorCode behaviorCode;
    private final String destMacAddr;
    private final int channel;

    public AckPacket(BehaviorCode behaviorCode, CPSCar car, String destMacAddr) {
        super(Opcode.ACK);
        this.behaviorCode = behaviorCode;
        srcMacAddr = car.getAddress();
        this.destMacAddr = destMacAddr;
        this.channel = 0;
    }

    public AckPacket(int behaviorCode, String srcMacAddr, String destMacAddr, int channel) {
        super(Opcode.ACK);
        this.behaviorCode = BehaviorCode.getBehaviorCode(behaviorCode);
        this.srcMacAddr = srcMacAddr;
        this.destMacAddr = destMacAddr;
        this.channel = channel;
    }

    public String getDestMacAddr() {
        return destMacAddr;
    }



    @Override
    public byte[] getBytes() {
        ArrayList<Byte> bytes = new ArrayList<>();
        Collections.addAll(bytes, (byte) 0, (byte) opcode.getInt()); //opcode
        Collections.addAll(bytes, (byte) 0, (byte) channel); // channel
        Collections.addAll(bytes, byteKlassConversion(srcMacAddr.getBytes())); // src addr
        Collections.addAll(bytes, byteKlassConversion(destMacAddr.getBytes())); // dest addr
        Collections.addAll(bytes, (byte) 0, (byte) behaviorCode.getInt()); // behavior code
        return ByteKlassConversion(bytes);
    }
}
