/*
 * Testing the CPSCar Class
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author notebook
 */
public class CPSTestProgram {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector("192.168.1.101", 5000);
        System.out.println("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles(); // Add the CPSCar class to AnkiConnector?
        List<CPSCar> cars = new ArrayList<>();
        // int random = (int) (Math.random() * 200);

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(vehicles.toString());
            for (Vehicle v : vehicles) {
                CPSCar c = new CPSCar(v);
                cars.add(c);
            }

            // Roadmap Manager(s)
            List<RoadmapManager> managers = new ArrayList<>();

//            Scanner kb = new Scanner(System.in);
            while (true) {
//                if (kb.nextLine().equalsIgnoreCase("q")) {
//                    break;
//                }                
                // If scan is done, get notified
                for (CPSCar c : cars) {
                    if (c.scanDone() && c.getManager() == null) {
                        for (RoadmapManager rm : managers) {
                            if (c.getPieceIDs().equals(rm.getPieceIDs()) && c.getReverses().equals(rm.getReverses())) {
                                System.out.println("Same manager...");
                                c.setRoadmapMannager(rm);
                            }
                        }
                        if (c.getManager() == null) {
                            System.out.println("New manager...");
                            RoadmapManager rm = new RoadmapManager(c.getMap(), c.getReverse(), c.getPieceIDs(), c.getReverses());
                            managers.add(rm);
                            rm.setID(managers.indexOf(rm));
                            c.setRoadmapMannager(rm);
                        }
                    }
                }
                Thread.sleep(100);
            }

        }
//        for (CPSCar c : cars) {
//            c.disconnect();
//        }
    }

    private static void adjust(boolean reverse, ArrayList<Integer> carIDs, ArrayList<Boolean> carRevs) {
        if (reverse) {
            Collections.reverse(carIDs);
            Collections.reverse(carRevs);
            for (int i = 0; i < carIDs.size(); i++) {
                if (carIDs.get(i) != 10) {
                    carRevs.set(i, !carRevs.get(i));
                }
            }
        }
        int distance = carIDs.size() - 1 - carIDs.indexOf(34);
        Collections.rotate(carIDs, distance);
        Collections.rotate(carRevs, distance);
    }
}
