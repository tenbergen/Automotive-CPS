/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.controller;

import edu.oswego.cs.CPSLab.AutomotiveCPS.gui.Parameter;
import de.adesso.anki.AnkiConnector;
import de.adesso.anki.Vehicle;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HN
 */
public class ConnectorDAO {
    private AnkiConnector ankiConnector;
    private List<Vehicle> scanningVehicles;
    private List<VehicleDAO> vehicles;
    private VehicleDAO selectedVehicle;
    private String ip;
    private int port;   
    
    /** 
     * Map 
     */
    private List<MapDAO> maps = new ArrayList<>();
    private List<RoadmapManager> managers = new ArrayList<>();  
    private boolean scanTrackComplete = false;
    private boolean scanningTrack = false;
    
    public ConnectorDAO(String ip, int port) throws IOException{
        this.ankiConnector = new AnkiConnector(ip,port);
        this.vehicles = new ArrayList<>();
        this.ip = ip;
        this.port = port;
    }
    
    public void disconnect(){
        try{
            System.out.println("ConnectorDAO - disconnect ...");
            for (VehicleDAO v: vehicles){
                v.disconnect();
            }
            //ankiConnector.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public AnkiConnector getAnkiConnector(){
        return ankiConnector;
    }

    public List<VehicleDAO> getVehicles() {
        return this.vehicles;
    }

    public void setVehicles(List<VehicleDAO> vehicles) {
        this.vehicles = vehicles;
    }

    public VehicleDAO getSelectedVehicle() {
        return selectedVehicle;
    }

    public void setSelectedVehicle(VehicleDAO selectedVehicle) {
        this.selectedVehicle = selectedVehicle;
    }
    
    public void addVehicle(Vehicle v){
        try{
            System.out.println("   " + v);
            System.out.println("      ID: " + v.getAdvertisement().getIdentifier());
            System.out.println("      Model: " + v.getAdvertisement().getModel());
            System.out.println("      Model ID: " + v.getAdvertisement().getModelId());
            System.out.println("      Product ID: " + v.getAdvertisement().getProductId());
            System.out.println("      Address: " + v.getAddress());
            System.out.println("      Color: " + v.getColor());
            System.out.println("      charging? " + v.getAdvertisement().isCharging());
                       
            VehicleDAO vehicle = new VehicleDAO();
            vehicle.setCpsCar(new CPSCar(v));
            vehicle.setImg(Parameter.PATH_MEDIA+"Vehicle/" +v.getAdvertisement().getModel()+".png");
            
            this.vehicles.add(vehicle);
        }
        catch(Exception e){
            e.printStackTrace();
        }      
    }
    
    public void updateVehicles(){     
        try{      
            System.out.println("ConnectorDAO - updateVehicles ...");
            for (Vehicle v : this.scanningVehicles) {
                String key = ""+v.getAdvertisement().getIdentifier();
                System.out.print("Get car: "+v.getAdvertisement().getModel());
                addVehicle(v);   
            }
        }     
        catch(Exception e){
            e.printStackTrace();
        }       
    }
    
    public void scanVehicles(){
        try {
            System.out.println("ConnectorDAO - scanVehicles ...");
            this.scanningVehicles = ankiConnector.findVehicles();
            for (Vehicle v : this.scanningVehicles) {
                System.out.println("Get car: "+v.getAdvertisement().getModel());
            }
            
        } catch (Exception ex) {
            Logger.getLogger(ConnectorDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void reconnect(){
        try{
            
            this.disconnect();
            System.out.println("ConnectorDAO - reconnect ...");
            this.vehicles = new ArrayList<>();
            this.selectedVehicle = null;
            this.ankiConnector = new AnkiConnector(this.ip,this.port);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    public List<String> getLightBehavior(){
        List<String> lights = new ArrayList();
        lights.add(Parameter.BEHAVIOR_BRAKE_LIGHT);
        lights.add(Parameter.BEHAVIOR_EMERGENCY_LIGHT);
        lights.add(Parameter.BEHAVIOR_FOUR_WAY_HAZARD_LIGHT);   
        return lights;
    }
    
    public List<String> getMovementBehavior(){
        List<String> movement = new ArrayList();
        movement.add(Parameter.BEHAVIOR_CHANGE_LANE);
        movement.add(Parameter.BEHAVIOR_EMERGENCY_STOP);
        movement.add(Parameter.BEHAVIOR_PULL_OVER);  
        movement.add(Parameter.BEHAVIOR_U_TURN); 
        return movement;
    }

    /**
     * Perform BEHAVIORS
     */  
    public void performBehavior(String behavior) {
        switch(behavior){
            case Parameter.BEHAVIOR_BRAKE_LIGHT:
                turnOnBrakeLight();
                break;
                
            case Parameter.BEHAVIOR_EMERGENCY_LIGHT:
                turnOnEmergencyLight();
                break;
                
            case Parameter.BEHAVIOR_FOUR_WAY_HAZARD_LIGHT:
                turnOnFourWayHazardLight();
                break;
                
            case Parameter.BEHAVIOR_EMERGENCY_STOP:
                performEmergencyStop();
                break;
            
            case Parameter.BEHAVIOR_PULL_OVER:
                performPullOver();
                break;
                
            case Parameter.BEHAVIOR_CHANGE_LANE:
                performChangeLane();
                break;
                
            case Parameter.BEHAVIOR_U_TURN:
                performUTurn();
                break;
            
        }
    }
    
    public void stopBehavior(String behavior){
        switch(behavior){
            case Parameter.BEHAVIOR_BRAKE_LIGHT:
                turnOffBrakeLight();
                break;
                
            case Parameter.BEHAVIOR_EMERGENCY_LIGHT:
                turnOffEmergencyLight();
                break;
                
            case Parameter.BEHAVIOR_FOUR_WAY_HAZARD_LIGHT:
                turnOffFourWayHazardLight();
                break;
        }
    }
    
    public void turnOnBrakeLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOnBrakeLight();
    }
    
    public void turnOffBrakeLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOffBrakeLight();
    }
    
    public void turnOnEmergencyLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOnEmergencyLight();
    }
    
    public void turnOffEmergencyLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOffEmergencyLight();
    }
    
    public void turnOnFourWayHazardLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOnFourWayHazardLight();
    }
    
    public void turnOffFourWayHazardLight(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.turnOffFourWayHazardLight();       
    }
    
    public void performEmergencyStop(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.performEmergencyStop();   
    }
    
    public void performPullOver(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.performPullOver();   
    }
    
    public void performChangeLane(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.performChangeLane();   
    }
    
    public void performUTurn(){
        if (selectedVehicle==null)
            return;
        selectedVehicle.performUTurn();   
    }

    /**
     * Scanning Track
     */
    public boolean isScanningTrack() {
        return scanningTrack;
    }

    public boolean isScanTrackComplete() {
        return scanTrackComplete;
    }
    
    public void scanTrack(){
        while (!scanTrackComplete) {
            // If scan is done, get notified
            for (VehicleDAO vehicleDAO : vehicles) {
                if (vehicleDAO.getCpsCar().scanDone() && vehicleDAO.getCpsCar().getManager() == null) {
                    for (RoadmapManager rm : managers) {
                        if (vehicleDAO.getCpsCar().getPieceIDs().equals(rm.getPieceIDs()) && vehicleDAO.getCpsCar().getReverses().equals(rm.getReverses())) {
                            System.out.println("Same manager...");
                            vehicleDAO.getCpsCar().setRoadmapMannager(rm);
                        }
                    }
                    if (vehicleDAO.getCpsCar().getManager() == null) {
                        System.out.println("New manager...");
                        RoadmapManager rm = new RoadmapManager(vehicleDAO.getCpsCar().getMap(), vehicleDAO.getCpsCar().getReverse(), vehicleDAO.getCpsCar().getPieceIDs(), vehicleDAO.getCpsCar().getReverses());
                        managers.add(rm);
                        rm.setID(managers.indexOf(rm));
                        vehicleDAO.getCpsCar().setRoadmapMannager(rm);
                    }
                    scanTrackComplete = true;
                } else {
                    scanTrackComplete = false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ConnectorDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("GUI - SCAN DONE");
        System.out.println("GUI - Roadmap Manager: " + managers.toString());
        System.out.println("GUI - set track");
        setTrack();
    }

    /**
     * Set Track
     */
    
    private void setTrack(){
        if (!this.scanTrackComplete)
            return;
        for(RoadmapManager rm : managers){
            MapDAO map = new MapDAO();
            map.setTracks(rm.getTrack());
            map.printBoard();
            maps.add(map);
        }     
    }

    public List<MapDAO> getMaps() {
        return maps;
    }
    
    
}
