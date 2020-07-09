/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author HN
 */
public class GUITestProgram {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
        String ip = "192.168.1.101";
        int port = 5000;
        
        System.out.println("Launching connector...");
        AnkiConnector anki = new AnkiConnector(ip, port);
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
                    System.out.println("Confirm Quit");
                    quit = true;
                }
            }
            for (CPSCar c : cars) {
                c.disconnect();
            }
        }
      

    }
}
