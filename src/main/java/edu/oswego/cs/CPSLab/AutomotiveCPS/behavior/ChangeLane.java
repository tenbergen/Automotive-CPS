/*
 * Class for Behavior ChangeLane
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import de.adesso.anki.messages.ChangeLaneMessage;

/**
 *
 * @author notebook
 */
public class ChangeLane extends Behavior {

    public ChangeLane(CPSCar car) {
        super(car);
    }

    public void changeLane() {
        if (this.offset > 23 || (this.offset < 0 && this.offset > -30)) {
            car.sendMessage(new ChangeLaneMessage(this.offset - 48, 100, 100));
        } else {
            car.sendMessage(new ChangeLaneMessage(this.offset + 48, 100, 100));
        }
    }
}
