/*
 * Class for Behavior Follow
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Section;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notebook
 */
public class Follow extends Behavior {

    public Follow(CPSCar car) {
        super(car);
    }

    public void follow(String received) {
        String[] parsed = parseBroadcast(received);
        int piece = Integer.parseInt(parsed[1]);
        int location = Integer.parseInt(parsed[2]);
        int prevPiece = Integer.parseInt(parsed[3]);
        boolean rev = Boolean.getBoolean(parsed[5]);
        int speed = Integer.parseInt(parsed[6]);
        float offset = Float.parseFloat(parsed[7]);
        if (virtualId == piece && prevId == prevPiece) {
            if (reverse == rev) {
                if (reverse) {
                    if (this.locationId - location <= 3 && this.locationId - location > 0 && this.speed > speed) {
                        car.sendMessage(new SetSpeedMessage(speed - 5, 100));
                        List<String[]> carList = car.getCarList();
                        carList.remove(received);
                        List<String[]> otherCars = carList;
                        boolean carBlocking = false;
                        for (String[] broadcast : otherCars) {
                            float otherOffset = Float.parseFloat(broadcast[7]);
                            if (otherOffset >= this.offset - 48 && otherOffset <= this.offset + 48) {
                                carBlocking = true;
                            }
                        }
                        if (!carBlocking) {
                            car.ableToOvertake(true);
                        }
                    }
                } else {
                    if (location - this.locationId <= 3 && location - this.locationId > 0 && this.speed > speed) {
                        car.sendMessage(new SetSpeedMessage(speed - 5, 100));
                        List<String[]> carList = car.getCarList();
                        carList.remove(received);
                        List<String[]> otherCars = carList;
                        boolean carBlocking = false;
                        for (String[] broadcast : otherCars) {
                            float otherOffset = Float.parseFloat(broadcast[7]);
                            if (otherOffset >= this.offset - 48 && otherOffset <= this.offset + 48) {
                                carBlocking = true;
                            }
                        }
                        if (!carBlocking) {
                            car.ableToOvertake(true);
                        }
                    }
                }
            }
        }
    }
}
