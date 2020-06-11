package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.BatteryLevelRequestMessage;
import de.adesso.anki.messages.BatteryLevelResponseMessage;
import de.adesso.anki.messages.CancelLaneChangeMessage;
import de.adesso.anki.messages.ChangeLaneMessage;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.PingRequestMessage;
import de.adesso.anki.messages.PingResponseMessage;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetOffsetFromRoadCenterMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.roadpieces.CurvedRoadpiece;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import de.adesso.anki.roadmap.roadpieces.StartRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StraightRoadpiece;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple test program to test a connection to your Anki 'Supercars' and
 * 'Supertrucks' using the NodeJS Bluetooth gateway. Simple follow the
 * installation instructions at http://github.com/adessoAG/anki-drive-java,
 * build this project, start the bluetooth gateway using ./gradlew server, and
 * run this class.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class AnkiTestProgram {

    static long pingReceivedAt;
    static long pingSentAt;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("192.168.1.100", 5000);
        System.out.print("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS! They are:");

            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                System.out.println("   " + v);
                System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
                System.out.println("      Model: " + v.getAdvertisement().getModel());
                System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
                System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
                System.out.println("      Address: " + v.getAddress());
                System.out.println("      Color: " + v.getColor());
                System.out.println("      charging? " + v.getAdvertisement().isCharging());
            }

            System.out.println("\nNow connecting to and doing stuff to your cars.\n\n");

            Vehicle v1 = vehicles.get(0);
            Vehicle v2 = vehicles.get(1);
            System.out.println("\nConnecting to " + v1 + " @ " + v1.getAddress());
            System.out.println("\nConnecting to " + v2 + " @ " + v2.getAddress());
            v1.connect();
            v2.connect();
            System.out.print("   Connected. Setting SDK mode...");   //always set the SDK mode FIRST!                
            v1.sendMessage(new SdkModeMessage());
            v2.sendMessage(new SdkModeMessage());
            System.out.println("   SDK Mode set.");

            System.out.println("   Sending asynchronous Battery Level Request. The Response will come in eventually.");
            //we have to set up a response handler first, in order to handle async responses
            BatteryLevelResponseHandler blrh = new BatteryLevelResponseHandler();
            //now we tell the car, who is listenening to the replies
            v1.addMessageListener(BatteryLevelResponseMessage.class, blrh);
            v2.addMessageListener(BatteryLevelResponseMessage.class, blrh);
            //now we can actually send it.
            v1.sendMessage(new BatteryLevelRequestMessage());
            v2.sendMessage(new BatteryLevelRequestMessage());

            System.out.println("   Sending Ping Request...");
            //again, some async set-up required...
            PingResponseHandler prh = new PingResponseHandler();
            v1.addMessageListener(PingResponseMessage.class, prh);
            v2.addMessageListener(PingResponseMessage.class, prh);
            long pingSentAt = System.currentTimeMillis();
            v1.sendMessage(new PingRequestMessage());
            v2.sendMessage(new PingRequestMessage());

            System.out.println("   Setting Speed...");
            SetSpeedMessage v1Speed = new SetSpeedMessage(300, 100);
            SetSpeedMessage v2Speed = new SetSpeedMessage(500, 100);

            System.out.println("   Getting the Roadmap...");   // Initialized
            Roadmap<Integer, Boolean, Roadpiece, Section> roadmap = new Roadmap<>();
            roadmap.add(new Block(33, false));
            roadmap.get(0).assignPiece(new StartRoadpiece());
            roadmap.add(new Block(18, false));
            roadmap.get(1).assignPiece(new CurvedRoadpiece());
            roadmap.add(new Block(17, false));
            roadmap.get(2).assignPiece(new CurvedRoadpiece());
            roadmap.add(new Block(48, false));
            roadmap.get(3).assignPiece(new StraightRoadpiece());
            roadmap.add(new Block(23, false));
            roadmap.get(4).assignPiece(new CurvedRoadpiece());
            roadmap.add(new Block(20, false));
            roadmap.get(5).assignPiece(new CurvedRoadpiece());
            roadmap.add(new Block(34, false));
            roadmap.get(6).assignPiece(new FinishRoadpiece());
            for (int x = 0; x < roadmap.size(); x++) {
                Section section = roadmap.get(x).c.getSectionByLocation(0, false);
                roadmap.get(x).assignSection(section);
            }
            roadmap.get(0).c.setPosition(Position.at(0, 0));
            for (int x = 0; x < roadmap.size() - 1; x++) {
                roadmap.get(x).d.connect(roadmap.get(x + 1).d);
            }
            roadmap.get(roadmap.size() - 1).d.connect(roadmap.get(0).d);

            System.out.println("   Safe Distance Test..."); //successful for same direction
            // Position Update
            LocalizationPositionUpdateHandler lpuh1 = new LocalizationPositionUpdateHandler();
            LocalizationPositionUpdateHandler lpuh2 = new LocalizationPositionUpdateHandler();
            v1.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh1);
            v2.addMessageListener(LocalizationPositionUpdateMessage.class, lpuh2);
            LocalizationPositionUpdateMessage lpum = new LocalizationPositionUpdateMessage();
            v1.sendMessage(lpum);
            v2.sendMessage(lpum);
            // Offset
            v1.sendMessage(new CancelLaneChangeMessage());
            v2.sendMessage(new CancelLaneChangeMessage());
            v1.sendMessage(new SetOffsetFromRoadCenterMessage(68));
            v2.sendMessage(new SetOffsetFromRoadCenterMessage(68));
            // Speed
            v1.sendMessage(v1Speed);
            v2.sendMessage(v2Speed);
            int currentPieceId1 = lpuh1.pieceId;
            int prevPieceId1 = 0;
            int currentPieceId2 = lpuh2.pieceId;
            int prevPieceId2 = 0;
            ChangeLaneMessage clm;
            while (true) {
                if (lpuh1.pieceId != currentPieceId1) {
                    prevPieceId1 = currentPieceId1;
                    currentPieceId1 = lpuh1.pieceId;
                }
                if (lpuh2.pieceId != currentPieceId2) {
                    prevPieceId2 = currentPieceId2;
                    currentPieceId2 = lpuh2.pieceId;
                }
                if (lpuh1.reverse == lpuh2.reverse) {
                    if (currentPieceId1 == currentPieceId2 && prevPieceId1 == prevPieceId2 && currentPieceId1 != 0) {
                        if (Math.abs(lpuh1.locationId - lpuh2.locationId) <= 3) {
                            if (lpuh1.reverse == false) {
                                if (lpuh1.locationId < lpuh2.locationId) {
                                    if (lpuh1.speed < lpuh2.speed) {
                                        continue;
                                    }
                                    v1.sendMessage(new SetSpeedMessage(lpuh2.speed - 1, 100));
                                    if (lpuh1.offset > 23) {
                                        float desiredOffset = lpuh1.offset - 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v1.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v1.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v1.sendMessage(v1Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v1.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false) != roadmap.lookup(lpuh2.pieceId, false).getNext()) {
                                        }
                                        v1.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                    } else {
                                        float desiredOffset = lpuh1.offset + 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v1.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {     // conditions need to be refined
                                            v1.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v1.sendMessage(v1Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v1.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false) != roadmap.lookup(lpuh2.pieceId, false).getNext()) {
                                        }
                                        v1.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                    }
                                } else {
                                    if (lpuh1.speed > lpuh2.speed) {
                                        continue;
                                    }
                                    v2.sendMessage(new SetSpeedMessage(lpuh1.speed - 1, 100));
                                    if (lpuh2.offset > 23) {
                                        float desiredOffset = lpuh2.offset - 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v2.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v2.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v2.sendMessage(v2Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v2.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false).getNext() != roadmap.lookup(lpuh2.pieceId, false)) {
                                        }
                                        v2.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                    } else {
                                        float desiredOffset = lpuh2.offset + 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v2.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v2.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v2.sendMessage(v2Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v2.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false).getNext() != roadmap.lookup(lpuh2.pieceId, false)) {
                                        }
                                        v2.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                    }
                                }
                            } else {
                                if (lpuh1.locationId > lpuh2.locationId) {
                                    if (lpuh1.speed < lpuh2.speed) {
                                        continue;
                                    }
                                    v1.sendMessage(new SetSpeedMessage(lpuh2.speed - 1, 100));
                                    if (lpuh1.offset > 23) {
                                        float desiredOffset = lpuh1.offset - 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v1.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v1.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v1.sendMessage(v1Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v1.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false).getNext() != roadmap.lookup(lpuh2.pieceId, false)) {
                                        }
                                        v1.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                    } else {
                                        float desiredOffset = lpuh1.offset + 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v1.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v1.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v1.sendMessage(v1Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v1.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false).getNext() != roadmap.lookup(lpuh2.pieceId, false)) {
                                        }
                                        v1.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                    }
                                } else {
                                    if (lpuh1.speed > lpuh2.speed) {
                                        continue;
                                    }
                                    v2.sendMessage(new SetSpeedMessage(lpuh1.speed - 1, 100));
                                    if (lpuh2.offset > 23) {
                                        float desiredOffset = lpuh2.offset - 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v2.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v2.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v2.sendMessage(v2Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v2.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false) != roadmap.lookup(lpuh2.pieceId, false).getNext()) {
                                        }
                                        v2.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
                                    } else {
                                        float desiredOffset = lpuh2.offset + 48;
                                        clm = new ChangeLaneMessage(desiredOffset, 100, 100);
                                        v2.sendMessage(clm);
                                        while (Math.abs(lpuh1.locationId - lpuh2.locationId) < 15) {
                                            v2.sendMessage(clm);
                                            Thread.sleep(50);
                                        }
                                        v2.sendMessage(v2Speed);
                                        if (lpuh1.pieceId != lpuh2.pieceId) {
                                            v2.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                            continue;
                                        }
                                        while (roadmap.lookup(lpuh1.pieceId, false) != roadmap.lookup(lpuh2.pieceId, false).getNext()) {
                                        }
                                        v2.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (carsAreClose(roadmap, lpuh1, lpuh2)) {
                        v1.sendMessage(new SetSpeedMessage(0, -12500));
                        v2.sendMessage(new SetSpeedMessage(0, -12500));
                    }
                }
                System.out.println("   v1 Current Piece: " + currentPieceId1 + ". Offset: " + lpuh1.offset + ". ");
                System.out.println("   v2 Current Piece: " + currentPieceId2 + ". Offset: " + lpuh2.offset + ". ");
                Thread.sleep(5);
            }
