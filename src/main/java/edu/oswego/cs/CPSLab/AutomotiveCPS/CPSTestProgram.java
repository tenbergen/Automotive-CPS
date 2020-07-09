/*
 * Testing the CPSCar Class
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import java.io.IOException;
import java.util.ArrayList;
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
            CPSCar cps1 = new CPSCar(vehicles.get(0));
            CPSCar cps2 = new CPSCar(vehicles.get(1));
            cars.add(cps1);
            cars.add(cps2);
            boolean quit = false;
            Scanner kb = new Scanner(System.in);
            while (!quit) {
                if (kb.nextLine().equalsIgnoreCase("q")) {
                    quit = true;
                }
            }
            for (CPSCar c : cars) {
                c.disconnect();
            }
        }
    }

}
