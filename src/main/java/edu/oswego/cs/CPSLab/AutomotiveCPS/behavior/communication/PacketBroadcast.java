package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Behavior;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.AckPacket;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;

public class PacketBroadcast extends Behavior implements Callable<Void> {

    private String host = "255.255.255.255";
    private int port;
    private Packet packet;


    public PacketBroadcast(CPSCar car, int port, Packet packet) {
        super(car);
        this.packet = packet;
        this.port = port;
    }

    @Override
    public Void call() throws Exception {
        trySend();
        return null;
    }

    public void broadcast() {
        final Duration timeout = Duration.ofSeconds(1);
        boolean isAck = false;
        while (! isAck)
        {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            final Future<Void> handler = executorService.submit(this);
            try {
                handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                //listen for ack
                isAck = true;
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                handler.cancel(true);
            }
        }
    }

    public void trySend() {
        byte[] packetBytes = packet.getBytes();
        byte[] receivedAck = new byte[50];
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(host);
            socket.setBroadcast(true);

            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, group, port);
            System.out.println("Sending behavior packet from src MAC address: " + car.getVehicle().getAddress());
            System.out.println("PACKET: " + Arrays.toString(packetBytes));
            socket.send(packet);

            packet = new DatagramPacket(receivedAck, 50);
            socket.receive(packet);
            System.out.println("RECEIVED");
            Packet ackPacket = Packet.packetFactory(packet.getData());
            AckPacket ack = (AckPacket) ackPacket;
            if (! ack.getDestMacAddr().contains(car.getVehicle().getAddress())) throw new Exception();
            System.out.println("Sending behavior packet from src MAC address: " + car.getVehicle().getAddress());

        } catch (Exception ignored) {
        }
    }
}
