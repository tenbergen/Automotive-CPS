/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import Controller.ConnectorDAO;
import Controller.VehicleDAO;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author HN
 */
public class ControlGUI extends Application {
    
    private Stage stage;
    private ConnectorDAO connectorDAO;
  
    private ListView<VehicleDAO> lv_vehicles = new ListView<>();
    
    //Component of information section
    Text txt_vehicle_name;
    Text txt_vehicle_desc;
    ImageView iv_vehicle_thumbnail;
    
    //Component of control section
    Text txt_control_parameter_offset;
    Text txt_control_parameter_speed;
    Text txt_control_parameter_battery;
    Text txt_control_road_type;
    
    //Behavior Drag & Drop 
    private final ObjectProperty<TreeCell<String>> dragSource = new SimpleObjectProperty<>();
    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
    private TreeItem<String> draggedItem;
    private ListView<String> lv_current_behaviors;
    
    @Override
    public void stop(){
        System.out.println("Stage is closing");
        connectorDAO.getAnkiConnector().close();
    }
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        GridPane grid = new GridPane();
        grid.setId("control-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(Parameter.GRID_HGAP);
        grid.setVgap(Parameter.GRID_VGAP);
        grid.setPadding(new Insets(Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING));
        //grid.setGridLinesVisible(true);
    
        //***************************************************
        //******************** Selection ********************
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
        connectorDAO.updateVehicles();
        
        lv_vehicles.setId("vehicle-listview");      
        lv_vehicles.setPrefHeight(Parameter.HEIGHT_LIST_CAR);
        lv_vehicles.setPrefWidth(Parameter.WIDTH_LIST_CAR);
        lv_vehicles.setCellFactory((ListView<VehicleDAO> l) -> new ColorRectCell());
        lv_vehicles.setItems(connectorDAO.getVehicles());
        
        //Handle select item
        lv_vehicles.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VehicleDAO>() {

            @Override
            public void changed(ObservableValue<? extends VehicleDAO> observable, VehicleDAO oldValue, VehicleDAO newValue) {
                // Action here
                System.out.println("Selected vehicle: " + newValue.getCpsCar().getVehicle().getAdvertisement().getIdentifier());
                updateSelectedVehicle(newValue);
            }
        });       
        hbox_selection.getChildren().add(lv_vehicles);
        hbox_selection.getChildren().add(vbox_vehicle);
        grid.add(hbox_selection,0,0);
        
        //***************************************************
        //*********************** Map ***********************
        //***************************************************
        /*VBox vbox_map = new VBox();
        vbox_map.setAlignment(Pos.CENTER);
        vbox_map.setStyle("-fx-background-color: white;");
        
        Text txt_map = new Text("Map");
        txt_map.setId("heading1-text");
        vbox_map.getChildren().add(txt_map);
        
        Rectangle rec_map = new Rectangle();
        rec_map.setX(50);
        rec_map.setY(50);
        rec_map.setWidth(300);
        rec_map.setHeight(200);
        rec_map.setStyle("-fx-background-color: white;");
        vbox_map.getChildren().add(rec_map);
        
        grid.add(vbox_map, 1,0);*/
        
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
        ImageView iv_control_parameter_offset= new ImageView(new Image("GUI/img/Icon/offset.png"));
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
        ImageView iv_control_parameter_speed= new ImageView(new Image("GUI/img/Icon/speed.png"));
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
        ImageView iv_control_parameter_battery= new ImageView(new Image("GUI/img/Icon/battery.png"));
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
        
        //Vehicle icon
        ImageView iv_control_vehicle= new ImageView(new Image("GUI/img/Icon/vehicle_control.png"));
        iv_control_vehicle.setFitWidth(Parameter.SIZE_ICON_BIG);
        iv_control_vehicle.setPreserveRatio(true);
        iv_control_vehicle.setSmooth(true);
        iv_control_vehicle.setCache(true);
        vbox_control_vehicle.getChildren().add(iv_control_vehicle);
               
        //Road status
        txt_control_road_type = new Text("Not Detected");
        txt_control_road_type.setId("road-type-text");
        vbox_control_vehicle.getChildren().add(txt_control_road_type);
        
        vbox_control.getChildren().add(vbox_control_vehicle);
        
        //Adjust box
        HBox hbox_control_adjust = new HBox();
        hbox_control_adjust.setAlignment(Pos.CENTER);
        hbox_control_adjust.setSpacing(Parameter.ICON_HGAP);
        
        //Adjust speed box
        VBox vbox_control_adjust_speed = new VBox();
        vbox_control_adjust_speed.setAlignment(Pos.CENTER);
        vbox_control_adjust_speed.setSpacing(Parameter.COMPONENT_VGAP);
        
        //Adjust speed arrow - UP        
        ImageView iv_control_adjust_speed_up = new ImageView(new Image("GUI/img/Icon/arrow-up.png"));
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
        vbox_control_adjust_speed.getChildren().add(btn_control_adjust_speed_up);
        
