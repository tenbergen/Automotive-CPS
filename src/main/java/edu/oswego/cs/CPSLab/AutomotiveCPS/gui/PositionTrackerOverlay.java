package edu.oswego.cs.CPSLab.AutomotiveCPS.gui;

import java.awt.*;

import de.adesso.anki.MessageListener;
import de.adesso.anki.messages.LocalizationTransitionUpdateMessage;
import de.adesso.anki.roadmap.roadpieces.StartRoadpiece;
import edu.oswego.cs.CPSLab.AutomotiveCPS.CPSCar;
import edu.oswego.cs.CPSLab.AutomotiveCPS.behavior.Speedometer;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.MapDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.controller.VehicleDAO;
import edu.oswego.cs.CPSLab.AutomotiveCPS.map.Block;
import edu.oswego.cs.CPSLab.AutomotiveCPS.utilities.Customized2DArray;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * @author Gregory Maldonado
 * @since 07-15-2021
 *
 * JavaFX Application for displaying the MapGUI and placing JavaFX.Circles for tracking each vehicle on the track in real time
 */
public class PositionTrackerOverlay extends Application {

    // DAOs
    private VehicleDAO vehicleSelected;
    private MapDAO mapSelected;

    // Lists
    private List<VehicleDAO> vehicles;
    private List<MapDAO> mapDAOs;
    private List<MultithreadedTracker> vehicleTrackers = new ArrayList<>();
    private List<Thread> vehicleThreads = new ArrayList<>();
    private List<Integer[][]> listOfRoadPieceCenters;
    private List<Block> blocks;
    private static List<Integer> trackID;
    private Integer[][][] formatedArray;
    private List<Integer[][]> listOfFormatedRoadPieceCenters;

    // Javafx components
    private GridPane mapGridPane;
    private StackPane entireMapPane = new StackPane();
    private Pane circleTrackingPane = new Pane();

    private Integer[][] roadPieceCenters;

    private boolean killThreads = false;
    private final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();


    public PositionTrackerOverlay(List<VehicleDAO> vehicles, List<MapDAO> mapDAOs, GridPane mapGrid,  List<Integer[][]> roadPieceCenters) {
        this.mapGridPane            = mapGrid;
        this.vehicles               = vehicles;
        this.mapDAOs                = mapDAOs;
        this.listOfRoadPieceCenters = roadPieceCenters;
        this.roadPieceCenters       = roadPieceCenters.get(0);

        /*
         * Note to developer : List<MapDAO> mapDAO should only contain 1 mapDAO. If there are other instances nearby and
         * the connectorDAO picks up another set of cars on a different map then that map will be stored in the mapDAO. If there are other maps
         * contained in the mapDAO, the position tracker will not work. This is because, I'm using the first mapDAO in the list, List<MapDAO>.index(0)
         * -GM
         * */

        if (mapDAOs.size() != 0) this.mapSelected = mapDAOs.get(0);
        assert mapSelected != null;
        blocks = mapSelected.getTracks();

        trackID = blocks.stream()
                .map(Block::getPieceId)
                .collect(Collectors.toList());

        debug();


        //rearrangeArray();
    }

    private void debug() {
        System.out.println(" RUNNING DEBUG MODE ");

        System.out.println(Arrays.deepToString(mapSelected.getBoard()));
        System.out.println(Arrays.toString(getRotatedMap()));

    }

    private String[] getRotatedMap() {
        String[][] board = mapSelected.getBoard();
        int rows         = board.length;
        int cols         = board[0].length;

        List<String> modifiedBoard = new ArrayList<>();
        String[] rotatedBoard = new String[rows * cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                modifiedBoard.add(board[i][j]);
            }
        }

        int SFindex = modifiedBoard.indexOf("SF");
        int spacing = modifiedBoard.size() - SFindex;

