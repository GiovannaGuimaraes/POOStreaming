/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MediaPlayer;

import java.io.File;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Rafael Augusto Monteiro - 9293095
 */
public class MediaPlayer {
    
    private FXMLLoader loader;
    private MediaPlayerController controller;
    private Stage stage;
    private Scene scene;
    
    /**
     *
     * @param f
     */
    public MediaPlayer(File f){
        
        Parent root = null;
        //loads a scene from a FXML file
        this.loader = new FXMLLoader(getClass().getResource("MediaPlayerFXML.fxml"));
        try {root = (Parent)loader.load();} catch(Exception e){;}
        //make a reference to this Scene controller (to pass the media used to the controller)
        this.controller = loader.getController();
        controller.passParameters(f, scene);
        
        //starts up a scene (window panel)
        this.stage = new Stage(StageStyle.DECORATED);
        this.scene = new Scene(root);
        stage.setTitle("BZMP - Bozó Media Player");
        stage.setScene(scene);
        stage.setMinHeight(400);
        stage.setMinWidth(500);
    }
    
    public void show(){
        stage.show();
    }
}
