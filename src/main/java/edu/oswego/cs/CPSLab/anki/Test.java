package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AnkiConnector;
import de.adesso.anki.MessageListener;
import de.adesso.anki.Model;
import de.adesso.anki.NotificationListener;
import de.adesso.anki.Vehicle;
import de.adesso.anki.messages.BatteryLevelRequestMessage;
import de.adesso.anki.messages.LightsPatternMessage;
import de.adesso.anki.messages.LightsPatternMessage.LightConfig;
import de.adesso.anki.messages.Message;
import de.adesso.anki.messages.SdkModeMessage;
import de.adesso.anki.messages.SetSpeedMessage;
import de.adesso.anki.messages.TurnMessage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */
public class Test implements MessageListener, NotificationListener {
    
    public void messageReceived(Message message) {
        System.out.println(message);
    }
    
    public void onReceive(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Test t = new Test();
        
        Vehicle gs = null;
        System.out.println("Launching connectors...");
        AnkiConnector anki = new AnkiConnector("localhost", 5000);
        System.out.println("looking for cars...");
        List<Vehicle> vehicles = anki.findVehicles();

        if (vehicles.isEmpty()) {
            System.out.println("NO CARS FOUND.");
        } else {
            System.out.println("here are the cars...");
            Iterator<Vehicle> iter = vehicles.iterator();
            while (iter.hasNext()) {
                Vehicle v = iter.next();
                System.out.println(v);
                if (v.getAdvertisement().getModel() /*== null) {//*/.equals(Model.BIGBANG)) {
                    System.out.println("taking model " + v.getAdvertisement().getModel());
                    gs = v;
                }
            }

            if (gs != null) {
                System.out.println("connecting to car with address " + gs.getAddress());
                System.out.println(gs.toString());
                System.out.println("ID: " + gs.getAdvertisement().getIdentifier());
                System.out.println("Model: " + gs.getAdvertisement().getModel());
                System.out.println("Model ID: " + gs.getAdvertisement().getModelId());
                System.out.println("Product ID: " + gs.getAdvertisement().getProductId());
                System.out.println("charging? " + gs.getAdvertisement().isCharging());
                //dieser Call schlaegt fehlt...
            //    anki.addMessageListener(gs, t);
                //und hier gibt's haufenweise NullPointerExceptions...
                gs.connect();
                System.out.println("Connected. Setting SDK mode...");
                gs.sendMessage(new SdkModeMessage());
                System.out.println("SDK Mode set. Battery Level is:");
                //TODO this.
                //gs.sendMessage(new BatteryLevelRequestMessage());
                //gs.addMessageListener(klass, t);
                System.out.println("UNKOWN BATT LEVEL");
                System.out.println("Setting lights...");
                LightConfig lc = new LightConfig(LightsPatternMessage.LightChannel.TAIL, LightsPatternMessage.LightEffect.STROBE, 0, 0, 0);
                LightsPatternMessage lpm = new LightsPatternMessage();
                lpm.add(lc);
                gs.sendMessage(lpm);
                System.out.println("Setting Speed...");
                gs.sendMessage(new SetSpeedMessage(255, 255));
                 gs.sendMessage(new TurnMessage());
                System.out.println("Sleeping for 10secs..");
               Thread.sleep(10000);
                System.out.println("Disconnecting");
               gs.disconnect();

            }
        }
                        anki.close();
                System.exit(0);
    }
}
