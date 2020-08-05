package edu.oswego.cs.CPSLab.AutomotiveCPS;

import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import de.adesso.anki.AdvertisementData;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.RoadmapScanner;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.ChangeLaneMessage;
import de.adesso.anki.messages.LocalizationIntersectionUpdateMessage;
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
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private boolean atIntersection;
    private boolean approachingIntersection;
    private Date date;
    private MulticastReceiver receiver;
    private MulticastPublisher publisher;
    private LocalizationPositionUpdateHandler lpuh;
    private LocalizationTransitionUpdateHandler ltuh;
    private LocalizationIntersectionUpdateHandler liuh;
    private Thread t;
    private int locationId;
    private int pieceId;
    private int virtualId;
    private boolean reverse;
    private int speed;
    private float offset;
    private int prevLocationId;
    private int prevId;
    private int transition;

    private MapScanner scan;
    private boolean scanDone;
    private ArrayList<Integer> pieceIDs;
    private ArrayList<Boolean> reverses;
    private Roadmap tempMap;
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
        v.sendMessage(new SdkModeMessage());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        virtualId = -1;
        transition = 0;
        lpuh = new LocalizationPositionUpdateHandler();
        v.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh);
        v.sendMessage(new LocalizationPositionUpdateMessage());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        ltuh = new LocalizationTransitionUpdateHandler();
        v.addMessageListener(LocalizationTransitionUpdateMessage.class, ltuh);
        v.sendMessage(new LocalizationTransitionUpdateMessage());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        LocalizationIntersectionUpdateHandler liuh = new LocalizationIntersectionUpdateHandler();
        v.addMessageListener(LocalizationIntersectionUpdateMessage.class, liuh);
        v.sendMessage(new LocalizationIntersectionUpdateMessage());
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        v.sendMessage(new SetOffsetFromRoadCenterMessage(-68));
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }

        scan = new MapScanner(v);
        scanDone = false;
        pieceIDs = new ArrayList<Integer>();
        reverses = new ArrayList<Boolean>();

        cars = new ArrayList<String[]>();
        time = new ArrayList<Long>();
        date = new Date();
        intersection = new LinkedList<String>();
        atIntersection = false;
        approachingIntersection = false;
        t = new Thread(new PositionUpdater());
        receiver = new MulticastReceiver(v.getAdvertisement().getModel().name());
        publisher = new MulticastPublisher();
        follow = new Follow(this);
        emergStop = new EmergencyStop(this);
        over = new Overtake(this);
        fwi = new FourWayIntersection(this);

        //      scanTrack();
