package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AdvertisementData;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.Message;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper for de.adesso.anki.Vehicle that adds minimal functionality needed
 * for vehicles to cyber-physically talk to one another. Simply use this class
 * to instantiate a Vehicle.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class CPSCar {

    private Vehicle v;
    private AnkiConnector anki;
    private List<CPSCar> cars;
    private List<Vehicle> vehicles;
    private MulticastReceiver receiver;
    private MulticastPublisher publisher;
    private LocalizationPositionUpdateHandler lpuh;

    private int locationId;
    private int pieceId;
    private boolean reverse;
    private int speed;
    private float offset;
    

    /**
     * Creates a new CPS car by simply calling the constructor of the super
     * class.
     */
//    public CPSCar(AnkiConnector anki, String address, String manufacturerData, String localName) {
//        super(anki, address, manufacturerData, localName);
//        this.anki = anki;
//        lpuh = new LocalizationPositionUpdateHandler();
//        v.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh);
//        v.sendMessage(new LocalizationPositionUpdateMessage());
//        
//        Thread t = new Thread(new PositionUpdater());
//        t.start();
//    }
    public CPSCar(Vehicle v, AnkiConnector anki, String id) {
  /*      this.v = v;
        v.connect();
        this.anki = anki;
        lpuh = new LocalizationPositionUpdateHandler();
        v.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh);
        v.sendMessage(new LocalizationPositionUpdateMessage());

        Thread t = new Thread(new PositionUpdater());
        t.start();
 */       Thread receiver = new Thread(new MulticastReceiver(id));
        receiver.start();
        publisher = new MulticastPublisher();
        try {
            while (true) publisher.multicast("Hello! The Multicast was sent by " + id);
        } catch (Exception e){
            System.out.println("meh..." + e.getMessage());
        }
    }

    public AnkiConnector getAnkiConnector() {
        return this.anki;
    }

    /**
     * Broadcasts a search for other CPSCars.
     */
    public void findVehicles() {
        List<Vehicle> vehicles = anki.findVehicles();
        for (Vehicle veh : vehicles) {
            if (veh.getAddress() == v.getAddress()) {
                vehicles.remove(veh);
            }
        }
        this.vehicles = vehicles;
    }

    public void updateVehicleNetwork() {
        List<Vehicle> update = anki.findVehicles();
        boolean exist = false;
        for (Vehicle veh : vehicles) {
            if (veh.getAddress() == v.getAddress()) {
                vehicles.remove(veh);
            }
        }
        if (update.size() != vehicles.size()) {
            // find out the new/disappeared one
        } else {
            for (Vehicle u : update) {
                for (Vehicle veh : vehicles) {
                    if (u == veh) {
                        exist = true;
                    }
                }
                if (exist == false) {
                    vehicles.add(u);
                }
            }
        }
    }

    public void sendMessage(Message message) {
        v.sendMessage(message);
    }

    public List<CPSCar> getCarList() {
        return cars;
    }

    /**
     * replies to other cars' broadcast.
     */
    public void replyBroadcast() {
        // need the LocalizationPositionUpdateMessage and Handler
    }

    /**
     * Determines if a car that answered a broadcast is a) new, b) known, or c)
     * disappeared.
     */
    private void updateCPSNetwork() {
        // TODO
    }

    // make the car aware of its own position
    private void updatePosition() throws IOException {
        this.locationId = lpuh.locationId;
        this.pieceId = lpuh.pieceId;
        this.reverse = lpuh.reverse;
        this.speed = lpuh.speed;
        this.offset = lpuh.offset;
        String position = "Piece: " + pieceId + " Location: " + locationId + "Reverse:" + reverse + " Speed: " + speed + " Offset: " + offset;
        publisher.multicast(position);
        // System.out.println("Current piece:" + pieceId + " Current location: " + locationId);
    }

    /**
     * Handles the response from the vehicle from the
     * LocalizationPositionUpdateMessage. We need handler classes because
     * responses from the vehicles are asynchronous.
     */
    private static class LocalizationPositionUpdateHandler implements MessageListener<LocalizationPositionUpdateMessage> {

        private int locationId;
        private int pieceId;
        private boolean reverse;
        private int speed;
        private float offset;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            locationId = m.getLocationId();
            pieceId = m.getRoadPieceId();
            reverse = m.isParsedReverse();
            speed = m.getSpeed();
            offset = m.getOffsetFromRoadCenter();
            System.out.println("   Right now we are on: " + pieceId + ". Location: " + locationId + ". ");
        }
    }

    private class PositionUpdater implements Runnable {

        public void run() {
            while (true) {
                try {
                    // sends a position update request to the vehicle
                    updatePosition();
                    Thread.sleep(100);
                } catch (IOException ex) {
                    Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    // tranfer the info b/t 2 CPSCars (broadcast) UDP connection
    //private class server: continuously broadcast location and ID(who you are) -> JSON
    //private class client: continuously receive/look for braodcast
    //receive broadcast
    //determine if it is here
    //here: update
    //not here: add car to the list
    private class MulticastReceiver extends Thread {

        protected MulticastSocket socket = null;
        protected byte[] buf = new byte[256];
        
        String id;
        
        public MulticastReceiver(String id) { this.id = id;}

        public void run() {
            try {
                socket = new MulticastSocket(4446);
                InetAddress group = InetAddress.getByName("230.0.0.0");
                socket.joinGroup(group);
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(
                            packet.getData(), 0, packet.getLength());
                    System.out.println(received + " was received by" + id);
                    if ("end".equals(received)) {
                        break;
                    }
                }
                socket.leaveGroup(group);
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class MulticastPublisher {

        private DatagramSocket socket;
        private InetAddress group;
        private byte[] buf;

        public void multicast(String multicastMessage) throws IOException {
            socket = new DatagramSocket();
            group = InetAddress.getByName("230.0.0.0");
            buf = multicastMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
            socket.send(packet);
            socket.close();
        }
    }
    
    
    public static void main(String[] args){
        CPSCar c1 = new CPSCar(null, null, "Car 2");
    }
}

