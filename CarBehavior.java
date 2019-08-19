/*
 * Class that includes all behaviors
 */
package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import java.util.ArrayList;

/**
 *
 * @author notebook
 */
public class CarBehavior {
    
    private Vehicle v;
    private LocalizationPositionUpdateHandler lpuh;
    
    public CarBehavior(Vehicle v) {
        this.v = v;
        lpuh = new LocalizationPositionUpdateHandler();
    }
    
    public Vehicle getVehicle() {
        return v;
    }
    
    public LocalizationPositionUpdateHandler getLPUH() {
        return lpuh;
    }
    
    public void startRunning(int speed, int accel) {
        v.sendMessage(new SetSpeedMessage(speed, accel));
    }
    
    public void emergencyBrake(Roadmap<Integer, Boolean, Roadpiece, Section> roadmap, ArrayList<LocalizationPositionUpdateHandler> lpuhList) {
        
    }
    
    public void takeOver() {
        
    }
    
    /**
     * Handles the response from the vehicle from the
     * LocalizationPositionUpdateMessage. We need handler classes because
     * responses from the vehicles are asynchronous.
     */
    private static class LocalizationPositionUpdateHandler implements MessageListener<LocalizationPositionUpdateMessage> {

        private int locationId;
        private int currentPieceId;
        private int prevPieceId;
        private boolean reverse;
        private int speed;
        private float offset;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            locationId = m.getLocationId();
            prevPieceId = currentPieceId;
            currentPieceId = m.getRoadPieceId();
            reverse = m.isParsedReverse();
            speed = m.getSpeed();
            offset = m.getOffsetFromRoadCenter();
            // System.out.println("   Right now we are on " + piece.getClass().getSimpleName() + ". Location: " + locationId + ". ");
        }
    }
    
    private static boolean carsAreClose(Roadmap<Integer, Boolean, Roadpiece, Section> roadmap, LocalizationPositionUpdateHandler lpuh1, LocalizationPositionUpdateHandler lpuh2) {
        int pieceId1 = lpuh1.currentPieceId;
        int pieceId2 = lpuh2.currentPieceId;
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