//        try {
//            while (true) publisher.multicast("Hello! The Multicast was sent by " + id);
//        } catch (Exception e){
//            System.out.println("meh..." + e.getMessage());
//        }
    }

    public void scanTrack() {
        v.sendMessage(new SetSpeedMessage(500, 1000));
        scan.startScanning();
        System.out.println(v.getAdvertisement().getModel().name() + ": Started Scanning... ");
        while (!scan.isComplete()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        scan.stopScanning();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(v.getAdvertisement().getModel().name() + ": Done Scanning... ");
        v.sendMessage(new SetSpeedMessage(0, 12500));
        tempMap = scan.getRoadmap();
        reverse = scan.getInitReverse();
        pieceIDs = scan.getPieceIDs();
        reverses = scan.getReverses();
        if (reverse) {
            tempMap.reverse();
            Collections.reverse(pieceIDs);
            Collections.reverse(reverses);
            for (int i = 0; i < pieceIDs.size(); i++) {
                if (pieceIDs.get(i) != 10) {
                    reverses.set(i, !reverses.get(i));
                }
            }
        }
        tempMap.normalize();
        int distance = pieceIDs.size() - 1 - pieceIDs.indexOf(34);
        Collections.rotate(pieceIDs, distance);
        Collections.rotate(reverses, distance);
        t.start();
        receiver.start();
        scanDone = true;

    }

    public String getAddress() {
        return v.getAddress();
    }

    public boolean scanDone() {
        return scanDone;
    }

    public ArrayList<Integer> getPieceIDs() {
        return pieceIDs;
    }

    public ArrayList<Boolean> getReverses() {
        return reverses;
    }

    public int lengthOfMap() {
        return pieceIDs.size();
    }

    public Roadmap getMap() {
        return tempMap;
    }

    public void setRoadmapMannager(RoadmapManager rm) {
        this.map = rm;
        System.out.println(v.getAdvertisement().getModel().name() + ": Track Completed... ");
    }

    public RoadmapManager getManager() {
        return map;
    }

    public Vehicle getVehicle() {
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
        int mapID = -1;
        try {
            mapID = this.map.getID();
        } catch (NullPointerException e) {
            // e.printStackTrace();
        }
        if (inCarList(parsed[0]) != -1) {
            int piece = Integer.parseInt(parsed[1]);
            time.set(inCarList(parsed[0]), date.getTime());
            cars.set(inCarList(parsed[0]), parsed);
            if (piece > (virtualId + 2) % (map.size()) || piece < (virtualId - 2) % (map.size())) {
                if (!(atIntersection || approachingIntersection)) {
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
                    if (mapID == Integer.parseInt(parsed[9])) {
                        if (piece <= (virtualId + 2) % (map.size()) && piece >= (virtualId - 2) % (map.size())) {
                            cars.add(parsed);
                            time.add(date.getTime());
                        }
                        boolean condition1 = approachingIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection(true, (virtualId + 1) % map.size(), Integer.parseInt(parsed[1]));
                        boolean condition2 = atIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection(true, (virtualId) % map.size(), Integer.parseInt(parsed[1]));
                        if (condition1 || condition2) {
                            intersection.add(parsed[0]);
                        }
                    } else {
                        boolean condition1 = approachingIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection(false, (virtualId + 1) % map.size(), Integer.parseInt(parsed[1]));
                        boolean condition2 = atIntersection && Boolean.parseBoolean(parsed[8]) && map.sameIntersection(false, (virtualId) % map.size(), Integer.parseInt(parsed[1]));
                        if (condition1 || condition2) {
                            cars.add(parsed);
                            time.add(date.getTime());
                            intersection.add(parsed[0]);
                        }
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
            if (virtualId == -1 || virtualId != map.indexOf(pieceId)) {
                if (!map.duplicate(pieceId)) {
                    Section s = map.lookup(pieceId);
                    if (!this.reverse) {
                        virtualId = (map.getBySection(s) + 1) % map.size();
                        prevId = (virtualId + map.size() - 1) % map.size();
                    } else {
                        virtualId = (map.getBySection(s) - 1) % map.size();
                        prevId = (virtualId + 1) % map.size();
                    }
                }
            }
        }
        if (this.locationId != lpuh.locationId) {
            prevLocationId = locationId;
        }
        this.locationId = lpuh.locationId;
        this.pieceId = lpuh.pieceId;
        this.speed = lpuh.speed;
        this.offset = lpuh.offset;
        if (virtualId != -1) {
            follow.updateInfo();
            emergStop.updateInfo();
            over.updateInfo();
            handlingIntersection();

            String position = v.getAddress() + " " + virtualId + " " + locationId + " " + prevId + " " + prevLocationId + " " + reverse + " " + speed + " " + offset + " " + atIntersection + " " + map.getID();
            publisher.multicast(position);
            // System.out.println(position);
        }
    }

    private void handlingIntersection() {
        if (virtualId != -1) {
            if (reverse) {
                int prevIndex = (virtualId + map.size() - 1) % map.size();

                boolean prevIsIntersection = map.get(prevIndex).getPiece().getType().equals(IntersectionRoadpiece.class
                        .getSimpleName());
                if (prevIsIntersection) {
                    approachingIntersection = true;
                    try {
                        Thread.sleep(5);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(CPSCar.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    v.sendMessage(new SetSpeedMessage(200, 100));
                }
            } else {
                int nextIndex = (virtualId + 1) % map.size();

                boolean nextIsIntersection = map.get(nextIndex).getPiece().getType().equals(IntersectionRoadpiece.class
                        .getSimpleName());
                if (nextIsIntersection) {
                    approachingIntersection = true;
                    try {
                        Thread.sleep(5);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(CPSCar.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    v.sendMessage(new SetSpeedMessage(200, 100));

                }
            }
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
        private int speed;
        private float offset;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            locationId = m.getLocationId();
            pieceId = m.getRoadPieceId();
            speed = m.getSpeed();
            offset = m.getOffsetFromRoadCenter();
            System.out.println(v.getAdvertisement().getModel().name() + ":   Right now we are on: " + virtualId + ". Location: " + locationId + ". ");
//                    if (reverse) {
//                        System.out.println(v.getAdvertisement().getModel().name() + ": true...");
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        v.sendMessage(new SetOffsetFromRoadCenterMessage(68));
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        v.sendMessage(new ChangeLaneMessage(-68, 100, 100));
//                    } else {
//                        System.out.println(v.getAdvertisement().getModel().name() + ": false...");
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        v.sendMessage(new SetOffsetFromRoadCenterMessage(-68));
//                        try {
//                            Thread.sleep(50);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        v.sendMessage(new ChangeLaneMessage(68, 100, 100));
//                    }
//                    v.sendMessage(new SetSpeedMessage(0, 12500));
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class LocalizationTransitionUpdateHandler implements MessageListener<LocalizationTransitionUpdateMessage> {

        private int transition = 0;

        @Override
        public void messageReceived(LocalizationTransitionUpdateMessage m) {
            transition = transition + 1;
            if (virtualId != -1) {
                if (!reverse) {
                    prevId = virtualId;
                    virtualId = (virtualId + 1) % map.size();
                } else {
                    prevId = virtualId;
                    virtualId = (virtualId + map.size() - 1) % map.size();
                }
            }
        }
    }

    private class LocalizationIntersectionUpdateHandler implements MessageListener<LocalizationIntersectionUpdateMessage> {

        private boolean exiting;

        @Override
        public void messageReceived(LocalizationIntersectionUpdateMessage m) {
            exiting = m.isExiting();
            if (map != null) {
                if (exiting) {
                    atIntersection = false;
                    intersection.clear();
                } else {
                    approachingIntersection = false;
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    v.sendMessage(new SetSpeedMessage(0, 12500));
                    intersection.add(v.getAddress());
                    atIntersection = true;
                }
            }
        }
    }

    private class PositionUpdater implements Runnable {

        public void run() {
            while (true) {
                try {
                    // sends a position update request to the vehicle
                    updatePosition();
                    Thread.sleep(50);
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
        private boolean stopped = false;
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
                    if (cars.contains(parsed)) {
                        // System.out.println(Arrays.toString(parsed) + " was received by " + id);
                        follow.follow(received);
                        emergStop.emergStop(received);
                    }
                    if (atIntersection) {
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
        //receiver.stopMC();
        //t.join();
    }

    private class MapScanner {

        private Vehicle vehicle;
        private Roadmap roadmap;
        private boolean initReverse;
        private ArrayList<Integer> pieceIDs;
        private ArrayList<Boolean> reverses;

        private LocalizationPositionUpdateMessage lastPosition;

        public MapScanner(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.roadmap = new Roadmap();
            this.pieceIDs = new ArrayList<Integer>();
            this.reverses = new ArrayList<Boolean>();
        }

        /**
         * Starts the scan by adding message listeners to the car. Updated from
         * original version, which would also move the car.
         *
         * @since 2016-12-13
         * @version 2020-05-10
         * @author adesso AG
         * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
         */
        public void startScanning() {
            vehicle.addMessageListener(
                    LocalizationPositionUpdateMessage.class,
                    (message) -> handlePositionUpdate(message)
            );

            vehicle.addMessageListener(
                    LocalizationTransitionUpdateMessage.class,
                    (message) -> handleTransitionUpdate(message)
            );
            //vehicle.sendMessage(new SetSpeedMessage(500, 12500));
        }

        /**
         * Stops the scan by removing the message listeners from the car.
         * Updated from original version, which would just stop the car.
         *
         * @since 2016-12-13
         * @version 2020-05-10
         * @author adesso AG
         * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
         */
        public void stopScanning() {
            vehicle.removeMessageListener(
                    LocalizationPositionUpdateMessage.class,
                    (message) -> handlePositionUpdate(message)
            );

            vehicle.removeMessageListener(
                    LocalizationTransitionUpdateMessage.class,
                    (message) -> handleTransitionUpdate(message)
            );
            //vehicle.sendMessage(new SetSpeedMessage(0, 12500));
        }

        public boolean isComplete() {
            return roadmap.isComplete();
        }

        public Roadmap getRoadmap() {
            return roadmap;
        }

        public boolean getInitReverse() {
            return initReverse;
        }

        public ArrayList<Integer> getPieceIDs() {
            return pieceIDs;
        }

        public ArrayList<Boolean> getReverses() {
            return reverses;
        }

        public void reset() {
            this.roadmap = new Roadmap();
            this.lastPosition = null;
        }

        private void handlePositionUpdate(LocalizationPositionUpdateMessage message) {
            lastPosition = message;
        }

        protected void handleTransitionUpdate(LocalizationTransitionUpdateMessage message) {
            if (!scanDone) {
                if (lastPosition != null) {
                    roadmap.add(
                            lastPosition.getRoadPieceId(),
                            lastPosition.getLocationId(),
                            lastPosition.isParsedReverse()
                    );

                    pieceIDs.add(lastPosition.getRoadPieceId());
                    reverses.add(lastPosition.isParsedReverse());
                    // System.out.println(v.getAdvertisement().getModel().name() + ": Added a piece... " + pieceIDs.get(pieceIDs.size() - 1));

                    if (lastPosition.getRoadPieceId() == 33 || lastPosition.getRoadPieceId() == 34) {
                        initReverse = lastPosition.isParsedReverse();
                    }

//                    if (roadmap.isComplete()) {
//                        this.stopScanning();
//                    }
                }
            }
        }
    }
}