//            Thread.sleep(1000);
//            gs.sendMessage(new TurnMessage());
//            System.out.print("Sleeping for a few secs... ");
//            Thread.sleep(2000);
//            v1.disconnect();
//            System.out.println("disconnected from " + v1 + "\n");
        }
//        anki.close();
//        System.exit(0);
    }

    private static boolean carsAreClose(Roadmap<Integer, Boolean, Roadpiece, Section> roadmap, LocalizationPositionUpdateHandler lpuh1, LocalizationPositionUpdateHandler lpuh2) {
        int pieceId1 = lpuh1.pieceId;
        int pieceId2 = lpuh2.pieceId;
        boolean close1;
        boolean close2;
        if (pieceId1 != 0 && pieceId2 != 0) {
            if (pieceId1 == 33 || pieceId1 == 34) {
                close1 = roadmap.lookup(pieceId1, false).getNext() == roadmap.lookup(pieceId2, false).getPrev();
            } else {
                close1 = roadmap.lookup(pieceId1, false).getNext() == roadmap.lookup(pieceId2, false);
            }
            if (pieceId2 == 33 || pieceId2 == 34) {
                close2 = roadmap.lookup(pieceId2, false).getNext() == roadmap.lookup(pieceId1, false).getPrev();
            } else {
                close2 = roadmap.lookup(pieceId2, false).getNext() == roadmap.lookup(pieceId1, false);
            }
            if (lpuh1.reverse == false) {
                if (close1) {
                    if (Math.abs(lpuh1.locationId - lpuh2.locationId) <= 15) {
                        return true;
                    }
                }
            } else {
                if (close2) {
                    if (Math.abs(lpuh1.locationId - lpuh2.locationId) <= 15) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles the response from the vehicle from the
     * BatteryLevelRequestMessage. We need handler classes because responses
     * from the vehicles are asynchronous.
     */
    private static class BatteryLevelResponseHandler implements MessageListener<BatteryLevelResponseMessage> {

        @Override
        public void messageReceived(BatteryLevelResponseMessage m) {
            System.out.println("   Battery Level is: " + m.getBatteryLevel() + " mV");
        }
    }

    /**
     * Handles the response from the vehicle from the PingRequestMessage. We
     * need handler classes because responses from the vehicles are
     * asynchronous.
     */
    private static class PingResponseHandler implements MessageListener<PingResponseMessage> {

        @Override
        public void messageReceived(PingResponseMessage m) {
            long pingReceivedAt = System.currentTimeMillis();
            System.out.println("   Ping response received. Roundtrip: " + (pingReceivedAt - pingSentAt) + " msec.");
        }
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
            // System.out.println("   Right now we are on " + piece.getClass().getSimpleName() + ". Location: " + locationId + ". ");
        }
    }

    /**
     *
     * @author notebook
     */
    private static class Roadmap<Integer, Boolean, Roadpiece, Section> {

        private ArrayList<Block<Integer, Boolean, Roadpiece, Section>> roadmap;

        public Roadmap() {
            roadmap = new ArrayList<>();
        }

        public void add(Block<Integer, Boolean, Roadpiece, Section> block) {
            roadmap.add(block);
        }

        public Block<Integer, Boolean, Roadpiece, Section> get(int index) {
            return roadmap.get(index);
        }

        public int size() {
            return roadmap.size();
        }

        public boolean member(Integer pieceId, Boolean reverse) {
            for (Block<Integer, Boolean, Roadpiece, Section> block : roadmap) {
                if (pieceId == block.a && reverse == block.b) {
                    return true;
                }
            }
            return false;
        }

        public Section lookup(Integer pieceId, Boolean reverse) {
            for (Block<Integer, Boolean, Roadpiece, Section> block : roadmap) {
                if (pieceId == block.a && reverse == block.b) {
                    return block.d;
                }
            }
            return null;
        }
    }

    /**
     *
     * @author notebook
     */
    private static class Block<Integer, Boolean, Roadpiece, Section> {

        private Integer a;
        private Boolean b;
        private Roadpiece c;
        private Section d;

        public Block(Integer a, Boolean b) {
            this.a = a;
            this.b = b;
        }

        public boolean isCorrespondingPiece(Integer a, Boolean b) {
            return this.a == a && this.b == b;
        }

        public void assignPiece(Roadpiece c) {
            this.c = c;
        }

        public void assignSection(Section d) {
            this.d = d;
        }

        public String toString() {
            return "(" + a + "," + b + "," + c + "," + d + ")";
        }
    }

}
