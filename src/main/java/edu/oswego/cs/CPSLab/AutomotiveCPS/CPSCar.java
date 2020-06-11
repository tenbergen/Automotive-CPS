package edu.oswego.cs.CPSLab.AutomotiveCPS;

import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Map;
import de.adesso.anki.AdvertisementData;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.RoadmapScanner;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.LocalizationTransitionUpdateMessage;
import de.adesso.anki.messages.Message;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetOffsetFromRoadCenterMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.EmergencyStop;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Follow;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Overtake;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private Map map;
    private List<String> cars;
    private List<Long> time;
    private Date date;
    private MulticastReceiver receiver;
    private MulticastPublisher publisher;
    private LocalizationPositionUpdateHandler lpuh;
    private Thread t;
    private int locationId;
    private int pieceId;
    private int virtualId;
    private boolean reverse;
    private int speed;
    private float offset;
    private int prevLocationId;
    private int prevId;
    private Section section;

    private RoadmapScanner scan;
    private boolean scanStarted;
    private ArrayList<Integer> pieceIDs;
    private Follow follow;
    private EmergencyStop emergStop;
    private Overtake over;

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
    public CPSCar(Vehicle v) {
        this.v = v;
        v.connect();
        virtualId = -1;
        lpuh = new LocalizationPositionUpdateHandler();
        v.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh);
        v.sendMessage(new LocalizationPositionUpdateMessage());
        v.sendMessage(new SetOffsetFromRoadCenterMessage(offset));

        cars = new ArrayList<String>();
        time = new ArrayList<Long>();
        date = new Date();
        t = new Thread(new PositionUpdater());
        t.start();
        Thread receiver = new Thread(new MulticastReceiver(v.getAdvertisement().getModel().name()));
        receiver.start();
        publisher = new MulticastPublisher();
        follow = new Follow(this);
        emergStop = new EmergencyStop(this);
        over = new Overtake(this);

        scan = new RoadmapScanner(v);
        scanStarted = false;
        pieceIDs = new ArrayList<Integer>();

        v.sendMessage(new SdkModeMessage());
        v.sendMessage(new SetSpeedMessage(400, 400));

