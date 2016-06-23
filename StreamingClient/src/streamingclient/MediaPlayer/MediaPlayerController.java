package MediaPlayer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 *
 * @author Rafael
 */
public class MediaPlayerController implements Initializable {
    private Scene s;
    private MediaPlayer mp;
    private Media m;
    private long lastSliderRequest;
    
    private final static double MIN_CHANGE = 0.05;
    
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button stopButton;
    
    @FXML
    private MediaView mView;

    @FXML
    private Slider progressSlider;

    @FXML
    private Button playButton;
    
    @FXML
    private HBox bottomBar;
    
    @FXML
    private GridPane gp;
    
    @FXML
    private TilePane tp;
    
    @FXML
    private Polygon playTriangle;
    
    @FXML
    private HBox stopBar;
    
    @FXML
    private Label fileLabel;
    
    @FXML
    private MenuBar menuFullScreen;

    @FXML
    private MenuItem fullScreenButton;

    @FXML
    void handlePlayButton(ActionEvent event) {
        if(mp.getStatus().compareTo(MediaPlayer.Status.PLAYING) == 0){
            mp.pause();
        } else {
            mp.play();
        }
    }

    @FXML
    void handleStopButton(ActionEvent event) {
        mp.stop();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    void passParameters(File f, Scene s) {
        try {
            this.m = new Media(f.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(MediaPlayerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.mp = new MediaPlayer(m);
        this.s = s;
        
        DoubleProperty h = mView.fitHeightProperty();
        DoubleProperty w = mView.fitWidthProperty();
        h.bind(tp.heightProperty());
        w.bind(tp.widthProperty());
        tp.setBackground(Background.EMPTY);
        
        //sets mediaView to have the passed media
        mView.setMediaPlayer(this.mp);
        
        //finds the name of the 
        String[] split = m.getSource().split("/");
        split[split.length-1] = split[split.length-1].replace("%20", " ");
        fileLabel.setText(split[split.length-1]);
        
        /*adds controllers to media events happening at mediaPlayer*/
        //when mediaPlayer is playing, set playButton to "Pause"
        //when media ends, sets mediaPlayer to stop
        mp.setOnPlaying(() -> {
            playTriangle.setVisible(false);
            stopBar.setVisible(true);
        });
        //when mediaPlayer is paused, set playButton to "play"
        mp.setOnPaused(() -> {
            playTriangle.setVisible(true);
            stopBar.setVisible(false);
        });
        //when mediaPlayer is stopped, set playButton to "Play"
        mp.setOnStopped(() -> {
            playTriangle.setVisible(true);
            stopBar.setVisible(false);
        });
        //when media ends, sets mediaPlayer to stop
        mp.setOnEndOfMedia(() -> {
            mp.stop();
            progressSlider.adjustValue(0);
        });  
        
        //whenever the media changes, sets the progressBar to the corresponding position
        mp.currentTimeProperty().addListener((obs,oldValue,newValue) -> {
            //if the slider isnt changing
            if(!progressSlider.isValueChanging()){
                double adjVal = newValue.toMillis()/m.getDuration().toMillis();
                progressSlider.setValue(adjVal*100);
            }
        });
        
        //when the progress slider changes, seeks the mediaPlayer to the corresponding time
        progressSlider.valueProperty().addListener((obs,oldval,newVal) -> {
            //checks if the change is significative
            if(Math.abs(((double)newVal - (double)oldval)/100) > MIN_CHANGE){
                //checks how long it took since the last slider change
                long cur = System.currentTimeMillis();
                if(cur - lastSliderRequest > 500){
                    mp.seek(Duration.millis(((double)newVal/100)*m.getDuration().toMillis()));
                }
            }
        });
        
        //when volumeSlider changes, sets mediaPLayer volume to the corresponding value
        volumeSlider.valueProperty().addListener((obs,oldVal,newVal) -> {
            mp.setVolume((double)newVal/100);
        });
        
        //sets the newly created mediaPlayer to play as soon as possible
        mp.setAutoPlay(true);
    }
    
}