        for (int i = 0; i < modifiedBoard.size(); i++) {
            if (i < SFindex) {
                int index = i + spacing;
                rotatedBoard[index] = modifiedBoard.get(i);
            } else {
                int index = i - SFindex;
                rotatedBoard[index] = modifiedBoard.get(i);
            }
        }
        return rotatedBoard;
    }

    public void setBlocks(List<Block> blocks) { this.blocks = blocks; }

    /**
     * @return Boolean if a vehicle is selected or not
     */
    private boolean isVehicleSelected() { return ! (vehicleSelected == null) ; }

    /**
     * Each time a new vehicle is selected, the method is called to change the selected vehicle within the PositionTrackerOverlay class.
     * TODO: This function is useful for when a vehicle is selected, only show that circle tracker on the GUI
     *
     * @param vehicleSelected VehicleDAO that is selected within the ControlGUI
     */
    public void setVehicleSelected(VehicleDAO vehicleSelected) {
        this.vehicleSelected = vehicleSelected;
        System.out.println("new Vehicle selected to track");
    }

    /**
     * Kills all threads controlling trackers all at once
     */
    public void killTrackers() {
        vehicleThreads.forEach(Thread::interrupt);
        killThreads = true;
    }

    /**
     * turns one-dimesional array into two-dimensitional
     */
    private void rearrangeArray() {
        Integer[][] array = listOfRoadPieceCenters.get(0);
        String[][] board = mapSelected.getBoard();
        int counter = 0;
        int rows = board.length;
        int cols = board[0].length;
        formatedArray = new Integer[rows][cols][2];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                formatedArray[i][j][0] = array[counter][0];
                formatedArray[i][j][1] = array[counter][1];
                counter ++ ;
            }
        }

        System.out.println("Array Rotation Method");
        for (Integer[][] row : formatedArray) {
            for (Integer[] col : row) {
                System.out.print(col[0] + ", " + col[1] + " |||");
            }
            System.out.print("\n");
        }
    }



    /**
     * For each vehicle found, create a tracker instance on a new thread.
     * For each tracker, draw a circle to the scene to represent the position on the track at any instance in time.
     * Use of java streams for easy list manipulation.
     */
    private void setupThreads() {
        // creates a new instance of a tracker for each vehicle in the list
        vehicleTrackers = vehicles.stream()
                .map(MultithreadedTracker::new)
                .collect(Collectors.toList());

        // creates a list of threads for each instance of the trackers
        vehicleThreads = vehicleTrackers.stream()
                .map(Thread::new)
                .collect(Collectors.toList());

        // starts all the threads in the list
        vehicleThreads.forEach(Thread::start);

        // adds all of the trackers (javafx.Circles) to the pane
        vehicleTrackers.stream()
                .map( (vehicle) -> vehicle.tracker)
                .collect(Collectors.toList())
                .forEach( (circle) -> {
                    circle.toFront();
                    circleTrackingPane.getChildren().add(circle);
                    System.out.println("Making a new Circle");
                } );
    }

    /**
     * Contains all javafx components/nodes to complete the scene
     * @param stage for the javafx.scene
     */
    private void drawStage(Stage stage) {
        VBox root = new VBox();
        root.setId("#scan-vehicles-grid");
        root.setAlignment(Pos.CENTER);
        entireMapPane.getChildren().addAll(mapGridPane);
        entireMapPane.setId("#scan-vehicles-grid");
        StackPane circleStackPane = new StackPane();
        circleStackPane.getChildren().addAll(entireMapPane, circleTrackingPane);
        circleStackPane.setId("#scan-vehicles-grid");
        root.getChildren().addAll(circleStackPane);
        Scene scene = new Scene(root, SIZE.width, SIZE.height);
        scene.getStylesheets().add(ControlGUI.class.getResource("design-style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    // Javafx application entry point
    @Override
    public void start(Stage stage) throws Exception {
        setupThreads();
        drawStage(stage);
    }

    /**
     * Responsible for collecting tracking data and initializing the javafx.Circle object.
     * The class inherits the Runnable class -> for each vehicle in the list, they each get an instance of the MultithreadedTracker class
     */
    public class MultithreadedTracker implements Runnable {

        private CPSCar cpsCar;
        private Speedometer speedometer;
        private double enteringVelocity;
        private int trackIndex = 0;

        Circle tracker = new Circle();

        public MultithreadedTracker(VehicleDAO vehicle) {
            this.cpsCar      = vehicle.getCpsCar();
            this.speedometer = cpsCar.getSpeedometer();
            tracker.setRadius(10);
            tracker.setFill(Paint.valueOf("#000FFF"));;
            this.cpsCar.setVehicleDAO(vehicle);
        }

        /**
         * When a vehicle transitions to a new piece, their tracker moves to that roadpiece
         *
         * WIP: this tracking function only works if the vehicle starting scanning on the Start RoadPiece :)
         */
        public void transition() {

            // If going backwards
                // Then trackIndex *= -1

            trackIndex += 1;
            if (trackIndex == mapSelected.getTracks().size() - 1) {
                trackIndex = 0;
            }

            System.out.println("CURRENT TRACK " + mapSelected.getTracks().get(trackIndex).getPiece().getType());
            int randomTrack = new Random().nextInt(roadPieceCenters.length);
            updatePosition(roadPieceCenters[randomTrack][0], roadPieceCenters[randomTrack][1] );

            this.debug();
        }

        @Override
        public void run() {

            while (! PositionTrackerOverlay.this.killThreads) {
                this.enteringVelocity = speedometer.getVelocity();
                this.speedometer.setTracker(this);
            }
        }

        // Updates the position of the tracker (javafx.Circle)
        private void updatePosition(int x, int y) {
            tracker.setCenterX(x);
            tracker.setCenterY(y);
            System.out.println(x + " " + y);
        }

        private void debug() {
            System.out.println(trackID.get(trackIndex));
        }

    }
}
