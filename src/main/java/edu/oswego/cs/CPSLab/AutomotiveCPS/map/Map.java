/*
 * List of roadpiecces initialized for the cars
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.map;

import de.adesso.anki.roadmap.Position;
import de.adesso.anki.roadmap.Section;
import de.adesso.anki.roadmap.roadpieces.CurvedRoadpiece;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StartRoadpiece;
import de.adesso.anki.roadmap.roadpieces.StraightRoadpiece;

/**
 *
 * @author notebook
 */
public class Map {

    private Roadmap map;

    public Map() {
        map = new Roadmap();
    }
    
    public Roadmap getMap() {
        return map;
    }

    public void map1() {
        map.add(new Block(33, false));
        map.get(0).assignPiece(new StartRoadpiece());
        map.add(new Block(39, false));
        map.get(1).assignPiece(new StraightRoadpiece());
        map.add(new Block(18, false));
        map.get(2).assignPiece(new CurvedRoadpiece());
        map.add(new Block(57, false));
        map.get(3).assignPiece(new StraightRoadpiece());
        map.add(new Block(17, false));
        map.get(4).assignPiece(new CurvedRoadpiece());
        map.add(new Block(36, false));
        map.get(5).assignPiece(new StraightRoadpiece());
        map.add(new Block(48, false));
        map.get(6).assignPiece(new StraightRoadpiece());
        map.add(new Block(23, false));
        map.get(7).assignPiece(new CurvedRoadpiece());
        map.add(new Block(39, false));
        map.get(8).assignPiece(new StraightRoadpiece());
        map.add(new Block(20, false));
        map.get(9).assignPiece(new CurvedRoadpiece());
        map.add(new Block(34, false));
        map.get(10).assignPiece(new FinishRoadpiece());
        for (int x = 0; x < map.size(); x++) {
            map.get(x).assignSection(map.get(x).getPiece().getSectionByLocation(0, false));
        }
        map.get(0).getPiece().setPosition(Position.at(0, 0));
        for (int x = 0; x < map.size() - 1; x++) {
            map.get(x).getSection().connect(map.get(x + 1).getSection());
        }
        map.get(map.size() - 1).getSection().connect(map.get(0).getSection());
    }

    public void map2() {
        map.add(new Block(33, false));
        map.get(0).assignPiece(new StartRoadpiece());
        map.add(new Block(18, false));
        map.get(1).assignPiece(new CurvedRoadpiece());
        map.add(new Block(17, false));
        map.get(2).assignPiece(new CurvedRoadpiece());
        map.add(new Block(36, false));
        map.get(3).assignPiece(new StraightRoadpiece());
        map.add(new Block(17, false));
        map.get(4).assignPiece(new CurvedRoadpiece());
        map.add(new Block(18, false));
        map.get(5).assignPiece(new CurvedRoadpiece());
        map.add(new Block(34, false));
        map.get(6).assignPiece(new FinishRoadpiece());
        for (int x = 0; x < map.size(); x++) {
            map.get(x).assignSection(map.get(x).getPiece().getSectionByLocation(0, false));
        }
        map.get(0).getPiece().setPosition(Position.at(0, 0));
        for (int x = 0; x < map.size() - 1; x++) {
            map.get(x).getSection().connect(map.get(x + 1).getSection());
        }
        map.get(map.size() - 1).getSection().connect(map.get(0).getSection());
    }
    
    public int size() {
        return map.size();
    }
    
    public Block get(int index) {
        return map.get(index);
    }
    
    public Section lookup(Integer pieceId, Boolean reverse) {
        Section s = map.lookup(pieceId, reverse);
        return s;
    }
    
    public boolean duplicate(Integer pieceId) {
            return map.duplicate(pieceId);
        }
    
    public int indexOf(int pieceId) {
        return map.indexOf(pieceId);
    }
    
    public int getBySection(Section s) {
        return map.getBySection(s);
    }
    
}
