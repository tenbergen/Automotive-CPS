/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.sql.Connection;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author HN
 */
public class HelloWorldGUI extends Application {

    //Gap
    private final int size_spacing = 10;
    private final int size_padding = 20;
    
    private final int width_scene = 600;
    private final int height_scene = 400;
    
    @Override
    public void start(Stage primaryStage) {
      
        GridPane grid = new GridPane();
        //grid.setId("connection-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(size_padding, size_padding, size_padding, size_padding));
        
        //grid.setGridLinesVisible(true);
        
        //Title
        Text scenetitle = new Text("Automotive CPS");
        //scenetitle.setId("title-text");
        grid.add(scenetitle, 0, 0, 2, 1);
       
               
        Scene scene = new Scene(grid, width_scene, height_scene);
        //scene.getStylesheets().add(Connection.class.getResource("design-style.css").toExternalForm());
                
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("CPS Connection");
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
