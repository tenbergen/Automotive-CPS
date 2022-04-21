package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

public class ChangeSpeed extends Behavior {

    public ChangeSpeed(CPSCar car) {
        super(car);
    }

    public void changeSpeed(int speed) {
        SetSpeedMessage speedMessage = new SetSpeedMessage(speed, 300);
        car.getVehicle().sendMessage(speedMessage);
    }

    public void changeSpeed(int speed, int acceleration) {
        SetSpeedMessage speedMessage = new SetSpeedMessage(speed, acceleration);
        car.getVehicle().sendMessage(speedMessage);
    }
}
