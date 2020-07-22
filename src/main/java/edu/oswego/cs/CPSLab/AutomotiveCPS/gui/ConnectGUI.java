/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.gui;

import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.ConnectorDAO;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author HN
 */
public class ConnectGUI extends Application {
    
    public GridPane grid;
    
    @Override
    public void start(Stage primaryStage) {
      
        grid = new GridPane();
        grid.setId("connection-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(Parameter.GRID_HGAP);
        grid.setVgap(Parameter.GRID_VGAP);
        grid.setPadding(new Insets(Parameter.SIZE_PADDING, Parameter.SIZE_PADDING, Parameter.SIZE_PADDING, Parameter.SIZE_PADDING));
        
        //grid.setGridLinesVisible(true);
        
        //Title
        Text scenetitle = new Text("Automotive CPS");
        scenetitle.setId("title-text");
        grid.add(scenetitle, 0, 0, 2, 1);
        
        //IP Address label and text field
        Label lb_ip_address = new Label("IP Address:");
        grid.add(lb_ip_address, 0, 1);

        TextField txt_ip_address = new TextField("192.168.1.101");
        grid.add(txt_ip_address, 1, 1);

        //Port label and text field
        Label lb_port = new Label("Port:");
        grid.add(lb_port, 0, 2);

        TextField txt_port = new TextField("5000");
        grid.add(txt_port, 1, 2);
        
        //Announcement text
        final Text announcement = new Text();
        announcement.setId("announcement-text");
        grid.add(announcement, 1, 6);
        
        //Connecting Button
        Button btn_connect = new Button("Connect");
        HBox hbBtn = new HBox(Parameter.COMPONENT_HGAP);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn_connect);
        grid.add(hbBtn, 1, 4);
        
        btn_connect.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent e) {
                String ip = txt_ip_address.getText();
                int port=5000;
                
                //Handle port input
                try{
                    port = Integer.parseInt(txt_port.getText());
                }
                catch(NumberFormatException exception){
                    announcement.setText("Port is not valid, please try again");
                    txt_port.setText("");
                }       
                
                //Launching connector
                try {
                    System.out.println("Launching connector...");
                    /*final ProgressIndicator pi = new ProgressIndicator();
                    pi.setProgress(-1.0f);
                    grid.add(pi, 1, 7);*/
                    
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.print(".");
                                Thread.sleep(100);
                                
                            } 
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    
                    System.out.println(ip+"-"+port);
                    ConnectorDAO connector = new ConnectorDAO(ip,port); 
                    
                    //Call control stage
                    Stage controlStage = new Stage();
                    ControlGUI control = new ControlGUI();
                    control.setConnectorDAO(connector);
                    control.start(controlStage);
                    controlStage.show();
                    
                    //Clean before close
                    connector.getAnkiConnector().close();
                    primaryStage.close();
                    
                } catch (IOException ex) {
                    announcement.setText("Connect fail, please try again");
                    Logger.getLogger(ConnectGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        
               
        Scene scene = new Scene(grid, Parameter.WIDTH_SCENE_CONNECT, Parameter.HEIGHT_SCENE_CONNECT);
        scene.getStylesheets().add(ConnectGUI.class.getResource("design-style.css").toExternalForm());
        
        URL url = ConnectGUI.class.getResource(".");
        System.out.println("Value = " + url);
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("CPS Connection");
        primaryStage.show();
    }
    
    public GridPane getGrid(){
        return this.grid;
    }
    
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public class LoadingThread extends Thread {

        @Override
        public void run(){
           System.out.println("MyThread running");
           //Progress bar 
           
        }
    }
}
