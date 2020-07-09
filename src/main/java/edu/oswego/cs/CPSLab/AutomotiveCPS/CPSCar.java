package edu.oswego.cs.CPSLab.AutomotiveCPS;

import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import de.adesso.anki.AdvertisementData;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.RoadmapScanner;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.ChangeLaneMessage;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.LocalizationTransitionUpdateMessage;
import de.adesso.anki.messages.Message;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetOffsetFromRoadCenterMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.roadpieces.IntersectionRoadpiece;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.EmergencyStop;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Follow;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.FourWayIntersection;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Overtake;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private RoadmapManager map;
    private List<String[]> cars;
    private List<Long> time;
    private Queue<String> intersection;
    private boolean approachingIntersection;
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
    private ArrayList<Boolean> reverses;
    private Follow follow;
    private EmergencyStop emergStop;
    private Overtake over;
    private FourWayIntersection fwi;

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
        v.sendMessage(new SetOffsetFromRoadCenterMessage(-68));

        cars = new ArrayList<String[]>();
        time = new ArrayList<Long>();
        date = new Date();
        intersection = new LinkedList<String>();
        approachingIntersection = false;
        t = new Thread(new PositionUpdater());
        t.start();
        Thread receiver = new Thread(new MulticastReceiver(v.getAdvertisement().getModel().name()));
        receiver.start();
        publisher = new MulticastPublisher();
        follow = new Follow(this);
        emergStop = new EmergencyStop(this);
        over = new Overtake(this);
        fwi = new FourWayIntersection(this);

        scan = new RoadmapScanner(v);
        scanStarted = false;
        pieceIDs = new ArrayList<Integer>();
        reverses = new ArrayList<Boolean>();

        v.sendMessage(new SdkModeMessage());
        v.sendMessage(new SetSpeedMessage((int) (300 + Math.random() * 300), 300));

