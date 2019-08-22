package edu.oswego.cs.CPSLab.anki;

import de.adesso.anki.AdvertisementData;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;

/**
 * A wrapper for de.adesso.anki.Vehicle that adds minimal functionality needed for vehicles to cyber-physically talk to
 * one another. Simply use this class to instantiate a Vehicle.
 *
 * @author Bastian Tenbergen (bastian.tenbergen@oswego.edu)
 */

public class CPSCar extends Vehicle {

    private Vehicle v;

    /**
     * Creates a new CPS car by simply calling the constructor of the super class.
     */
    public CPSCar(AnkiConnector anki, String address, String manufacturerData, String localName) {
        super(anki, address, manufacturerData, localName);
    }

    /**
     * Broadcasts a search for other CPSCars.
     */
    public void findVehicles() {
        //todo
    }

    /**
     * replies to other cars' broadcast.
     */
    public void replyBroadcast() {

    }

    /**
     * Determines if a car that answered a broadcast is a) new, b) known, or c) disappeared.
     */
    private void updateCPSNetwork() {

    }
}
