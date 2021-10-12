package edu.oswego.cs.CPSLab.AutomotiveCPS.gui;

import java.awt.*;

import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.ConnectorDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.MapDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.VehicleDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.RoadmapManager;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class MapGUI extends Application {

    private Scene scene;
    private Stage stage;
    private GridPane grid;
    private VBox vbox_map;
    private ConnectorDAO connectorDAO;
    private HashMap<ImageView,Block> map_iv_block = new HashMap<>();
    private boolean join_intersection_flag = false;
    private Block temp_intersection;
    private RoadmapManager temp_roadmap_manager;
    private ImageView temp_iv_intersection;
    private List<ImageView> list_iv_intersection = new ArrayList<>();
    private String[] join_intersection_colors = {"yellow", "green", "blue"};
    private final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private final double RESOLUTION_HEIGHT = SIZE.getHeight();
    private final double RESOLUTION_WIDTH  = SIZE.getWidth();
    private int sidePanelWidth;
    private VBox map_GUI;
    private List<Integer[][]> trackPieceImageCenter = new ArrayList<>();
    private List<Integer[]> trackPieceIDs = new ArrayList<>();
    private int[] startingCoordinate = new int[2];

    public MapGUI(ConnectorDAO connectorDAO) {
        this.connectorDAO = connectorDAO;
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Starting GUI");
        stage.setResizable(false);
        GridPane grid = new GridPane();
        this.grid = grid;
        grid.setId("control-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(Parameter.GRID_HGAP_CONTROL);
        grid.setVgap(Parameter.GRID_VGAP);
        grid.setPadding(new Insets(Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING,Parameter.SIZE_PADDING));
        grid.setGridLinesVisible(true);

        //***************************************************
        //*********************** Map ***********************
        //***************************************************

        this.vbox_map = new VBox();
        this.vbox_map.setAlignment(Pos.TOP_CENTER);
        this.vbox_map.setSpacing(Parameter.BOX_VGAP);

        Text txt_map = new Text("Map");
        txt_map.setTextAlignment(TextAlignment.CENTER);
        txt_map.setId("map-heading-text");
        vbox_map.getChildren().add(txt_map);

        grid.add(vbox_map, 0,0);
        drawTrack();

        Scene scene = new Scene(grid, RESOLUTION_WIDTH, RESOLUTION_HEIGHT);
        scene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Map View");
        this.scene = scene;


        for (Integer[] integers : trackPieceImageCenter.get(0)) {
            System.out.println(Arrays.toString(integers));
        }
        //stage.show();
    }

    public Scene getScene() { return scene; }

    public GridPane getGrid() { return grid; }

    public List<Integer[][] > getImageCenters() { return trackPieceImageCenter; }

    public void stop() {
        stage.close();
    }

    public void drawTrack(){

        System.out.println("GUI - Draw Track");
        List<MapDAO> maps = this.connectorDAO.getMaps();
        for (MapDAO map : maps) {
            String[][] board = map.getBoard();
            map_GUI = new VBox();
            map_GUI.setAlignment(Pos.CENTER);
            int rows = board.length;
            int cols = board[0].length;
            //GUI track size
            int padding = 200;
            int size_piece = (int) (RESOLUTION_HEIGHT - padding) / rows;
            this.sidePanelWidth = (int) (RESOLUTION_WIDTH - (size_piece * cols)) / 2;
            this.startingCoordinate[0] = sidePanelWidth;
            this.startingCoordinate[1] = 100;
            trackPieceImageCenter.add(new Integer[cols * rows][2]);
            trackPieceIDs.add(new Integer[cols * rows]);
            for (int i = 0; i < rows; i++) {
                HBox one_row_map = new HBox();
                one_row_map.setAlignment(Pos.CENTER);
                for (int j = 0; j < cols; j++) {

                    if (board[i][j] == null) {
                        board[i][j] = "null";
                    }
                    if (board[i][j].equals("SF")) {
                        map.addStartingLocation(new Integer[]{i, j});
                    }
                    // draws track with pictures
                    ImageView iv_road_piece = new ImageView(new Image(Parameter.PATH_MEDIA + "Track/" + board[i][j] + ".png"));
                    //ImageView iv_road_piece = new ImageView(new Image("edu/oswego/cs/CPSLab/AutomotiveCPS/gui/img/Track/arrow-up.png"));
                    iv_road_piece.setId("#testing-image");
                    iv_road_piece.setFitHeight(size_piece);
                    iv_road_piece.setPreserveRatio(true);
                    iv_road_piece.setSmooth(true);
                    iv_road_piece.setCache(true);
                    if (board[i][j].equals("IN")) {
                        initializeIntersectionPiece(iv_road_piece, map, i, j);
                    }
                    one_row_map.getChildren().add(iv_road_piece);
                }
                map_GUI.getChildren().add(one_row_map);
            }
            vbox_map.getChildren().add(map_GUI);
            int currentXPos = startingCoordinate[0] + (size_piece / 2);
            int currentYPos = startingCoordinate[1] + (size_piece / 2);

            trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[0][0] = currentXPos;
            trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[0][1] = currentYPos;



            int arrayIndex = 1;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols - 1; j++) {
                    // Only adding half of a box each time, make the currentXPos starting at startingCoordinate[0] + size_piece / 2, and then in the for loop
                    // add currentXPos += size_piece
                    currentXPos += size_piece;
                    trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[arrayIndex][0] = currentXPos;
                    trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[arrayIndex][1] = currentYPos;
                    arrayIndex += 1;
                }
                currentYPos += size_piece;
                currentXPos = startingCoordinate[0] + (size_piece / 2);
                if (arrayIndex < trackPieceImageCenter.get(trackPieceImageCenter.size() - 1).length) {
                    trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[arrayIndex][0] = currentXPos;
                    trackPieceImageCenter.get(trackPieceImageCenter.size() - 1)[arrayIndex][1] = currentYPos;
                }
                arrayIndex += 1;
            }

        }

        System.out.println("GUI - Draw Done");
    }

    public void initializeIntersectionPiece(ImageView iv_road_piece,MapDAO map,int i,int j){
        iv_road_piece.setId("road-piece-image-view");
        this.map_iv_block.put(iv_road_piece, map.getBlock(i, j));
        iv_road_piece.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Block block = map_iv_block.get(iv_road_piece);
                    RoadmapManager roadmapManager = connectorDAO.getRoadmapManager(block);
                    System.out.println("Intersection: " + block.toString());
                    System.out.println("RoadmapManager: " + roadmapManager.toString());

                    if(!join_intersection_flag){
                        handleFirstJointIntersection(block,roadmapManager,iv_road_piece);
                    }
                    else{
                        handleSecondJointIntersection(block,roadmapManager,iv_road_piece);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void handleFirstJointIntersection(Block block,RoadmapManager roadmapManager,ImageView iv_road_piece){
        if (list_iv_intersection.contains(iv_road_piece)){
            return;
        }

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this.stage);

        VBox dialogVbox = new VBox(Parameter.BOX_VGAP);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setId("message-vbox");

        Text txt_message = new Text("Do you want to join intersections?");
        txt_message.setId("message-text");
        dialogVbox.getChildren().add(txt_message);

        HBox hbox_button = new HBox();
        hbox_button.setAlignment(Pos.CENTER);
        hbox_button.setSpacing(Parameter.BOX_HGAP);

        Button btn_yes = new Button("Yes");
        btn_yes.setOnAction(new EventHandler<ActionEvent>() {
            @Override

            public void handle(ActionEvent e) {
                join_intersection_flag = true;
                temp_intersection = block;
                temp_roadmap_manager = roadmapManager;
                temp_iv_intersection = iv_road_piece;
                temp_iv_intersection.setStyle("-fx-effect: innershadow(three-pass-box, "+join_intersection_colors[list_iv_intersection.size()/2]+", 10, 10, 0, 0);");
                dialog.close();
            }
        });
        hbox_button.getChildren().add(btn_yes);

        Button btn_no = new Button("No");
        btn_no.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                dialog.close();
            }
        });
        hbox_button.getChildren().add(btn_no);
        dialogVbox.getChildren().add(hbox_button);

        Scene dialogScene = new Scene(dialogVbox, Parameter.WIDTH_SCENE_POPUP, Parameter.HEIGHT_SCENE_POPUP);
        dialogScene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public void handleSecondJointIntersection(Block block,RoadmapManager roadmapManager,ImageView iv_road_piece){
        if (!join_intersection_flag || temp_intersection == null || temp_roadmap_manager == null){
            resetTempIntersection(false);
            return;
        }
        if (block == temp_intersection){
            resetTempIntersection(false);
            return;
        }
        if (roadmapManager == temp_roadmap_manager){
            resetTempIntersection(false);
            return;
        }
        if (list_iv_intersection.contains(iv_road_piece)){
            resetTempIntersection(false);
            return;
        }
        System.out.println("--- Joining two intersections ---");
        System.out.println("Intersection 1: "+temp_intersection.toString());
        System.out.println("RoadmapManager 1: "+temp_roadmap_manager.toString());
        System.out.println("Intersection 2: "+block.toString());
        System.out.println("RoadmapManager 2: "+roadmapManager.toString());
        iv_road_piece.setStyle("-fx-effect: innershadow(three-pass-box, "+join_intersection_colors[list_iv_intersection.size()/2]+", 10, 10, 0, 0);");
        connectorDAO.joinIntersection(temp_intersection,temp_roadmap_manager,block,roadmapManager);

        this.list_iv_intersection.add(temp_iv_intersection);
        this.list_iv_intersection.add(iv_road_piece);


        resetTempIntersection(true);

    }

    public void resetTempIntersection(boolean success){
        if(!success){
            temp_iv_intersection.setStyle("-fx-effect: None;");
        }
        join_intersection_flag = false;
        temp_intersection = null;
        temp_roadmap_manager = null;
        temp_iv_intersection = null;
    }

}
