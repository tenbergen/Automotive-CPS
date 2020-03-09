/*
 * Testing the CPSCar Class
 */
package edu.oswego.cs.CPSLab.anki;

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
        AnkiConnector anki = new AnkiConnector("192.168.1.100", 5000);
        System.out.println("...looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles(); // Add the CPSCar class to AnkiConnector?
        List<CPSCar> cars = new ArrayList<>();
        Map map1 = new Map();
        map1.map1();
        Map map2 = new Map();
        map2.map2();
        // int random = (int) (Math.random() * 200);

        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");

        } else {
            System.out.println(vehicles.toString());
            CPSCar cps1 = new CPSCar(vehicles.get(0), anki, map2, 68);
            CPSCar cps2 = new CPSCar(vehicles.get(1), anki, map2, 68);
//            CPSCar cps3 = new CPSCar(vehicles.get(2), anki, map2);
//            CPSCar cps4 = new CPSCar(vehicles.get(3), anki, map2);
//            CPSCar cps5 = new CPSCar(vehicles.get(4), anki, map1);
            cars.add(cps1);
            cars.add(cps2);
//            cars.add(cps3);
//            cars.add(cps4);
//            cars.add(cps5);
            cps1.sendMessage(new SdkModeMessage());
            cps2.sendMessage(new SdkModeMessage());
//            cps3.sendMessage(new SdkModeMessage());
//            cps4.sendMessage(new SdkModeMessage());
//            cps5.sendMessage(new SdkModeMessage());
            cps1.sendMessage(new SetSpeedMessage(300, 100));
            cps2.sendMessage(new SetSpeedMessage(500, 100));
//            cps3.sendMessage(new SetSpeedMessage(400, 100));
//            cps4.sendMessage(new SetSpeedMessage(350, 100));
//            cps5.sendMessage(new SetSpeedMessage(350, 100));
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
