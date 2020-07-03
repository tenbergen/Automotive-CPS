/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;

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
    
    
    
    
    
}