//        try {
//            while (true) publisher.multicast("Hello! The Multicast was sent by " + id);
//        } catch (Exception e){
//            System.out.println("meh..." + e.getMessage());
//        }
    }

    public Map getMap() {
        return map;
    }

    public int getLocationId() {
        return locationId;
    }

    public int getPieceId() {
        return pieceId;
    }

    public int getVirtualId() {
        return virtualId;
    }

    public boolean getReverse() {
        return reverse;
    }

    public int getSpeed() {
        return speed;
    }

    public float getOffset() {
        return offset;
    }

    public int getPrevLocationId() {
        return prevLocationId;
    }

    public int getPrevId() {
        return prevId;
    }

    public Section getSection() {
        return section;
    }

    public void sendMessage(Message message) {
        v.sendMessage(message);
    }

    public List<String> getCarList() {
        return cars;
    }

    /**
     * Determines if a car that answered a broadcast is a) new, b) known, or c)
     * disappeared.
     */
    private void updateCPSNetwork(String[] parsed) {
        String self = v.getAddress();
        if (cars.contains(parsed[0])) {
            int piece = Integer.parseInt(parsed[1]);
            time.set(cars.indexOf(parsed[0]), date.getTime());
            if (piece > (virtualId + 2) % (map.size()) || piece < (virtualId - 2) % (map.size())) {
                time.remove(cars.indexOf(parsed[0]));
                cars.remove(parsed[0]);
            }
        } else {
            if (!self.equals(parsed[0]) && this.virtualId != -1) {
                int piece = Integer.parseInt(parsed[1]);
                if (piece <= (virtualId + 2) % (map.size()) && piece >= (virtualId - 2) % (map.size())) {
                    cars.add(parsed[0]);
                    time.add(date.getTime());
                }
            }
        }
        for (long t : time) {
            if (date.getTime() - t > 10000) {
                cars.remove(time.indexOf(t));
                time.remove(t);
            }
        }
    }

    // make the car aware of its own position
    private void updatePosition() throws IOException {
        if (map != null) {
            if (virtualId == -1 || (virtualId != map.indexOf(pieceId))) {
                if (!map.duplicate(pieceId)) {
                    section = map.lookup(pieceId, false);
                    virtualId = map.getBySection(section);
                    if (!this.reverse) {
                        prevId = map.getBySection(section.getPrev());
                    } else {
                        prevId = map.getBySection(section.getNext());
                    }
                }
            }
        } else {
            if (pieceId == 33 && !scanStarted) {
                reverse = lpuh.reverse;
                pieceIDs.clear();
                pieceIDs.add(pieceId);
                scan.startScanning();
                System.out.println(v.getAdvertisement().getModel().name() + ": Started Scanning... ");
                scanStarted = true;
            } else {
                if (pieceIDs != null && !pieceIDs.isEmpty()) {
                    if (!scan.isComplete()) {
                        if (pieceId != pieceIDs.get(pieceIDs.size() - 1)) {
                            pieceIDs.add(pieceId);
                        }
                    } else {
                        System.out.println(v.getAdvertisement().getModel().name() + ": Scan Completed... ");
                        scan.stopScanning();
                        System.out.println(v.getAdvertisement().getModel().name() + ": Stopped Scanning... ");
                        Roadmap tempMap = scan.getRoadmap();
                        System.out.println(pieceIDs);
                        System.out.println(tempMap.toList());
                        this.map = new Map(tempMap, this.reverse, pieceIDs);
                        map.generateTrack();
                        System.out.println(v.getAdvertisement().getModel().name() + ": Track Completed... ");
                    }
                }
            }
        }
        if (this.pieceId != lpuh.pieceId) {
            prevLocationId = locationId;
            if (virtualId != -1) {
                if (!reverse) {
                    section = section.getNext();
                    virtualId = map.getBySection(section);
                    prevId = map.getBySection(section.getPrev());
                } else {
                    section = section.getPrev();
                    virtualId = map.getBySection(section);
                    prevId = map.getBySection(section.getNext());
                }
            }
        }
        this.locationId = lpuh.locationId;
        this.pieceId = lpuh.pieceId;
        this.speed = lpuh.speed;
        this.offset = lpuh.offset;
        if (virtualId != -1) {
            v.sendMessage(new SetSpeedMessage(300, 100));
            follow.updateInfo();
            emergStop.updateInfo();
            over.updateInfo();

            String position = v.getAddress() + " " + virtualId + " " + locationId + " " + prevId + " " + prevLocationId + " " + reverse + " " + speed + " " + offset;
            publisher.multicast(position);
            System.out.println(position);
        }
    }

    /**
     * Handles the response from the vehicle from the
     * LocalizationPositionUpdateMessage. We need handler classes because
     * responses from the vehicles are asynchronous.
     */
    private class LocalizationPositionUpdateHandler implements MessageListener<LocalizationPositionUpdateMessage> {

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
                    Thread.sleep(300);
                } catch (IOException ex) {
                    Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class MulticastReceiver extends Thread {

        protected MulticastSocket socket = null;
        protected byte[] buf = new byte[256];
        private boolean stopped;
        String id;

        public MulticastReceiver(String id) {
            this.id = id;
        }

        public boolean stopMC() {
            this.stopped = true;
            return stopped;
        }

        public void run() {
            try {
                socket = new MulticastSocket(4446);
                InetAddress group = InetAddress.getByName("230.0.0.0");
                socket.joinGroup(group);
                while (!stopped) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(
                            packet.getData(), 0, packet.getLength());
                    String[] parsed = parseBroadcast(received);
                    updateCPSNetwork(parsed);
                    if (cars.contains(parsed[0])) {
                        System.out.println(Arrays.toString(parsed) + " was received by " + id);
                        follow.follow(received);
//                    try {
//                        over.overtake(received);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                        emergStop.emergStop(received);
                    }
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

    private String[] parseBroadcast(String received) {
        String[] parsed = received.split(" ");
        return parsed;

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

//disconnect - stop the thread
    public void disconnect() throws InterruptedException {
        v.disconnect();
        t.join();
        receiver.stopMC();
    }

//    
//    public static void main(String[] args){
//        CPSCar c1 = new CPSCar(null, null, "Car 1");
//    }
}
