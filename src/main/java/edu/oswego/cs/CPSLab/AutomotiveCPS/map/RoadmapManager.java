/*
 * List of roadpiecces initialized for the cars
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.map;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import de.adesso.anki.roadmap.roadpieces.CurvedRoadpiece;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import de.adesso.anki.roadmap.roadpieces.IntersectionRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StartRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StraightRoadpiece;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author notebook
 */
public class RoadmapManager {

    private Roadmap map;
    private boolean rev;
    private ArrayList<Integer> pieceIDs;
    private ArrayList<Boolean> reverses;
    private List<Block> track;
    private List<Intersection> intersections;

    private int id;

    public RoadmapManager(Roadmap map, boolean rev, ArrayList<Integer> pieceIDs, ArrayList<Boolean> reverses) {
        this.map = map;
        this.rev = rev;
        this.pieceIDs = pieceIDs;
        this.reverses = reverses;
        track = new ArrayList<Block>();
        intersections = new ArrayList<Intersection>();
        generateTrack();
    }

    public Roadmap getMap() {
        return map;
    }
    
    public List<Block> getTrack(){
        return this.track;
    }

    private void generateTrack() {
        List<Roadpiece> pieces = map.toList();
        if (rev) {
            Collections.reverse(pieceIDs);
            Collections.rotate(pieceIDs, 1);
            Collections.reverse(reverses);
            Collections.rotate(reverses, 1);
            for (boolean r : reverses) {
                r = !r;
            }
        }
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getType().equals(IntersectionRoadpiece.class.getSimpleName())) {
                if (pieceIDs.get(i) != 10) {
                    pieceIDs.add(i, 10);
                    reverses.add(i, false);
                }
                track.add(new Block(10, false));
            } else {
                track.add(new Block(pieceIDs.get(i), reverses.get(i)));
            }
            track.get(i).assignPiece(pieces.get(i));
        }
        for (int x = 0; x < track.size(); x++) {
            track.get(x).assignSection(track.get(x).getPiece().getSectionByLocation(0, false));
        }
        track.get(0).getPiece().setPosition(Position.at(0, 0));
        for (int x = 0; x < track.size() - 1; x++) {
            track.get(x).getSection().connect(track.get(x + 1).getSection());
        }
        track.get(track.size() - 1).getSection().connect(track.get(0).getSection());

        generateIntersections();
        for (Intersection i : intersections) {
            System.out.println(i.toString());
        }
    }

    public void add(Block block) {
        track.add(block);
    }

    public Block get(int index) {
        return track.get(index);
    }

    public int indexOf(int pieceId) {
        for (Block block : track) {
            if (pieceId == block.getPieceId()) {
                return track.indexOf(block);
            }
        }
        return -1;
    }

    public int size() {
        return track.size();
    }

    public boolean member(Integer pieceId, Boolean reverse) {
        for (Block block : track) {
            if (pieceId == block.getPieceId() && reverse == block.getReverse()) {
                return true;
            }
        }
        return false;
    }

    public Section lookup(Integer pieceId, Boolean reverse) {
        for (Block block : track) {
            if (pieceId == block.getPieceId() && reverse == block.getReverse()) {
                return block.getSection();
            }
        }
        return null;
    }

    public boolean duplicate(Integer pieceId) {
        int counter = 0;
        for (Block block : track) {
            if (block.getPieceId() == pieceId) {
                counter++;
            }
        }
        if (counter == 1) {
            return false;
        } else {
            return true;
        }
    }

    public int getBySection(Section s) {
        for (Block block : track) {
            if (block.getSection() == s) {
                return track.indexOf(block);
            }
        }
        return -1;
    }

    public Block getByPiece(Roadpiece p) {
        for (Block block : track) {
            if (block.getPiece() == p) {
                return block;
            }
        }
        return null;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public boolean sameIntersection(int piece, int otherPiece) {
        for (Intersection i : intersections) {
            if (otherPiece == i.relatedPiece(piece)) {
                return true;
            }
        }
        return false;
    }

    private void generateIntersections() {
        // Look for all intersection pieces, record their piece number
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getPiece().getType().equals(IntersectionRoadpiece.class.getSimpleName())) {
                numbers.add(i);
            }
        }
        // Determine if they are related, create Intersection instance
        ArrayList<Integer> paired = new ArrayList<>();
        if (!numbers.isEmpty()) {
            for (int a : numbers) {
                for (int b : numbers) {
                    if (Math.abs(a - b) <= 1 || paired.contains(a) || paired.contains(b)) {     // if the two intersections are the same/consecutive
                        continue;
                    } else {
                        ArrayList<Block> between1 = between(a, b);
                        ArrayList<Block> between2 = between(b, a);
                        if (between1.size() <= between2.size()) {
                            if (determine(between1)) {
                                intersections.add(new Intersection(a, b));
                                paired.add(a);
                                paired.add(b);
                            } else {
                                continue;
                            }
                        } else {
                            if (determine(between2) == true) {
                                intersections.add(new Intersection(a, b));
                                paired.add(a);
                                paired.add(b);
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
        }
        // Create intersection instance for intersections that are not paired up
        numbers.removeAll(paired);
        System.out.println(numbers);
        for (int a : numbers) {
            intersections.add(new Intersection(a));
        }
        numbers.clear();
    }

    private ArrayList<Block> between(int a, int b) {
        ArrayList<Block> list = new ArrayList<>();
        if (a < b) {
            for (int i = a + 1; i < b; i++) {
                list.add(track.get(i));
            }
        } else {
            if (a != track.size() - 1) {
                for (int i = a + 1; i < track.size(); i++) {
                    list.add(track.get(i));
                }
            }
            for (int i = 0; i < b; i++) {
                list.add(track.get(i));
            }
        }
        return list;
    }

    private boolean determine(ArrayList<Block> between) {
        List<Block> blocks = new ArrayList<>();
        for (Block b : between) {
            if (b.getPiece().getType().equals(CurvedRoadpiece.class.getSimpleName())) {
                blocks.add(b);
            }
        }
        if (between.size() < 3 || blocks.size() < 3 || (blocks.size() % 2 == 0)) {
            return false;
        } else if (blocks.size() == 3 && between.size() == 3) {
            boolean direction = blocks.get(0).getReverse();
            for (Block b : blocks) {
                if (b.getReverse() != direction) {
                    return false;
                }
            }
            return true;
        } else {
            // With the idea of 2D grid
            int counterR = 1;
            int counterU = 0;
            String currentState = "R";
            for (Block b : between) {
                if (b.getPiece().getType().equals(CurvedRoadpiece.class.getSimpleName()) && b.getReverse()) {
                    currentState = setState(true, currentState);
                } else if (b.getPiece().getType().equals(CurvedRoadpiece.class.getSimpleName()) && !b.getReverse()) {
                    currentState = setState(false, currentState);
                } else if (b.getPiece().getType().equals(StartRoadpiece.class.getSimpleName())) {
                    continue;
                }
                counterR = setCounterR(currentState, counterR);
                counterU = setCounterU(currentState, counterU);
            }
            if (counterR == 0 && counterU == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    private int setCounterR(String currentState, int counter) {
        switch (currentState) {
            case "R":
                return (counter + 1);
            case "L":
                return (counter - 1);
            default:
                return counter;
        }
    }

    private int setCounterU(String currentState, int counter) {
        switch (currentState) {
            case "U":
                return (counter + 1);
            case "D":
                return (counter - 1);
            default:
                return counter;
        }
    }

    private String setState(boolean reverse, String currentState) {
        if (reverse) {     // Right curve -> RDLU
            switch (currentState) {
                case "R":
                    return "D";
                case "D":
                    return "L";
                case "L":
                    return "U";
                case "U":
                    return "R";
                default:
                    return currentState;
            }
        } else {     // Left curve -> RULD
            switch (currentState) {
                case "R":
                    return "U";
                case "U":
                    return "L";
                case "L":
                    return "D";
                case "D":
                    return "R";
                default:
                    return currentState;
            }
        }
    }

    private class Intersection {

        private int a;
        private int b;

        public Intersection(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public Intersection(int a) {
            this.a = a;
            this.b = -1;
        }

        public void assignIntersection(int b) {
            this.b = b;
        }

        public int relatedPiece(int piece) {
            if (piece == a) {
                return b;
            } else if (piece == b) {
                return a;
            } else {
                return -1;
            }
        }

        public String toString() {
            return "[" + a + ", " + b + "]";
        }

    }

}
