/*
 * Abstract Behavior Class
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.Section;

/**
 *
 * @author notebook
 */
public abstract class Behavior {

    protected CPSCar car;
    protected RoadmapManager map;
    protected int locationId;
    protected int pieceId;
    protected int virtualId;
    protected boolean reverse;
    protected int speed;
    protected float offset;
    protected int prevLocationId;
    protected int prevId;
    protected Section section;

    public Behavior(CPSCar car) {
        this.car = car;
        this.map = car.getMap();
    }

    public void updateInfo() {
        locationId = car.getLocationId();
        pieceId = car.getPieceId();
        virtualId = car.getVirtualId();
        reverse = car.getReverse();
        speed = car.getSpeed();
        offset = car.getOffset();
        prevLocationId = car.getPrevLocationId();
        prevId = car.getPrevId();
        section = car.getSection();
    }

    public String[] parseBroadcast(String received) {
        String[] parsed = received.split(" ");
        return parsed;
    }

}
