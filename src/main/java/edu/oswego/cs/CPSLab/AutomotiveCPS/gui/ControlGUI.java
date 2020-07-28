/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.oswego.cs.CPSLab.AutomotiveCPS.gui;

import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.ConnectorDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.MapDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.VehicleDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author HN
 */
public class ControlGUI extends Application {
    
    private Stage stage;
    private ConnectorDAO connectorDAO;
      
    //******** Map ********
    private List<MapDAO> maps = new ArrayList<>();
    List<RoadmapManager> roadmapManagers = new ArrayList<>();
    private boolean scanTrackComplete = false;
    private VBox vbox_map;
    
    //******** Selected Car ********
    private UpdateRealTimeData updateRealTimeData;
    private Text txt_vehicle_name;
    private ImageView iv_vehicle_thumbnail;
    private Text txt_control_parameter_offset;
    private Text txt_control_parameter_speed;
    private Text txt_control_parameter_battery;
    
    //******** List of Vehicles ********
    private ListView<VehicleDAO> lv_vehicles = new ListView<>();
    private ObservableList<VehicleDAO> observable_list_vehicles;
     
    //******** Behavior Drag & Drop ********
    private final ObjectProperty<TreeCell<String>> dragSource = new SimpleObjectProperty<>();
    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
    private TreeItem<String> draggedItem;
    private ListView<String> lv_current_behaviors;
  
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        disconnect();
        stage.close();
    }
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        GridPane grid = new GridPane();
        grid.setId("control-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(Parameter.GRID_HGAP_CONTROL);
        grid.setVgap(Parameter.GRID_VGAP);
        grid.setPadding(new Insets(Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING));
        grid.setGridLinesVisible(true);
        
        //this.connectorDAO.scanVehicles();
        this.connectorDAO.updateVehicles();
        
        //***************************************************
        //**************** List of Vehicles *****************
        //***************************************************

        VBox vbox_list_vehicles = new VBox();
        vbox_list_vehicles.setMaxHeight(Parameter.HEIGHT_INDIVIDUAL_CAR);
        vbox_list_vehicles.setSpacing(Parameter.SIZE_SPACING);
                
        //****************List View****************
        observable_list_vehicles = FXCollections.observableList(connectorDAO.getVehicles());
        
        lv_vehicles.setId("vehicle-listview");      
        lv_vehicles.setPrefHeight(Parameter.HEIGHT_LIST_CAR);
        lv_vehicles.setPrefWidth(Parameter.WIDTH_LIST_CAR);
        lv_vehicles.setCellFactory((ListView<VehicleDAO> l) -> new ColorRectCell());
        lv_vehicles.setItems(this.observable_list_vehicles);
        
        //Handle select item
        lv_vehicles.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VehicleDAO>() {
            @Override
            public void changed(ObservableValue<? extends VehicleDAO> observable, VehicleDAO oldValue, VehicleDAO newValue) {
                // Action here
                System.out.println("Selected vehicle: " + newValue.getCpsCar().getVehicle().getAdvertisement().getIdentifier());
                updateSelectedVehicle(newValue);
            }
        });       
        vbox_list_vehicles.getChildren().add(lv_vehicles);
        
        //**************** Button Rescan ****************
        Button btn_rescan = new Button("Rescan");
        btn_rescan.setId("control-button");
        btn_rescan.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("Rescan ...");
                rescan();
            }
        });
        vbox_list_vehicles.getChildren().add(btn_rescan);
        
        //**************** Button Scan Track ****************
        Button btn_scan_track = new Button("Scan Track");
        btn_scan_track.setId("control-button");
        btn_scan_track.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("Scan Track ...");
                drawTrack();
                //ScanTrackThread scanTrackThread = new ScanTrackThread();
                //scanTrackThread.start();
            }
        });
        vbox_list_vehicles.getChildren().add(btn_scan_track);
        
        
        
        //**************** Button Disconnect ****************
        Button btn_disconnect = new Button("Disconnect");
        btn_disconnect.setId("control-button");
        btn_disconnect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                disconnect();
                
                //Call Connect stage
                Stage connectStage = new Stage();
                ConnectGUI connectGUI = new ConnectGUI();
                connectGUI.start(connectStage);
                connectStage.show();
                    
                stage.close();
            }
        });
        vbox_list_vehicles.getChildren().add(btn_disconnect);
        
        grid.add(vbox_list_vehicles,0,0);
        
        
        //***************************************************
        //#################### Control #####################
        //***************************************************
        VBox vbox_control = new VBox();
        vbox_control.setSpacing(Parameter.SIZE_SPACING);
        vbox_control.setAlignment(Pos.CENTER);
        
        //Parameter box
        HBox hbox_control_parameter = new HBox();
        hbox_control_parameter.setAlignment(Pos.CENTER);
        hbox_control_parameter.setSpacing(Parameter.ICON_HGAP);
        
        //Offset box
        VBox vbox_control_parameter_offset = new VBox();  
        vbox_control_parameter_offset.setAlignment(Pos.CENTER);
        
        //Offset icon
        ImageView iv_control_parameter_offset= new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Icon/offset.png"));
        iv_control_parameter_offset.setFitHeight(Parameter.SIZE_ICON_MEDIUM);
        iv_control_parameter_offset.setPreserveRatio(true);
        iv_control_parameter_offset.setSmooth(true);
        iv_control_parameter_offset.setCache(true);
        vbox_control_parameter_offset.getChildren().add(iv_control_parameter_offset);
         
        //Offset label
        txt_control_parameter_offset = new Text("Not Detected");
        txt_control_parameter_offset.setId("label-text");
        vbox_control_parameter_offset.getChildren().add(txt_control_parameter_offset);
        
        hbox_control_parameter.getChildren().add(vbox_control_parameter_offset);
        
        //Speed box
        VBox vbox_control_parameter_speed = new VBox();   
        vbox_control_parameter_speed.setAlignment(Pos.CENTER);
        
        //Speed icon
        ImageView iv_control_parameter_speed= new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Icon/speed.png"));
        iv_control_parameter_speed.setFitHeight(Parameter.SIZE_ICON_MEDIUM);
        iv_control_parameter_speed.setPreserveRatio(true);
        iv_control_parameter_speed.setSmooth(true);
        iv_control_parameter_speed.setCache(true);
        vbox_control_parameter_speed.getChildren().add(iv_control_parameter_speed);
        
        //Speed label
        txt_control_parameter_speed = new Text("Not Detected");
        txt_control_parameter_speed.setId("label-text");
        vbox_control_parameter_speed.getChildren().add(txt_control_parameter_speed);
        
        hbox_control_parameter.getChildren().add(vbox_control_parameter_speed);
        
        //Battery box
        VBox vbox_control_parameter_battery = new VBox();     
        vbox_control_parameter_battery.setAlignment(Pos.CENTER);
        
        //Battery icon
        ImageView iv_control_parameter_battery= new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Icon/battery.png"));
        iv_control_parameter_battery.setFitHeight(Parameter.SIZE_ICON_MEDIUM);
        iv_control_parameter_battery.setPreserveRatio(true);
        iv_control_parameter_battery.setSmooth(true);
        iv_control_parameter_battery.setCache(true);
        vbox_control_parameter_battery.getChildren().add(iv_control_parameter_battery);
        
        //Batter label
        txt_control_parameter_battery = new Text("Not Detected");
        txt_control_parameter_battery.setId("label-text");
        vbox_control_parameter_battery.getChildren().add(txt_control_parameter_battery);
              
        hbox_control_parameter.getChildren().add(vbox_control_parameter_battery);
        
        vbox_control.getChildren().add(hbox_control_parameter);
        
        //Vehicle box
        VBox vbox_control_vehicle = new VBox();
        vbox_control_vehicle.setAlignment(Pos.CENTER);
        
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
        vbox_control_vehicle.getChildren().add(vbox_vehicle);
        vbox_control.getChildren().add(vbox_control_vehicle);
        
        
        //*********** Adjust speed ***********
        HBox hbox_control_adjust_speed = new HBox();
        hbox_control_adjust_speed.setAlignment(Pos.CENTER);
        hbox_control_adjust_speed.setSpacing(Parameter.COMPONENT_VGAP);
        
        //Adjust speed arrow - UP        
        ImageView iv_control_adjust_speed_up = new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Icon/arrow-up.png"));
        iv_control_adjust_speed_up.setFitHeight(Parameter.SIZE_ICON_SMALL);      
        iv_control_adjust_speed_up.setPreserveRatio(true);
        iv_control_adjust_speed_up.setSmooth(true);
        iv_control_adjust_speed_up.setCache(true);        
        
        Button btn_control_adjust_speed_up = new Button(); 
        btn_control_adjust_speed_up.setGraphic(iv_control_adjust_speed_up);
        btn_control_adjust_speed_up.setId("adjust-button");
        btn_control_adjust_speed_up.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                adjustSpeed(true);          
            }
        });
        hbox_control_adjust_speed.getChildren().add(btn_control_adjust_speed_up);
        
        //Adjust speed text
        Text txt_control_adjust_speed = new Text("Speed");
        txt_control_adjust_speed.setId("label-text");   
        hbox_control_adjust_speed.getChildren().add(txt_control_adjust_speed);
        
        //Adjust speed arrow - DOWN       
        ImageView iv_control_adjust_speed_down = new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Icon/arrow-down.png"));
        iv_control_adjust_speed_down.setFitHeight(Parameter.SIZE_ICON_SMALL);      
        iv_control_adjust_speed_down.setPreserveRatio(true);
        iv_control_adjust_speed_down.setSmooth(true);
        iv_control_adjust_speed_down.setCache(true);        
        
        Button btn_control_adjust_speed_down = new Button(); 
        btn_control_adjust_speed_down.setGraphic(iv_control_adjust_speed_down);
        btn_control_adjust_speed_down.setId("adjust-button");
        btn_control_adjust_speed_down.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {   
                adjustSpeed(false);           
            }
        });
        hbox_control_adjust_speed.getChildren().add(btn_control_adjust_speed_down);       
        vbox_control.getChildren().add(hbox_control_adjust_speed);
        
        //***************************************************
        //#################### Behavior #####################
        //***************************************************
               
        //Behavior Box
        VBox vbox_behavior = new VBox();
        vbox_behavior.setAlignment(Pos.CENTER);
        vbox_behavior.setSpacing(Parameter.COMPONENT_VGAP);
        vbox_behavior.setStyle("-fx-background-color: black;");
        
        //Behavior Title
        Text txt_behavior = new Text("Vehicle Behavior");
        txt_behavior.setId("heading1-text");
        vbox_behavior.getChildren().add(txt_behavior);
        
        //Drag and Drop Box
        HBox hbox_behavior_drag_drop = new HBox();
        hbox_behavior_drag_drop.setAlignment(Pos.CENTER);
        
        //Box of list of available behaviors
        VBox vbox_available_behavior = new VBox();
        vbox_available_behavior.setAlignment(Pos.CENTER);
        
        //Title of list of available behaviors
        Text txt_available_behavior = new Text("Available Behavior");
        txt_available_behavior.setId("label-text");
        vbox_available_behavior.getChildren().add(txt_available_behavior);
        
        //List of behavior
        TreeItem<String> behaviors = new TreeItem<> ("Behaviors");
        behaviors.setExpanded(true);
        
        //Behavior: Light Behavior
        TreeItem<String> light_behavior = new TreeItem<> ("Light Behavior");
        light_behavior.setExpanded(true);
        for (String item: connectorDAO.getLightBehavior()){
            light_behavior.getChildren().add(new TreeItem<> (item));
        }
        behaviors.getChildren().add(light_behavior);
        
        //Behavior: Movement Behavior
        TreeItem<String> movement_behavior = new TreeItem<> ("Movement Behavior");
        movement_behavior.setExpanded(true);
        for (String item: connectorDAO.getMovementBehavior()){
            movement_behavior.getChildren().add(new TreeItem<> (item));
        }        
        behaviors.getChildren().add(movement_behavior);
        
        //Tree View of behaviors
        TreeView<String> behavior_tree = new TreeView(behaviors); 
        behavior_tree.setId("behavior-treeview");
        behavior_tree.setCellFactory(param -> {
            TreeCell<String> treeCell = new TreeCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };
            
            treeCell.setOnDragDetected(event -> {
                draggedItem = treeCell.getTreeItem();

                // root can't be dragged
                if (draggedItem.getParent() == null) 
                    return;
                
                // categories can't be dragged
                if (draggedItem.getParent().getParent() == null) 
                    return;
                
                if (!treeCell.isEmpty()) {
                    Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(treeCell.getItem());
                    db.setContent(cc);
                    dragSource.set(treeCell);
                    db.setDragView(treeCell.snapshot(null, null));
                    event.consume();
                }
            });

            treeCell.setOnDragOver(event -> {
                if (!event.getDragboard().hasContent(JAVA_FORMAT)) 
                    return;
                
                TreeItem<String> thisItem = treeCell.getTreeItem();
                //System.out.println("setOnDragOver"); 
                
                // can't drop on itself
                if (draggedItem == null || thisItem == null || thisItem == draggedItem) 
                    return;
                
                // ignore if this is the root
                if (draggedItem.getParent() == null || draggedItem.getParent().getParent() == null)
                    return;

                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    //System.out.println("setOnDragOver");
                }
                
            });

            treeCell.setOnDragDone(event -> {
                //System.out.println("TreeCell: setOnDragDone");           
             });

            treeCell.setOnDragDropped(event -> {
                //System.out.println("TreeCell: setOnDragDropped");  
            });

            return treeCell;
        });
        
        vbox_available_behavior.getChildren().add(behavior_tree);
        
        hbox_behavior_drag_drop.getChildren().add(vbox_available_behavior);
    
        //Box of list of available behaviors
        VBox vbox_current_behavior = new VBox();
        vbox_current_behavior.setAlignment(Pos.CENTER);
        
        //Title of list of available behaviors
        Text txt_current_behavior = new Text("Current Behavior");
        txt_current_behavior.setId("label-text");
        vbox_current_behavior.getChildren().add(txt_current_behavior);
    
        //List view of current behaviors
        lv_current_behaviors = new ListView<>();
        lv_current_behaviors.setId("behavior-listview");
        lv_current_behaviors.getItems().add(Parameter.BEHAVIOR_CONNECTED);
        
        lv_current_behaviors.setCellFactory(lv -> {
            ListCell<String> listCell = new ListCell<String>(){
                @Override
                public void updateItem(String item , boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };

            listCell.setOnDragDetected(event -> {
                if (!listCell.isEmpty()) {
                    Dragboard db = listCell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(listCell.getItem());
                    db.setContent(cc);
                    //System.out.println("listCell: setOnDragDetected");
                }
            });

            listCell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    //System.out.println("listCell: setOnDragOver");
                }
            });

            listCell.setOnDragDone(event -> {
                removeBehavior(listCell);
                //System.out.println("listCell: setOnDragDone");           
             });

            listCell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                             
                if (db.hasString()) {
                    addBehavior();
                    event.setDropCompleted(true);
                    dragSource.set(null);
                } else {
                    event.setDropCompleted(false);
                }
                //System.out.println("listCell: setOnDragDropped");   
            });

            return listCell;
        });
        vbox_current_behavior.getChildren().add(lv_current_behaviors);
              
        hbox_behavior_drag_drop.getChildren().add(vbox_current_behavior);        
        
        vbox_behavior.getChildren().add(hbox_behavior_drag_drop);
        
        
        vbox_control.getChildren().add(vbox_behavior);
                 
        grid.add(vbox_control,1,0);
        
        
        //***************************************************
        //*********************** Map ***********************
        //***************************************************
        
        this.vbox_map = new VBox();
        this.vbox_map.setAlignment(Pos.TOP_CENTER);
        this.vbox_map.setSpacing(Parameter.BOX_VGAP);
        
        Text txt_map = new Text("Map");
        txt_map.setTextAlignment(TextAlignment.CENTER);
        txt_map.setId("heading1-text");
        vbox_map.getChildren().add(txt_map);
        
        grid.add(vbox_map, 2,0);
        
        
        
        //***************************************************
        //###################### SCENE ######################
        //***************************************************
        
        updateRealTimeData = new UpdateRealTimeData();
        updateRealTimeData.start();       
        
               
        Scene scene = new Scene(grid, Parameter.WIDTH_SCENE_CONTROL, Parameter.HEIGHT_SCENE_CONTROL);
        scene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CPS Control");
        stage.show();
    }
    
    public void setConnectorDAO(ConnectorDAO connectorDAO){
        this.connectorDAO = connectorDAO;
    }
    
    static class ColorRectCell extends ListCell<VehicleDAO> {
        @Override
        public void updateItem(VehicleDAO item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null){
                String url = item.getImg();
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
    
    private void updateSelectedVehicle(VehicleDAO vehicle){
        
        connectorDAO.setSelectedVehicle(vehicle);
        
        //Information
        txt_vehicle_name.setText(""+vehicle.getCpsCar().getVehicle());
        iv_vehicle_thumbnail.setImage(new Image(vehicle.getImg()));
        
        //Control
        txt_control_parameter_offset.setText(""+vehicle.getCpsCar().getOffset());
        txt_control_parameter_speed.setText(""+vehicle.getCpsCar().getSpeed());
        
        if(vehicle.getCpsCar().getVehicle().getAdvertisement().isCharging())
            txt_control_parameter_battery.setText("Charging");
        else
            txt_control_parameter_battery.setText("Not Charging");
        
        ///Update list of behaviors
        List<String> behaviors = vehicle.getCurrentBehaviors();
        lv_current_behaviors.getItems().clear();
        lv_current_behaviors.getItems().add(Parameter.BEHAVIOR_CONNECTED);
        for (String behavior: behaviors){
            lv_current_behaviors.getItems().add(behavior);
        }       
    }
    
    public void rescan(){
        this.connectorDAO.reconnect();
        this.connectorDAO.scanVehicles();
        this.connectorDAO.updateVehicles();
        
        //GUI
        txt_vehicle_name.setText("Select a vehicle");
        iv_vehicle_thumbnail.setImage(null);
        if(lv_vehicles.getSelectionModel().getSelectedItems()!=null)
            lv_vehicles.getSelectionModel().clearSelection();
        lv_vehicles.getItems().removeAll();
        observable_list_vehicles = FXCollections.observableList(connectorDAO.getVehicles());
        lv_vehicles.setItems(observable_list_vehicles);
    }
    
    private void adjustSpeed(boolean increase){
        if (connectorDAO.getSelectedVehicle()==null){
            showPopup(Parameter.MESSAGE_NO_SELECTED_VEHICLE);
            return;
        }
        
        if (increase){
            connectorDAO.getSelectedVehicle().increaseSpeed(Parameter.SPEED_ADJUST);
            System.out.println("Increase Speed >>> "+connectorDAO.getSelectedVehicle().getCpsCar().getSpeed());
        }
        else{           
            connectorDAO.getSelectedVehicle().decreaseSpeed(Parameter.SPEED_ADJUST);
            System.out.println("Decrease Speed <<< "+connectorDAO.getSelectedVehicle().getCpsCar().getSpeed());
        }      
    }
    
    private void removeBehavior(ListCell<String> listCell) {
        if (connectorDAO.getSelectedVehicle()==null){
            showPopup(Parameter.MESSAGE_NO_SELECTED_VEHICLE);
            return;
        }
        if (listCell.getItem().equals(Parameter.BEHAVIOR_CONNECTED)){
            showPopup(Parameter.MESSAGE_NO_ADJUST_CONNECTED_BEHAVIOR);
            return;
        }
        if (lv_current_behaviors.getItems().size()>1){
            System.out.println("Remove -- "+listCell.getItem());
            connectorDAO.getSelectedVehicle().removeCurrentBehaviors(""+listCell.getItem());
            connectorDAO.stopBehavior(""+listCell.getItem());
            lv_current_behaviors.getItems().remove(listCell.getItem());
        }
    }
    
    private void addBehavior(){
        if (connectorDAO.getSelectedVehicle()==null){
            showPopup(Parameter.MESSAGE_NO_SELECTED_VEHICLE);
            return;
        }
        TreeCell<String> dragSourceCell = dragSource.get();
        if(!lv_current_behaviors.getItems().contains(dragSourceCell.getItem())){
            System.out.println("Add behavior ++ "+dragSourceCell.getItem());
            switch (""+dragSourceCell.getItem()){
                case Parameter.BEHAVIOR_EMERGENCY_STOP:
                    showPopup(Parameter.BEHAVIOR_EMERGENCY_STOP+" has been performing");
                    break;
                case Parameter.BEHAVIOR_PULL_OVER:
                    showPopup(Parameter.BEHAVIOR_PULL_OVER+" has been performing");
                    break;
                case Parameter.BEHAVIOR_CHANGE_LANE:
                    showPopup(Parameter.BEHAVIOR_CHANGE_LANE+" has been performing");
                    break;  
                case Parameter.BEHAVIOR_U_TURN:
                    showPopup(Parameter.BEHAVIOR_U_TURN+" has been performing");
                    break; 
                default:
                    connectorDAO.getSelectedVehicle().addCurrentBehaviors(""+dragSourceCell.getItem());
                    lv_current_behaviors.getItems().add(dragSourceCell.getItem());
            }
            connectorDAO.performBehavior(""+dragSourceCell.getItem());                             
        }
    }
    
    
    public void showPopup(String message){
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this.stage);
        
        VBox dialogVbox = new VBox(Parameter.BOX_VGAP);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setId("message-vbox");
        
        Text txt_message = new Text(message);
        txt_message.setId("message-text");
        dialogVbox.getChildren().add(txt_message);
        
        Button btn_message = new Button("OK");
        btn_message.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
               dialog.close();
            }
        });
        dialogVbox.getChildren().add(btn_message);
        
        Scene dialogScene = new Scene(dialogVbox, Parameter.WIDTH_SCENE_POPUP, Parameter.HEIGHT_SCENE_POPUP);
        dialogScene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }
    
    class UpdateRealTimeData extends Thread{
        boolean status = true;
        @Override
        public void run(){
            while(status){
                if (connectorDAO.getSelectedVehicle() == null){
                    txt_control_parameter_speed.setText("Not detected");
                    txt_control_parameter_offset.setText("Not detected");
                    txt_control_parameter_battery.setText("Not detected");                   
                }
                else{
                    txt_control_parameter_offset.setText(""+connectorDAO.getSelectedVehicle().getCpsCar().getOffset());
                    txt_control_parameter_speed.setText(""+connectorDAO.getSelectedVehicle().getCpsCar().getSpeed());

                    if(connectorDAO.getSelectedVehicle().getCpsCar().getVehicle().getAdvertisement().isCharging())
                        txt_control_parameter_battery.setText("Charging");
                    else
                        txt_control_parameter_battery.setText("Not Charging");
                }
                try{
                    Thread.sleep(100);
                }catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        public void exit(){
            this.status = false;
        }
    }
    
    public void disconnect(){
        try{
            System.out.println("Disconnenct");
            updateRealTimeData.exit();
            updateRealTimeData.join();
            connectorDAO.disconnect();  
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void scanTrack(){
        int n = connectorDAO.getVehicles().size();
        Boolean[] check = new Boolean[n];
        boolean finished = false;
        for (int i=0;i<n;i++){
            check[i] = false;
        }
        while(!finished){
            for(int i=0;i<n;i++){
                if(check[i])
                    continue;
                CPSCar c = connectorDAO.getVehicles().get(i).getCpsCar();
                System.out.println(c.getAddress() + " Scanning... "+i);
                if (c.scanDone()) {
                    finished = true;
                    check[i] = true;
                    System.out.println(c.getAddress() + ": Scan Done... ");
                    for (RoadmapManager rm : roadmapManagers) {
                        if (c.getMap().equals(rm.getMap())) {
                            System.out.println("Same manager...");
                            c.setRoadmapMannager(rm);
                        }
                    }
                    if (c.getManager() == null) {
                        System.out.println("New manager...");
                        RoadmapManager rm = new RoadmapManager(c.getMap(), c.getReverse(), c.getPieceIDs(), c.getReverses());
                        roadmapManagers.add(rm);
                        c.setRoadmapMannager(rm);
                        rm.setID(roadmapManagers.indexOf(rm));
                    }
                }
                else
                    finished = false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ControlGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            //All vehicles are completed
            System.out.println("GUI - SCAN FULLY COMPLETED");
            System.out.println("GUI - RoadmapManager Size "+roadmapManagers.size());
            this.scanTrackComplete = true;
    }
    
    public void setTrack(List<Block> track){
        MapDAO map = new MapDAO();
        map.setTracks(track);
        map.printBoard();
        maps.add(map);
            
            
        //mapDAO = new MapDAO();
        //mapDAO.setTracks(connectorDAO.getVehicles().get(0).getCpsCar().getMap().getTrack());
        //mapDAO.printBoard();
        this.drawTrack();
    }
    
    public void drawTrack(){
        System.out.println("[GUI] Draw Track");
        
        Text txt_scan = new Text("This is the map");
        txt_scan.setTextAlignment(TextAlignment.CENTER);
        txt_scan.setWrappingWidth(400);
        txt_scan.setId("heading1-text");
        vbox_map.getChildren().add(txt_scan);   
        
        String[][] dummy = this.dummyMap();
        
        for(int k=0;k<2;k++){
            VBox map_GUI = new VBox();
            map_GUI.setAlignment(Pos.CENTER);
            int rows = dummy.length;
            int cols = dummy[0].length;
            int size_piece = 300/rows;

            for (int i=0;i<rows;i++){
                HBox one_row_map = new HBox();
                one_row_map.setAlignment(Pos.CENTER);
                for (int j=0;j<cols;j++){

                    if (dummy[i][j]==null)
                        dummy[i][j] = "null";
                    System.out.println(dummy[i][j]);
                    ImageView iv_road_piece = new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Track/"+dummy[i][j]+".png"));
                    //ImageView iv_road_piece = new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Track/arrow-up.png"));
                    iv_road_piece.setFitHeight(size_piece);
                    iv_road_piece.setPreserveRatio(true);
                    iv_road_piece.setSmooth(true);
                    iv_road_piece.setCache(true);
                    one_row_map.getChildren().add(iv_road_piece);
                }
                map_GUI.getChildren().add(one_row_map);
            }
            vbox_map.getChildren().add(map_GUI); 
        }
        System.out.println("GUI - Draw Done");
        
    }
    
    public String[][] dummyMap(){
        //Create Board
        //int rows = 4;
        //int cols = 4;
        //String[][] board = new String[rows][cols];  
        String[][] board = {
            {"null",    "SE",   "SW",   "null", "null"},
            {"null",    "SV",   "NE",   "SW",   "null"},
            {"null",    "NE",   "SH",   "IN",   "SW",},
            {"SE",      "IN",   "SH",   "NW",   "SV"},
            {"NE",      "IN",   "SH",   "SF",   "NW"}
        };
        
        /*board[0][0] = "SE";
        board[0][1] = "IN";
        board[0][2] = "IN";
        board[0][3] = "SW";
        
        board[1][0] = "SV";
        //board[1][1] = null;
        //board[1][2] = null;
        board[1][3] = "SV";
        
        board[2][0] = "SV";
        //board[2][1] = null;
        //board[2][2] = null;
        board[2][3] = "SV";
        
        board[3][0] = "NE";
        board[3][1] = "SH";
        board[3][2] = "SF";
        board[3][3] = "NW";*/
        
        return board;
    }
    
    class ScanTrackThread extends Thread{
        @Override
        public void run(){
            scanTrack();
        }
    } 
}
