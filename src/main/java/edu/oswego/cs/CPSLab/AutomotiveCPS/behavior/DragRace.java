package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.MessageListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.*;
import de.adesso.anki.roadmap.roadpieces.FinishRoadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

public class DragRace extends Behavior {

    private Vehicle v;
    private long interval;
    private double raceTime;
    private Status status;

    private LightsPatternMessage.LightConfig brakeLightsOnConfig, brakeLightsOffConfig;
    private LightsPatternMessage brakeLightsOn, brakeLightsOff;
    private FinishLineDetector fld;
    private PingResponseHandler prh;

    public DragRace(CPSCar car) {
        super(car);
        brakeLightsOnConfig = new LightsPatternMessage.LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STEADY, 100, 0, 0);
        brakeLightsOn = new LightsPatternMessage();
        brakeLightsOn.add(brakeLightsOnConfig);

        brakeLightsOffConfig = new LightsPatternMessage.LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STEADY, 0, 0, 0);
        brakeLightsOff = new LightsPatternMessage();
        brakeLightsOff.add(brakeLightsOffConfig);

        fld = new FinishLineDetector();
        prh = new PingResponseHandler();
        prestage();
    }

    public void prestage() {
        v.connect();
        v.sendMessage(new SdkModeMessage());

        raceTime = 0;
        interval = 10;

        v.addMessageListener(LocalizationPositionUpdateMessage.class, fld);
        v.sendMessage(new LocalizationPositionUpdateMessage());
        v.addMessageListener(PingResponseMessage.class, prh);

        this.status = Status.PRESTAGED;
    }

    public void stage() {
        v.sendMessage(new PingRequestMessage());
        prh.pingSentAt = System.currentTimeMillis();
        long timeout  = 10000;
        while (!prh.pingReceived && timeout > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                return;
            }
            timeout -= interval;
        }

        interval = prh.roundTrip;

        v.sendMessage(brakeLightsOn);

        this.status = Status.READY;
    }

    public void run() {
        v.sendMessage(brakeLightsOff);
        this.status = Status.RACING;
        long started = System.currentTimeMillis();

        try {
            Thread.sleep(interval); // reaction time delay
        } catch (InterruptedException ie) {

        }
        v.sendMessage(new SetSpeedMessage(1500, 1000)); //race

        while (!fld.stop ) {    //stop at finish line
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) { }
        }

        raceTime =  (System.currentTimeMillis() - started) / 1000.0;

        v.sendMessage(new SetSpeedMessage(0, 10500));
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ie) { }
        v.sendMessage(brakeLightsOn);

        try {
            Thread.sleep(2500); // finish
        } catch (InterruptedException ie) { }


        v.disconnect();
        this.status = Status.FINISHED;
    }

    /**
     * Simulates a reaction time delay.
     */
    private class PingResponseHandler implements MessageListener<PingResponseMessage> {
        private boolean pingReceived = false;
        private long pingSentAt = System.currentTimeMillis();
        private long roundTrip = -1;

        @Override
        public void messageReceived(PingResponseMessage m) {
            this.pingReceived = true;
            this.roundTrip = System.currentTimeMillis() - pingSentAt;
        }
    }

    /**
     * Looks for the finish line. (Actually the start line).
     */
    private class FinishLineDetector implements MessageListener<LocalizationPositionUpdateMessage> {
        private int finishLineId = FinishRoadpiece.ROADPIECE_IDS[0];//StartRoadpiece.ROADPIECE_IDS[0];
        private boolean stop = false;

        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (m.getRoadPieceId() == finishLineId) this.stop = true;
        }
    }

    public enum Status {
        FINISHED,
        ERROR,
        RACING,
        READY,
        PRESTAGED
    }
}
