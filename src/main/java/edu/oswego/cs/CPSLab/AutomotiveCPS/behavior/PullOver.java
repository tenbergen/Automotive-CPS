/*
 * Class for Behavior Pull Over
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.ChangeLaneMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notebook
 */
public class PullOver extends Behavior {

    public PullOver(CPSCar car) {
        super(car);
    }

    public void run() {
        if (this.reverse) {
            car.sendMessage(new ChangeLaneMessage(-68, 100, 100));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PullOver.class.getName()).log(Level.SEVERE, null, ex);
            }
            car.sendMessage(new SetSpeedMessage(0, 12500));
        } else {
            car.sendMessage(new ChangeLaneMessage(68, 100, 100));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PullOver.class.getName()).log(Level.SEVERE, null, ex);
            }
            car.sendMessage(new SetSpeedMessage(0, 12500));
        }
    }

}
