/*
 * Abstract Behavior Class
 */
package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.roadmap.Section;

/**
 *
 * @author notebook
 */
public abstract class Behavior {

    protected CPSCar car;
    protected Map map;
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
