/*
 * Class for Behavior U-Turn
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.TurnMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

/**
 *
 * @author notebook
 */
public class UTurn extends Behavior{
    
    public UTurn(CPSCar car) {
        super(car);
    }
    
    public void run() {
        car.sendMessage(new TurnMessage(3,1));
    }
    
}
