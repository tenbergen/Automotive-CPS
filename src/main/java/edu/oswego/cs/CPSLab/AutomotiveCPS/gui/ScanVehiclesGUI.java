/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.gui;

import de.adesso.anki.Vehicle;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.ConnectorDAO;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author HN
 */
public class ScanVehiclesGUI extends Application {
    
    //Controller
    private ConnectorDAO connectorDAO;
    
    //Data
    private ListView<Vehicle> lv_vehicles = new ListView<>();
    
    //Component
    private Stage stage;
    private GridPane grid;
    private Text txt_vehicle_name;
    private Text txt_vehicle_desc;
    private ImageView iv_vehicle_thumbnail;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        
        this.grid = new GridPane();
        this.grid.setId("scan-vehicles-grid");
        this.grid.setAlignment(Pos.CENTER);
        this.grid.setHgap(Parameter.GRID_HGAP);
        this.grid.setVgap(Parameter.GRID_VGAP);
        this.grid.setPadding(new Insets(Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING));
        //this.grid.setGridLinesVisible(true);
        
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(Parameter.TITLE_VGAP);
    
        //***************************************************
        //********************* Title ***********************
        //***************************************************
        Text txt_title = new Text("Vehicle Scanning");
        txt_title.setId("title-text");
        
        vbox.getChildren().add(txt_title);
        //grid.add(txt_title,0,0);
        
        //***************************************************
        //******************** Vehicles *********************
        //***************************************************
        
        HBox hbox_selection = new HBox();
        hbox_selection.setMaxHeight(Parameter.HEIGHT_INDIVIDUAL_CAR);
        hbox_selection.setSpacing(Parameter.SIZE_SPACING);
        
        //Title + Thumbnail + Description
        VBox vbox_vehicle = new VBox();
        vbox_vehicle.setAlignment(Pos.CENTER);
        vbox_vehicle.setSpacing(Parameter.TITLE_VGAP);
        
        //Title
        txt_vehicle_name = new Text("Select a vehicle");
        txt_vehicle_name.setId("heading1-text");
        vbox_vehicle.getChildren().add(txt_vehicle_name);
        
        iv_vehicle_thumbnail = new ImageView();
        iv_vehicle_thumbnail.setFitHeight(Parameter.HEIGHT_THUMBNAIL);
        iv_vehicle_thumbnail.setPreserveRatio(true);
        iv_vehicle_thumbnail.setSmooth(true);
        iv_vehicle_thumbnail.setCache(true);
        vbox_vehicle.getChildren().add(iv_vehicle_thumbnail);
        
        txt_vehicle_desc = new Text();
        txt_vehicle_desc.setId("normal-text");
        txt_vehicle_desc.setWrappingWidth(Parameter.WIDTH_DESCRIPTION_CAR);
        vbox_vehicle.getChildren().add(txt_vehicle_desc);
                
        //List View
        
        lv_vehicles.setId("vehicle-listview");      
        lv_vehicles.setPrefHeight(Parameter.HEIGHT_LIST_CAR);
        lv_vehicles.setPrefWidth(Parameter.WIDTH_LIST_CAR);
        lv_vehicles.setCellFactory((ListView<Vehicle> l) -> new ScanVehiclesGUI.ColorRectCell());
        lv_vehicles.setItems(FXCollections.observableList(connectorDAO.scanVehicles()));
        
        //Handle select item
        lv_vehicles.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Vehicle>() {

            @Override
            public void changed(ObservableValue<? extends Vehicle> observable, Vehicle oldValue, Vehicle newValue) {
                // Action here
                System.out.println("Selected vehicle: " + newValue.getAdvertisement().getIdentifier());
                updateSelectedVehicle(newValue);
            }
        });       
        hbox_selection.getChildren().add(lv_vehicles);
        hbox_selection.getChildren().add(vbox_vehicle);
        
        vbox.getChildren().add(hbox_selection);
        //grid.add(hbox_selection,0,1);
        
        //***************************************************
        //********************* Button **********************
        //***************************************************
        HBox hbox_button = new HBox();
        hbox_button.setAlignment(Pos.CENTER);
        hbox_button.setSpacing(Parameter.SIZE_SPACING);
        
        Button btn_scan = new Button("Rescan");
        btn_scan.setMinWidth(Parameter.HEIGHT_BUTTON_SCAN_VEHICLE);
        btn_scan.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("Rescan ...");
                rescan();
            }
        });
        hbox_button.getChildren().add(btn_scan);
        
        
        Button btn_next = new Button("Next");
        btn_next.setMinWidth(Parameter.HEIGHT_BUTTON_SCAN_VEHICLE);
        btn_next.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("Next ...");
                //next();
            }
        });
        hbox_button.getChildren().add(btn_next);
        
        vbox.getChildren().add(hbox_button);
        //grid.add(hbox_button,0,2);
        
        grid.add(vbox, 0, 0);
        
        
        //***************************************************
        //###################### SCENE ######################
        //***************************************************
        
        Scene scene = new Scene(grid, Parameter.WIDTH_SCENE_SCAN_VEHICLES, Parameter.HEIGHT_SCENE_SCAN_VEHICLES);
        scene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CPS Vehicle Scanning");
        stage.show();
    }
    
    public void setConnectorDAO(ConnectorDAO connectorDAO){
        this.connectorDAO = connectorDAO;
    }
    
    static class ColorRectCell extends ListCell<Vehicle> {
        @Override
        public void updateItem(Vehicle item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null){
                String url = "edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Vehicle/" +item.getAdvertisement().getModel()+".png";
                //System.out.println(url);
                Image img = new Image(url);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(60);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.setCache(true);
                setGraphic(iv);
            }
            else{
                setGraphic(null);
            }
        }
    }
    
    private void updateSelectedVehicle(Vehicle vehicle){
        
        //Information
        txt_vehicle_name.setText(""+vehicle);
        txt_vehicle_desc.setText("Kind: "+vehicle.getAdvertisement().getModel());
        iv_vehicle_thumbnail.setImage(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Vehicle/" +vehicle.getAdvertisement().getModel()+".png"));       
    }
    
    public void rescan(){
        txt_vehicle_name.setText("Select a vehicle");
        txt_vehicle_desc.setText("");
        iv_vehicle_thumbnail.setImage(null);
        
        if(lv_vehicles.getSelectionModel().getSelectedItems()!=null)
            lv_vehicles.getSelectionModel().clearSelection();
        lv_vehicles.getItems().removeAll();
        lv_vehicles.setItems(FXCollections.observableList(connectorDAO.scanVehicles()));
    }
    
    
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        try{
            System.out.println("Disconnenct");
            connectorDAO.disconnect();  
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        launch(args);
    }
}
