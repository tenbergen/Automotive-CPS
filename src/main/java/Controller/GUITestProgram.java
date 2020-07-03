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
               
        ConnectorDAO connector = new ConnectorDAO(ip,port); 
        System.out.println("Launching connector...");
        
        connector.updateVehicles();
        
      

    }
}
