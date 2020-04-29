/*
 * Class for Behavior ChangeLane
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.ChangeLaneMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

/**
 *
 * @author notebook
 */
public class ChangeLane extends Behavior {

    public ChangeLane(CPSCar car) {
        super(car);
    }

    public void changeLane(String received) {
        if (this.offset > 23) {
            car.sendMessage(new ChangeLaneMessage(this.offset - 48, 100, 100));
        } else {
            car.sendMessage(new ChangeLaneMessage(this.offset + 48, 100, 100));
        }
    }
}