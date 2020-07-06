/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.BrakeLight;

/**
 *
 * @author HN
 */
public class VehicleDAO {
    
    private CPSCar cpsCar;
    /*
    private int speed;
    private float offset;
    */
    
    private String name;
    private String img;
    private String type;
    private String description;
    private int battery;
    
    private BrakeLight brakeLight;
    

    
    public CPSCar getCpsCar() {
        return cpsCar;
    }

    public void setCpsCar(CPSCar cpsCar) {
        this.cpsCar = cpsCar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }
    
    public void turnOnBrakeLight(){
        if (this.brakeLight == null){
            this.brakeLight = new BrakeLight(this.cpsCar);
        }
        this.brakeLight.turnOn();
        System.out.println("Turn on brake light Vehicle DAO");
    }
    
    public void turnOffBrakeLight(){
        if (this.brakeLight == null){
            this.brakeLight = new BrakeLight(this.cpsCar);
        }
        this.brakeLight.turnOff();
        System.out.println("Turn off brake light Vehicle DAO");
    }
}
