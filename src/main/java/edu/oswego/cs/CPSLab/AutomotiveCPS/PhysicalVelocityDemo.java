package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.ConnectGUI;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gregory Maldonado
 * @since  06.11.2021
 */

public class PhysicalVelocityDemo {

    /**
     * Demo for testing threaded background velocity calculation
     */

    private static ArrayList<CPSCar> vehicles = new ArrayList<>();
    private static ArrayList<PhysicalVelocityDemo> demos = new ArrayList<>();
    private CPSCar car;

    public PhysicalVelocityDemo(Vehicle v) {
        car = new CPSCar(v);
        vehicles.add(car);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        ArrayList<Thread> threads = new ArrayList<>();
        System.out.println("Launching connector...");
        AnkiConnector anki = null;
        try {
            anki = new AnkiConnector("localhost", 5000);
        } catch (IOException ioe) {
            System.out.println("Error connecting to server. Is it running?");
            System.out.println("Exiting.");
            System.exit(0);
        }
        System.out.print(" looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();
        vehicles = vehicles.stream()
                .filter( x -> !(x.getAdvertisement().isCharging()))
                .collect(Collectors.toList());
        if (vehicles.isEmpty()) {
            System.out.println(" NO CARS FOUND. I guess that means we're done.");
        } else {
            System.out.println(" FOUND " + vehicles.size() + " CARS!");
            System.out.println(" Now connecting to and doing stuff to your cars.");

            // If the vehicle ArrayList is size 1 then we don't have to thread
            if (vehicles.size() == 1) {
                PhysicalVelocityDemo pvd = new PhysicalVelocityDemo(vehicles.get(0));
                pvd.run();
            }
            // If the vehicle ArrayList is greater than size 1 then we have the thread to run simultaneously
            else if (vehicles.size() > 1) {
                // threaded run
                for (Vehicle v : vehicles) {
                    Demo demo = new Demo(new PhysicalVelocityDemo(v));
                    demo.start();
                    threads.add(demo.getThread());
                    demos.add(demo.getTSD());
                }
                for (Thread t : threads) {
                    t.join();
                }
                for (PhysicalVelocityDemo demo : demos) {
                    Thread.sleep(3000);
                }
            }

        }
        for (Thread t : threads) {
            t.interrupt();
        }
        anki.close();
        System.out.println("Test complete.");
        System.exit(0);
    }

    /**
     * Actions performed by a CPSCar - where the velocity calculation is called
     */
    public void run() {
        car.loopTrack();
    }


    private static class Demo implements Runnable {
        PhysicalVelocityDemo tsd;
        Thread t = null;

        public Demo(PhysicalVelocityDemo tsd) { this.tsd = tsd; }

        @Override
        public void run() {
            tsd.run();
        }

        public void start() {
            if (t == null) {
                String name = tsd.car.getVehicle().getAdvertisement().getModel().toString();
                System.out.println("Starting up " + name);
                t = new Thread(this, name);
                t.start();
            }
        }

        public PhysicalVelocityDemo getTSD() { return tsd; }

        public Thread getThread() {
            return t;
        }

    }

}

