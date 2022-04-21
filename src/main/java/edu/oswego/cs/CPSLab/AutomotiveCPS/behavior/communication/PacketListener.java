package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Behavior;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.BrakeLight;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.ChangeSpeed;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.AckPacket;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.BehaviorCode;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.BehaviorPacket;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketListener extends Behavior implements Runnable {

    private ConcurrentLinkedQueue<QueuedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private String host = "239.0.0.0";
    private int port;
    MulticastSocket socket = null;
    DatagramSocket datagramSocket = null;
    InetAddress group;

    public PacketListener(CPSCar car, int port) {
        super(car);
        this.port = port;
        new PacketParser().start();
        System.out.println("Vehicle: " + car.getVehicle().getAddress() + " starting Packet Parser & Multicast Listening on port " + port);
        try {
            group = InetAddress.getByName(host);
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            socket = new MulticastSocket(port);
            datagramSocket = new DatagramSocket();

            socket.joinGroup(group);
        } catch (Exception e){
            return;
        }
        while (true) {
            byte[] bytes = new byte[255];
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            executorService.execute( new RetrievedPacket(bytes, packet.getAddress(), packet.getPort()));
        }

    }

    protected final class QueuedPacket {
        protected InetAddress addr;
        protected Packet packet;
        protected int port;
        public QueuedPacket(InetAddress addr, Packet packet, int port) {
            this.addr = addr;
            this.packet = packet;
            this.port = port;
        }
    }


    private final class RetrievedPacket implements Runnable {

        private final byte[] bytes;
        private InetAddress addr;
        private int port;

        public RetrievedPacket(byte[] bytes, InetAddress addr, int port) {
            this.bytes = bytes;
            this.addr = addr;
            this.port = port;
        }

        @Override
        public void run() {
            Packet packet = Packet.packetFactory(bytes);
            if (packet == null) return;
            if (! car.getVehicle().getAddress().startsWith(packet.getSrcMacAddr())) {
                PacketListener.this.packetQueue.add(new QueuedPacket(addr, packet, port));
            }
        }
    }

    private final class PacketParser extends Thread {

        @Override
        public void run() {
            while (true) {
                if (packetQueue.size() > 0) {
                    QueuedPacket queuedPacket = packetQueue.poll();
                    Packet packet = queuedPacket.packet;

                    if (packet instanceof BehaviorPacket) {
                        BehaviorPacket behaviorPacket = (BehaviorPacket) packet;

                        //ACK the behavior request
                        AckPacket ack = new AckPacket(behaviorPacket.getBehaviorCode(), car, behaviorPacket.getSrcMacAddr());
                        DatagramPacket datagramPacket = new DatagramPacket(ack.getBytes(), ack.getBytes().length, queuedPacket.addr, queuedPacket.port);
                        try {
                            datagramSocket.send(datagramPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        executeBehavior(behaviorPacket, behaviorPacket.getParams());
                        System.out.println("BEHAVIOR CODE: " + behaviorPacket.getBehaviorCode());
                        System.out.println("PARAMS: " + Arrays.toString(behaviorPacket.getParams()));
                        System.out.println("MAC Address " + car.getVehicle().getAddress() + " executing behavior from " + behaviorPacket.getSrcMacAddr() );
                    }
                }
            }
        }

        public void executeBehavior(BehaviorPacket behaviorPacket, String[] args) {

            BehaviorCode behaviorCode = behaviorPacket.getBehaviorCode();

            if (behaviorCode == BehaviorCode.BRAKELIGHT) {
                BrakeLight brakeLight = new BrakeLight(car);
                brakeLight.turnOn();
            } else if (behaviorCode == BehaviorCode.CHANGESPEED) {
                int speed = 0;
                try {
                    speed = Integer.parseInt(args[0]);
                } catch (Exception ignored) { return; }
                ChangeSpeed changeSpeed = new ChangeSpeed(car);
                changeSpeed.changeSpeed(speed);
            }
        }


    }

}