//        try {
//            while (true) publisher.multicast("Hello! The Multicast was sent by " + id);
//        } catch (Exception e){
//            System.out.println("meh..." + e.getMessage());
//        }
    }

    public String getAddress() {
        return v.getAddress();
    }

    public RoadmapManager getMap() {
        return map;
    }
    public Vehicle getVehicle(){
        return v;
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

    public Overtake getOvertake() {
        return over;
    }

    public void sendMessage(Message message) {
        v.sendMessage(message);
    }

    public List<String[]> getCarList() {
        return cars;
    }

    public Queue<String> getIntersectionList() {
        return intersection;
    }

    /**
     * Determines if a car that answered a broadcast is a) new, b) known, or c)
     * disappeared.
     */
    private void updateCPSNetwork(String[] parsed) {
        String self = v.getAddress();
        if (inCarList(parsed[0]) != -1) {
            int piece = Integer.parseInt(parsed[1]);
            time.set(inCarList(parsed[0]), date.getTime());
            cars.set(inCarList(parsed[0]), parsed);
            if (piece > (virtualId + 2) % (map.size()) || piece < (virtualId - 2) % (map.size())) {
                if (!(atIntersection() || approachingIntersection)) {
                    time.remove(cars.indexOf(parsed));
                    cars.remove(parsed);
                    intersection.clear();
                } else if (!Boolean.parseBoolean(parsed[8])) {
                    time.remove(cars.indexOf(parsed));
                    cars.remove(parsed);
                    intersection.remove(parsed[0]);
                }
            }
        } else {
            if (this.virtualId != -1) {
                if (!self.equals(parsed[0])) {
                    int piece = Integer.parseInt(parsed[1]);
                    if (piece <= (virtualId + 2) % (map.size()) && piece >= (virtualId - 2) % (map.size())) {
                        cars.add(parsed);
                        time.add(date.getTime());
                        if (approachingIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection((virtualId + 1) % map.size(), Integer.parseInt(parsed[1]))) {
                            intersection.add(parsed[0]);
                        }
                    } else if (approachingIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection((virtualId + 1) % map.size(), Integer.parseInt(parsed[1]))) {
                        cars.add(parsed);
                        time.add(date.getTime());
                        intersection.add(parsed[0]);
                    }
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

    private int inCarList(String name) {
        int index = -1;
        for (String[] broadcast : cars) {
            if (broadcast[0].equals(name)) {
                index = cars.indexOf(broadcast);
            }
        }
        return index;
    }

    // make the car aware of its own position
    private void updatePosition() throws IOException {
        if (map != null) {
            if (virtualId == -1 || (virtualId != map.indexOf(pieceId))) {
                if (!map.duplicate(pieceId)) {
                    section = map.lookup(pieceId, false);
                    virtualId = map.getBySection(section);
                    if (!this.reverse) {
                        prevId = (virtualId + map.size() - 1) % map.size();
                    } else {
                        prevId = (virtualId + 1) % map.size();
                    }
                }
            }
        } else {
            if (pieceId == 33 && !scanStarted) {
                reverse = lpuh.reverse;
                scan.startScanning();
                System.out.println(v.getAdvertisement().getModel().name() + ": Started Scanning... ");
                pieceIDs.add(pieceId);
                reverses.add(lpuh.reverse);
                scanStarted = true;
            } else {
                if (pieceIDs != null && !pieceIDs.isEmpty()) {
                    if (!scan.isComplete()) {
                        if (pieceId != pieceIDs.get(pieceIDs.size() - 1)) {
                            pieceIDs.add(pieceId);
                            reverses.add(lpuh.reverse);
                        }
                    } else {
                        System.out.println(v.getAdvertisement().getModel().name() + ": Scan Completed... ");
                        scan.stopScanning();
                        System.out.println(v.getAdvertisement().getModel().name() + ": Stopped Scanning... ");
                        System.out.println(pieceIDs);
                        Roadmap tempMap = scan.getRoadmap();
                        tempMap.normalize();
                        System.out.println(tempMap.toList());
                        this.map = new RoadmapManager(tempMap, this.reverse, pieceIDs, reverses);
                        map.generateTrack();
                        System.out.println(v.getAdvertisement().getModel().name() + ": Track Completed... ");
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                        }
//                        v.sendMessage(new ChangeLaneMessage(68, 100, 100));
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
            follow.updateInfo();
            emergStop.updateInfo();
            over.updateInfo();

            String position = v.getAddress() + " " + virtualId + " " + locationId + " " + prevId + " " + prevLocationId + " " + reverse + " " + speed + " " + offset;
            publisher.multicast(position);
        }
    }

    private boolean atIntersection() {
        if (virtualId != -1) {
            int nextIndex = (virtualId + 1) % map.size();
            int currentIndex = virtualId;
            int prevIndex = (virtualId + map.size() - 1) % map.size();
            boolean nextIsIntersection = map.get(nextIndex).getPiece().getType().equals(IntersectionRoadpiece.class.getSimpleName());
            boolean currentIsIntersection = map.get(currentIndex).getPiece().getType().equals(IntersectionRoadpiece.class.getSimpleName());
            boolean prevIsIntersection = map.get(prevIndex).getPiece().getType().equals(IntersectionRoadpiece.class.getSimpleName());
            if (reverse) {
                if (currentIsIntersection) {
                    approachingIntersection = false;
                    if (!intersection.contains(v.getAddress())) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        v.sendMessage(new SetSpeedMessage(0, 12500));
                        intersection.add(v.getAddress());
                    }
                    return true;
                } else if (prevIsIntersection) {
                    approachingIntersection = true;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    v.sendMessage(new SetSpeedMessage(100, 100));
                }
            } else {
                if (currentIsIntersection) {
                    approachingIntersection = false;
                    if (!intersection.contains(v.getAddress())) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        v.sendMessage(new SetSpeedMessage(0, 12500));
                        intersection.add(v.getAddress());
                    }
                    return true;
                } else if (nextIsIntersection) {
                    approachingIntersection = true;
                    System.out.println("Approaching intersection... ");
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    v.sendMessage(new SetSpeedMessage(100, 100));
                }
            }
        }
        return false;
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
            //System.out.println("   Right now we are on: " + pieceId + ". Location: " + locationId + ". ");
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
                InetAddress group = InetAddress.getByName("230.0.0.4");
                socket.joinGroup(group);
                while (!stopped) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(
                            packet.getData(), 0, packet.getLength());
                    String[] parsed = parseBroadcast(received);
                    updateCPSNetwork(parsed);
                    if (cars.contains(parsed)) {
                        System.out.println(Arrays.toString(parsed) + " was received by " + id);
                        follow.follow(received);
                        emergStop.emergStop(received);
                    }
                    if (atIntersection()) {
                        fwi.run();
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
        receiver.stopMC();
        t.join();
      
    }

//    
//    public static void main(String[] args){
//        CPSCar c1 = new CPSCar(null, null, "Car 1");
//    }
}
