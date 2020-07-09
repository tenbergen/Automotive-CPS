/*
 * Class for Behavior 4-way Hazard Light
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import de.adesso.anki.messages.LightsPatternMessage;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;

/**
 *
 * @author notebook
 */
public class FourWayHazardLight extends Behavior {

    private LightConfig configOn1;
    private LightConfig configOn2;
    private LightConfig configOff1;
    private LightConfig configOff2;
    private LightsPatternMessage lpmOn;
    private LightsPatternMessage lpmOff;
    private boolean lightOn;

    public FourWayHazardLight(CPSCar car) {
        super(car);

        configOn1 = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
        configOn2 = new LightConfig(LightsPatternMessage.LightChannel.FRONT_RED, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
        lpmOn = new LightsPatternMessage();
        lpmOn.add(configOn1);
        lpmOn.add(configOn2);

        configOff1 = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STEADY, 0, 0, 0);
        configOff2 = new LightConfig(LightsPatternMessage.LightChannel.FRONT_RED, LightsPatternMessage.LightEffect.STEADY, 0, 0, 0);
        lpmOff = new LightsPatternMessage();
        lpmOff.add(configOff1);
        lpmOff.add(configOff2);

        lightOn = false;
    }

    public void run() {
        if (lightOn) {
            car.sendMessage(lpmOff);
            lightOn = false;
        } else {
            car.sendMessage(lpmOn);
            lightOn = true;
        }
    }

}
