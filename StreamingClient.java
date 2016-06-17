/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streamingclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class StreamingClient extends Application {
    final String path = "C://Users//Rafael//Desktop//airo//StreamingClient//t_video232933488259498019.mp4";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    //the entire window is called "stage"
    //aka "JFrame" in swing
    
    //the content inside the stage is the scene
    //aka "JPanel" in swing

    /**
     * Our main JavaFX application runs on this method.
     * The "launch" method invokes this one.
     * @param primaryStage
     * @throws Exception 
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //creating a stackpane for this scene
        StackPane root = new StackPane();
        //creating the window
        primaryStage.setTitle("Window title");
        
        //creating a Media object that holds the media to be played
        Media m = new Media("file:///" + path);
        //creating a MediaPlayer object that controls the created media
        MediaPlayer mp = new MediaPlayer(m);
        //creating a mediaView object that displays the media
        MediaView mv = new MediaView(mp);
        
        //adding the created mediaView to the stackpane
        root.getChildren().add(mv);
        
        Scene sc = new Scene(root, Color.BLACK);
        
        primaryStage.setMinHeight(720);
        primaryStage.setMinWidth(1280);
        
        primaryStage.setScene(sc);
        //showing my stage
        primaryStage.show();
        mp.play();
    }

}
