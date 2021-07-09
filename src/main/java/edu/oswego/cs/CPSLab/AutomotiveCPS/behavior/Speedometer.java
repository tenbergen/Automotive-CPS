package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadPieceLength;

/**
 * @author Gregory Maldonado
 * @since 06-10-2021
 */
public class Speedometer extends Behavior {
    /**
     *  Speedometer is a behavior that calculates the physical velocity of the vehicle.
     *  The calculateVelocity() method should be called after every track piece transition
     *  the vehicle traverses.
     */

    private double velocity;
    private long startTime;
    private long endTime;
    private double elapsedTime;
    private int lastTrackTraversed = -1;
    private int startingLane = 0;
    private double runningVelocitySum = 0;
    private int velocityUpdates = 0;

    public Speedometer(CPSCar v) {
        super(v);
    }

    /**
     * This method finds the length of the track piece traversed based on the pieceID inherited by the
     * behavior class
     * @return the distance of the track piece the vehicle just traversed.
     */
    private double getTrackLength() {
        // checks if the vehicle is not on the starting line
        if (car.getPieceId() != lastTrackTraversed) {
            lastTrackTraversed = car.getPieceId();
            if (lastTrackTraversed != FinishRoadpiece.ROADPIECE_IDS[0] || lastTrackTraversed != 0) {
                try {
                    Roadpiece roadpiece = Roadpiece.createFromId(lastTrackTraversed);
                    assert roadpiece != null;
                    if (roadpiece.getType().equals("StraightRoadpiece") || roadpiece.getType().equals("IntersectionRoadpiece")) {
                        return RoadPieceLength.STRAIGHT.cmLength();
                    }
                    else if (roadpiece.getType().equals("CurvedRoadpiece")) {
                        switch (car.getLane()) {
                            case 4:
                                return RoadPieceLength.FOURTHLANECURVE.cmLength();
                            case 3:
                                return RoadPieceLength.THIRDLANECURVE.cmLength();
                            case 2:
                                return RoadPieceLength.SECONDLANECURVE.cmLength();
                            default:
                                return RoadPieceLength.FIRSTLANECURVE.cmLength();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        // returns zero if the whole track piece wasn't traversed i.e. the car starting at the starting line
        return 0.0;
    }

    /**
     * Gets the lane of the vehicle
     * TODO: Get continuous lane updates, this gets the starting lane
     */
    public void getLane() {
        this.updateInfo();
        try {
            // Checks if the current piece is a starting road piece and then gets the locationID on the track
            if (Roadpiece.createFromId(pieceId).getType().equals("StartRoadpiece")) {
                if (locationId >= 0 && locationId < 5)        startingLane = 1;
                else if (locationId >= 5 && locationId < 10)  startingLane = 2;
                else if (locationId >= 10 && locationId < 15) startingLane = 3;
                else if (locationId >= 15 && locationId < 20) startingLane = 4;
                car.setLane(startingLane);
            }
            // If the vehicle crosses a intersection piece, flip the lane
            if (Roadpiece.createFromId(pieceId).getType().equals("IntersectionRoadpiece") && this.car.getPieceId() != lastTrackTraversed ) {
                lastTrackTraversed = this.car.getPieceId();
                if (this.car.getLane() == 1) this.car.setLane(4);
                else if (this.car.getLane() == 2) this.car.setLane(3);
                else if (this.car.getLane() == 3) this.car.setLane(2);
                else if (this.car.getLane() == 4) this.car.setLane(1);
                System.out.println("Lane: " + this.car.getLane());
            }
        } catch (Exception ignored) {}

    }

    /**
     * Timer for the traversal of the track. If the vehicle transitions to a new piece a new timer is started
     */
    private void timer() {
        updateInfo();
        if (pieceId != FinishRoadpiece.ROADPIECE_IDS[0] || pieceId != 0) {
            endTime = System.nanoTime();
            elapsedTime = (endTime - startTime) / 1e9;
        }
        startTime = System.nanoTime();
    }

    /**
     * Calculates physical Velocity if both quanities aren't zero
     */
    public void calculateVelocity() {
        this.getLane();
        timer();
        double distance = getTrackLength();
        if (elapsedTime != 0.0 && distance != 0.0) {
            this.velocity = distance / elapsedTime;
            runningVelocitySum += this.velocity;
            velocityUpdates ++;
        }

    }

    public double getVelocity() { return this.velocity; }

    public double getAverageVelocity() {
        return velocityUpdates > 0 ? ( runningVelocitySum / velocityUpdates ) : 0;
    }
}
