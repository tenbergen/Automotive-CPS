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
        
        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");
            
        } else {
            System.out.println(vehicles.toString());
            for (Vehicle v : vehicles) {
                CPSCar c = new CPSCar(v);
                cars.add(c);
                Thread.sleep(100);
            }

            // Roadmap Manager(s)
            List<RoadmapManager> managers = new ArrayList<>();
            
            while (true) {
                // If scan is done, get notified
                for (CPSCar c : cars) {
                    if (c.scanDone() && c.getManager() == null) {
                        for (RoadmapManager rm : managers) {
//                            System.out.println(c.getMap().toString());
//                            System.out.println(rm.getMap().toString());
                            if (c.getMap().equals(rm.getMap())) {
                                System.out.println("Same manager...");
                                c.setRoadmapMannager(rm);
                            }
                        }
                        if (c.getManager() == null) {
                            System.out.println("New manager...");
                            System.out.println(c.getMap());
                            RoadmapManager rm = new RoadmapManager(c.getMap(), c.getPieceIDs(), c.getReverses());
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
}
