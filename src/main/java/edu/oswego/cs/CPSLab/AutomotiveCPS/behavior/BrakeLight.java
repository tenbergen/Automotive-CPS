/*
 * Class for Behavior Brake Light
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import de.adesso.anki.messages.LightsPatternMessage;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;

/**
 *
 * @author notebook
 */
public class BrakeLight extends Behavior {

    private LightConfig configOn;
    private LightConfig configOff;
    private LightsPatternMessage lpmOn;
    private LightsPatternMessage lpmOff;
    private boolean lightOn;

    public BrakeLight(CPSCar car) {
        super(car);

        configOn = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.FADE, 0, 10, 0);
        lpmOn = new LightsPatternMessage();
        lpmOn.add(configOn);

        configOff = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.FADE, 10, 0, 0);
        lpmOff = new LightsPatternMessage();
        lpmOff.add(configOff);

        lightOn = false;
    }

    public void run() {
        if (lightOn) {
            car.sendMessage(lpmOff);
        } else {
            car.sendMessage(lpmOn);
        }
    }

}
