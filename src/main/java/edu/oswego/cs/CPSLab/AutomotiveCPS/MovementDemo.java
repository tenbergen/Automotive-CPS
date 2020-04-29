package edu.oswego.cs.CPSLab.AutomotiveCPS;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.RoadmapScanner;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.LocalizationPositionUpdateMessage;
import de.adesso.anki.messages.SdkModeMessage;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * A simple test program to test a connection to your Anki 'Supercars' and
 * 'Supertrucks' using the NodeJS Bluetooth gateway. Simple follow the
 * installation instructions at http://github.com/adessoAG/anki-drive-java,
 * build this project, start the bluetooth gateway using ./gradlew server, and
 * run this class.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class MovementDemo implements Runnable {

    private AnkiConnector anki;
    private List<Vehicle> vehicles;
    private Vehicle v;
    private RoadmapScanner rms;

    public MovementDemo() {
        try {
            this.anki = new AnkiConnector("localhost", 5000);
        } catch (IOException ioe) {
            System.out.println("An error occurred: " + ioe.getMessage());
            anki.close();
            System.exit(1);
        }

        this.vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("NO CARS FOUND.");
            anki.close();
            System.exit(0);
        } else {
            this.v = vehicles.get(0);
            v.connect();
            v.sendMessage(new SdkModeMessage());
            v.addMessageListener(LocalizationPositionUpdateMessage.class, new LocalizationPositionUpdateHandler(v));
            rms = new RoadmapScanner(v);
        }
    }

    @Override
    public void run() {
     //  v.sendMessage(new SetSpeedMessage(250, 150));
     rms.startScanning();
     while (!rms.isComplete()) {
         continue;
     }
     if (rms.isComplete()) {
             System.out.println("YATY!");
         }
     
    }

    public void stop() {
        v.disconnect();
        anki.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        MovementDemo md = new MovementDemo();
        md.run();
       // Thread t = new Thread(md);
        //t.start();
        System.out.println("press q to quit");
        Scanner kb = new Scanner(System.in);
     //   if (kb.nextLine().equals("q")) {
      //      md.stop();
      //  }
    }

    /**
     * Handles the response from the vehicle from the
     * BatteryLevelRequestMessage. We need handler classes because responses
     * from the vehicles are asynchronous.
     */
    private class LocalizationPositionUpdateHandler implements MessageListener<LocalizationPositionUpdateMessage> {

        private Vehicle v;
        
        public LocalizationPositionUpdateHandler(Vehicle v) {
            this.v = v;
        }
        
        @Override
        public void messageReceived(LocalizationPositionUpdateMessage m) {
            if (m.getRoadPieceId() == 33) {
              //  System.out.println("CAR IS AT START! Stopping at location: " + m.getLocationId());
                System.out.println(m);
             //   v.sendMessage(new SetSpeedMessage(0, 0));
            //   v.sendMessage(new TurnMessage(180, 1));
            }
        }
    }
}
