/*
 * Class for Behavior Emergency Stop
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import edu.oswego.cs.CPSLab.AutomotiveCPS.*;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.roadmap.Roadmap;
import de.adesso.anki.roadmap.roadpieces.Roadpiece;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notebook
 */
public class EmergencyStop extends Behavior {

    public EmergencyStop(CPSCar car) {
        super(car);
    }

    public void emergStop(String received) {
        String[] parsed = parseBroadcast(received);
        String rev = parsed[5];
        if (!Boolean.toString(reverse).equals(rev) && (virtualId != -1)) {
            if (carsAreClose(map, parsed)) {
                car.sendMessage(new SetSpeedMessage(0, -12500));
            }
        }
    }

    private boolean carsAreClose(RoadmapManager roadmap, String[] parsed) {
        int piece = Integer.parseInt(parsed[1]);
        int location = Integer.parseInt(parsed[2]);
        try {
            if (!reverse) {
                if (virtualId <= roadmap.size() - 3 && virtualId >= 0) {
                    if ((piece - virtualId) <= 2 && (piece - virtualId) >= 0) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }
                } else if (virtualId == roadmap.size() - 2) {
                    if (piece == roadmap.size() - 1 || piece == 0) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }
                } else if (virtualId == roadmap.size() - 1) {
                    if (piece == 0 || piece == 1) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }
                }
            } else {
                if (virtualId <= roadmap.size() - 1 && virtualId > 1) {
                    if ((virtualId - piece) <= 2 && (virtualId - piece) >= 0) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }
                } else if (virtualId == 1) {
                    if (piece == roadmap.size() - 1 || piece == 0) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }
                } else if (virtualId == 0) {
                    if (piece == roadmap.size() - 1 || piece == roadmap.size() - 2) {
                        if (Math.abs(locationId - location) <= 15) {
                            return true;
                        }
                    }

                }
            }
        } catch (NullPointerException e) {
            // e.printStackTrace();
        }
        return false;
    }

}