        //Adjust speed text
        Text txt_control_adjust_speed = new Text("Speed");
        txt_control_adjust_speed.setId("label-text");   
        vbox_control_adjust_speed.getChildren().add(txt_control_adjust_speed);
        
        //Adjust speed arrow - DOWN       
        ImageView iv_control_adjust_speed_down = new ImageView(new Image("GUI/img/Icon/arrow-down.png"));
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
        vbox_control_adjust_speed.getChildren().add(btn_control_adjust_speed_down);
        
        hbox_control_adjust.getChildren().add(vbox_control_adjust_speed);
        
        //Steering wheel
        ImageView iv_control_adjust_steering_wheel= new ImageView(new Image("GUI/img/Icon/steering-wheel.png"));
        iv_control_adjust_steering_wheel.setFitHeight(Parameter.SIZE_ICON_MEDIUM);
        iv_control_adjust_steering_wheel.setPreserveRatio(true);
        iv_control_adjust_steering_wheel.setSmooth(true);
        iv_control_adjust_steering_wheel.setCache(true);
        
        hbox_control_adjust.getChildren().add(iv_control_adjust_steering_wheel);
              
        vbox_control.getChildren().add(hbox_control_adjust);
                 
        grid.add(vbox_control, 0,1);
        
        
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
                System.out.println("setOnDragOver"); 
                
                // can't drop on itself
                if (draggedItem == null || thisItem == null || thisItem == draggedItem) 
                    return;
                
                // ignore if this is the root
                if (draggedItem.getParent() == null || draggedItem.getParent().getParent() == null)
                    return;

                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    System.out.println("setOnDragOver");
                }
                
            });

            treeCell.setOnDragDone(event -> {
                System.out.println("TreeCell: setOnDragDone");           
             });

            treeCell.setOnDragDropped(event -> {
                System.out.println("TreeCell: setOnDragDropped");  
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
        lv_current_behaviors.getItems().add("Running");
        
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
                    System.out.println("listCell: setOnDragDetected");
                }
            });

            listCell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    System.out.println("listCell: setOnDragOver");
                }
            });

            listCell.setOnDragDone(event -> {
                removeBehavior(listCell);
                System.out.println("listCell: setOnDragDone");           
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
                System.out.println("listCell: setOnDragDropped");   
            });

            return listCell;
        });
        vbox_current_behavior.getChildren().add(lv_current_behaviors);
              
        hbox_behavior_drag_drop.getChildren().add(vbox_current_behavior);        
        
        vbox_behavior.getChildren().add(hbox_behavior_drag_drop);
        
        grid.add(vbox_behavior, 1,1);
        
        
        //***************************************************
        //###################### SCENE ######################
        //***************************************************
        
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
                System.out.println(url);
                Image img = new Image(url);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(60);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.setCache(true);
                setGraphic(iv);
            }
 
        }
    }
    
    private void updateSelectedVehicle(VehicleDAO vehicle){
        
        connectorDAO.setSelectedVehicle(vehicle);
        
        //Information
        txt_vehicle_name.setText(""+vehicle.getCpsCar().getVehicle());
        txt_vehicle_desc.setText("Kind: "+vehicle.getCpsCar().getVehicle().getAdvertisement().getModel());
        iv_vehicle_thumbnail.setImage(new Image(vehicle.getImg()));
        
        //Control
        txt_control_parameter_offset.setText(""+vehicle.getCpsCar().getOffset());
        txt_control_parameter_speed.setText(""+vehicle.getCpsCar().getSpeed());
        
        if(vehicle.getCpsCar().getVehicle().getAdvertisement().isCharging())
            txt_control_parameter_battery.setText("Charging");
        else
            txt_control_parameter_battery.setText("Not Charging");
        
        //txt_control_road_type.setText(""+vehicle.getCpsCar().getMap());
        
    }
    
    private void adjustSpeed(boolean increase){
        if (increase){
            System.out.println("Increase Speed >>> ");
            showPopup("Hello");
            //Increase Speed
            
            /*VehicleDAO vehicle = connectorDAO.getSelectedVehicle();
            
            vehicle.pullOver();
            
            
            PullOver pullOver = new PullOver(vehicle.getCpsCar());
            pullOver.run();
            
            System.out.println("Done");*/
            
            
        }
        else{
            System.out.println("Decrease Speed <<< ");
            //Decrease Speed
        }      
    }
    
    private void removeBehavior(ListCell<String> listCell) {
        if (connectorDAO.getSelectedVehicle()==null){
            showPopup(Parameter.MESSAGE_NO_SELECTED_VEHICLE);
            return;
        }
        if (lv_current_behaviors.getItems().size()>1){
            System.out.println("Remove -- "+listCell.getItem());
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
            lv_current_behaviors.getItems().add(dragSourceCell.getItem());
            System.out.println("Add ++ "+dragSourceCell.getItem());
            connectorDAO.performBehavior(""+dragSourceCell.getItem());            
        }
    }
    
    
    public void showPopup(String message){
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this.stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text(message));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
