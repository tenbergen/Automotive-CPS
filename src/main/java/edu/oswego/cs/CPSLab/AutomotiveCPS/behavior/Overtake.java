/*
 * Class for Behavior Overtake
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import de.adesso.anki.messages.ChangeLaneMessage;
import de.adesso.anki.messages.SetSpeedMessage;

/**
 *
 * @author notebook
 */
public class Overtake extends Behavior {

    public Overtake(CPSCar car) {
        super(car);
    }

    public void overtake(String received) throws InterruptedException {
        String[] parsed = parseBroadcast(received);
        int piece = Integer.parseInt(parsed[1]);
        int location = Integer.parseInt(parsed[2]);
        int prevPiece = Integer.parseInt(parsed[3]);
        boolean rev = Boolean.getBoolean(parsed[5]);
        int speed = Integer.parseInt(parsed[6]);
        float offset = Float.parseFloat(parsed[7]);
        if (reverse) {
            if (this.offset > 23) {
                float desiredOffset = this.offset - 48;
                car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                while (Math.abs(this.locationId - location) < 15) {
                    car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                    Thread.sleep(50);
                }
                car.sendMessage(new SetSpeedMessage(this.speed + 100, 100));
                while ((piece - this.virtualId) < 1) {
                }
                car.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
            } else {
                float desiredOffset = this.offset + 48;
                car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                while (Math.abs(this.locationId - location) < 15) {     // conditions need to be refined
                    car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                    Thread.sleep(50);
                }
                car.sendMessage(new SetSpeedMessage(this.speed + 100, 100));
                while ((piece - this.virtualId) < 1) {
                }
                car.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
            }
        } else {
            if (this.offset > 23) {
                float desiredOffset = this.offset - 48;
                car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                while (Math.abs(this.locationId - location) < 15) {
                    car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                    Thread.sleep(50);
                }
                car.sendMessage(new SetSpeedMessage(this.speed + 100, 100));
                while ((this.virtualId - piece) < 1) {
                }
                car.sendMessage(new ChangeLaneMessage(desiredOffset + 48, 100, 100));
            } else {
                float desiredOffset = this.offset + 48;
                car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                while (Math.abs(this.locationId - location) < 15) {
                    car.sendMessage(new ChangeLaneMessage(desiredOffset, 100, 100));
                    Thread.sleep(50);
                }
                car.sendMessage(new SetSpeedMessage(this.speed + 100, 100));
                while ((this.virtualId - piece) < 1) {
                }
                car.sendMessage(new ChangeLaneMessage(desiredOffset - 48, 100, 100));
            }
        }
    }
}
