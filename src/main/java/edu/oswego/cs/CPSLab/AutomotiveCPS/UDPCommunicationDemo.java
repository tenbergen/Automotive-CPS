package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationIntersectionUpdateMessage;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.BrakeLight;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.ChangeSpeed;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.BehaviorBytes;
//import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.Broadcast;
//import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.Listen;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.PacketBroadcast;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.PacketListener;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.communication.packets.BehaviorPacket;
import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.ConnectGUI;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Gregory Maldonado
 * @since  02.23.2022
 */

public class UDPCommunicationDemo implements Runnable {

    private static ArrayList<CPSCar> vehicles = new ArrayList<>();
    private static ArrayList<UDPCommunicationDemo> demos = new ArrayList<>();
    private CPSCar car;
    private static ConcurrentHashMap<String, Boolean> scanners = new ConcurrentHashMap<>();

    public UDPCommunicationDemo(Vehicle v) {
        car = new CPSCar(v);
        vehicles.add(car);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.out.println("Launching connector...");
        AnkiConnector anki = null;
        try {
            anki = new AnkiConnector("127.0.0.1", 6000);
        } catch (IOException ioe) {
            System.out.println("Error connecting to server. Is it running?");
            System.out.println("Exiting.");
            System.exit(0);
        }
        System.out.print(" looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();
//        vehicles = vehicles.stream()
//                .filter( x -> !(x.getAdvertisement().isCharging()))
//                .collect(Collectors.toList());
        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");
        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS!");
            System.out.println(" Now connecting to and doing stuff to your cars.");

            // If the vehicle ArrayList is size 1 then we don't have to thread
//            if (vehicles.size() == 1) {
//                System.out.println("The demo needs at least 2 vehicles for UDP communication. Exiting ...");
//                System.exit(1);
//            }
            // If the vehicle ArrayList is greater than size 1 then we have the thread to run simultaneously
                // threaded run
            int randomCar = new Random().nextInt(vehicles.size());
            for (int i = 0 ; i < vehicles.size(); i ++) {

//                System.out.println("   Setting Speed...");
//                SetSpeedMessage v1Speed = new SetSpeedMessage(100, 100);
//                v.sendMessage(v1Speed);

                UDPCommunicationDemo.Demo demo = new UDPCommunicationDemo.Demo(new UDPCommunicationDemo(vehicles.get(i)));
                UDPCommunicationDemo.scanners.put(vehicles.get(i).getColor(), false);
                demo.start();
                demos.add(demo.getDemo());
            }

//            for (Thread t : threads) {
//                t.join();
//            }
//            for (Thread t : threads) {
//                t.interrupt();
//            }
            anki.close();

        }


    }

    /**
     * Actions performed by a CPSCar - where the velocity calculation is called
     */
    public void run() {

        car.scanTrack();
        scanners.put(car.getVehicle().getColor(), true);
        SetSpeedMessage speedMessage = new SetSpeedMessage(0, 1000);
        car.getVehicle().sendMessage(speedMessage);

        while (new ArrayList<>(scanners.values()).stream().anyMatch(bool -> !bool)) {}

        speedMessage = new SetSpeedMessage(1000, 500);
        car.getVehicle().sendMessage(speedMessage);
        car.getVehicle().addMessageListener(LocalizationPositionUpdateMessage.class, new LocalizationPositionUpdateHandler(car));

        PacketListener packetListener = new PacketListener(car, 4444);
        Thread t1 = new Thread(packetListener);
        t1.start();

//            BehaviorPacket behaviorPacket = new BehaviorPacket(BrakeLight.class, car);
//            PacketBroadcast broadcaster = new PacketBroadcast(car, 4444, behaviorPacket);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            BehaviorPacket behaviorPacket = new BehaviorPacket(ChangeSpeed.class, car, new String[]{"200"});
//            PacketBroadcast broadcaster = new PacketBroadcast(car, 4444, behaviorPacket);
//
//            t2 = new Thread(broadcaster::broadcast);
//
//            t2.start();

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Listen listener = new Listen(car, 4444);
//        listener.receive();
//        if (broadcaster) {
//            Broadcast broadcast = new Broadcast(car, BehaviorBytes.BREAK_LIGHTS_ON, 4444);
//            broadcast.broadcast();
//        }
    }


    private static class Demo implements Runnable {
        UDPCommunicationDemo udpCommunicationDemo;
        Thread t = null;

        public Demo(UDPCommunicationDemo udpCommunicationDemo) {
            this.udpCommunicationDemo = udpCommunicationDemo;
        }

        @Override
        public void run() {
            udpCommunicationDemo.run();
        }

        public void start() {
            if (t==null) {
                String name = udpCommunicationDemo.car.getVehicle().getAdvertisement().getModel().toString();
                System.out.println("Starting UDP Demo");
                t = new Thread(this, name);
                t.start();
            }
        }

        public UDPCommunicationDemo getDemo() { return udpCommunicationDemo; }

        public Thread getThread() {
            return t;
        }

    }

    private class LocalizationPositionUpdateHandler implements MessageListener<LocalizationPositionUpdateMessage> {

        private CPSCar car;

        public LocalizationPositionUpdateHandler(CPSCar car) {
            this.car = car;
        }

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            Roadpiece roadpiece = Roadpiece.createFromId(m.getRoadPieceId());
            final int limit = 300;
            if (roadpiece.getType().equals("PowerzoneRoadpiece") && m.getSpeed() > limit) {
                changeSpeed(limit);
                broadcastSpeedChange(limit);
            }
        }

        private void changeSpeed(int speed) {
            ChangeSpeed changeSpeed = new ChangeSpeed(car);
            changeSpeed.changeSpeed(speed, 300);
        }

        private void broadcastSpeedChange(int limit) {

            BehaviorPacket behaviorPacket = new BehaviorPacket(ChangeSpeed.class, car, new String[]{String.valueOf(limit)});
            PacketBroadcast broadcaster = new PacketBroadcast(car, 4444, behaviorPacket);

            new Thread(broadcaster::broadcast).start();

        }


    }

}

