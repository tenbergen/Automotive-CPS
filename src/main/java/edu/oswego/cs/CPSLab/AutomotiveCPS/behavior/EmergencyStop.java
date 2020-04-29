/*
 * Class for Behavior EmergencyStop
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.behavior;

import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Map;

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

    private boolean carsAreClose(Map roadmap, String[] parsed) {
        int piece = Integer.parseInt(parsed[1]);
        int location = Integer.parseInt(parsed[2]);
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
        return false;
    }

}
