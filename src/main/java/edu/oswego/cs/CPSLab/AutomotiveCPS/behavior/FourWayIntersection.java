/*
 * Class for car's behavior at intersection
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notebook
 */
public class FourWayIntersection extends Behavior {

    public FourWayIntersection(CPSCar car) {
        super(car);
    }

    public void run() {
        Queue<String> queue = car.getIntersectionList();
        String name = car.getAddress();
        if (queue.element() == name) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CPSCar.class.getName()).log(Level.SEVERE, null, ex);
            }
            car.sendMessage(new SetSpeedMessage(400, 100));
        }
    }

}
