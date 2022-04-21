package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class Packet {

    protected Opcode opcode;
    protected String srcMacAddr;

    public Packet(Opcode opcode) {
        this.opcode = opcode;
    }

    public abstract byte[] getBytes();

    public Byte[] byteKlassConversion(byte[] bytes) {
        Byte[] Bytes = new Byte[bytes.length];
        for (int b = 0; b < bytes.length; b++) Bytes[b] = bytes[b];
        return Bytes;
    }

    public String getSrcMacAddr() {
        return srcMacAddr;
    }

    public byte[] ByteKlassConversion(List<Byte> Bytes) {
        byte[] bytes = new byte[Bytes.size()];
        for (int b = 0; b < Bytes.size(); b++) bytes[b] = Bytes.get(b);
        return bytes;

    }

    public static Packet packetFactory(byte[] bytes) {
        int opcode = new BigInteger( new byte[]{ bytes[0], bytes[1] } ).intValue();
        switch (opcode) {
            case 0 : return generateBehaviorPacket(bytes);
            case 1 : return generateAckPacket(bytes);
            default : return null;
        }
    }

    private static BehaviorPacket generateBehaviorPacket(byte[] bytes) {
        byte[] channel = new byte[]{bytes[2], bytes[3]};
        byte[] srcMac  = Arrays.copyOfRange(bytes, 4, 36);
        byte[] behaviorCode = new byte[]{bytes[36], bytes[37]};
        ArrayList<String> paramsAL = new ArrayList<>();

        // parse parameters
        if (bytes.length > 38) {
            int index = 38;
            ArrayList<Byte> paramAL = new ArrayList<>();
            while (index < bytes.length) {
                if (bytes[index] != "!".getBytes()[0])
                    paramAL.add(bytes[index]);
                else {
                    byte[] param = new byte[paramAL.size()];
                    for (int i = 0; i < paramAL.size(); i++) {
                        param[i] = paramAL.get(i);
                    }
                    paramsAL.add(new String(param));
                }
                index++;
            }
        }
        String[] params = new String[paramsAL.size()];
        for (int i = 0; i < paramsAL.size(); i++) {
            params[i] = paramsAL.get(i);
        }
        return new BehaviorPacket(new BigInteger(behaviorCode).intValue(), new String(srcMac), new BigInteger(channel).intValue(), params);
    }

    private static AckPacket generateAckPacket(byte[] bytes) {
        byte[] channel  = new byte[]{bytes[2], bytes[3]};
        byte[] srcMac  = Arrays.copyOfRange(bytes, 4, 16);
        byte[] destMac = Arrays.copyOfRange(bytes, 16, 28);
        byte[] behaviorCode = new byte[]{ bytes[28], bytes[29] };

        return new AckPacket( new BigInteger(behaviorCode).intValue(), new String(srcMac), new String(destMac), new BigInteger(channel).intValue() );
    }

}
