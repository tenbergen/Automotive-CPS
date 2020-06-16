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
import java.util.List;

/**
 *
 * @author notebook
 */
public class Map {

    private Roadmap map;
    private boolean rev;
    private ArrayList<Integer> pieceIDs;
    private List<Block> track;

    public Map(Roadmap map, boolean rev, ArrayList<Integer> pieceIDs) {
        map.normalize();
        this.map = map;
        this.rev = rev;
        this.pieceIDs = pieceIDs;
        track = new ArrayList<Block>();
    }

    public Roadmap getMap() {
        return map;
    }

    public void generateTrack() {
        List<Roadpiece> pieces = map.toList();
        if (rev) {
            track.add(new Block(pieceIDs.get(0), false));
            track.get(0).assignPiece(pieces.get(0));
            for (int i = pieces.size() - 1; i > 0; i--) {
                track.add(new Block(pieceIDs.get(i), false));
                track.get(i).assignPiece(pieces.get(i));
            }
        } else {
            for (int i = 0; i < pieces.size(); i++) {
                track.add(new Block(pieceIDs.get(i), false));
                track.get(i).assignPiece(pieces.get(i));
            }
        }
        for (int x = 0; x < track.size(); x++) {
            track.get(x).assignSection(track.get(x).getPiece().getSectionByLocation(0, false));
        }
        track.get(0).getPiece().setPosition(Position.at(0, 0));
        for (int x = 0; x < track.size() - 1; x++) {
            track.get(x).getSection().connect(track.get(x + 1).getSection());
        }
        track.get(track.size() - 1).getSection().connect(track.get(0).getSection());
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

}
