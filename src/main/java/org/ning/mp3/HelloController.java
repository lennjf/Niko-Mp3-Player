package org.ning.mp3;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {


    @FXML
    public Button playBtn;
    @FXML
    public Button pauseBtn;
    @FXML
    public Button restBtn;
    @FXML
    public Button previousBtn;
    @FXML
    public Button nextBtn;
    @FXML
    public Label face;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public AnchorPane layout;
    @FXML
    public Slider volumeSlider;

    List<File> musicList = new ArrayList<>();

    Media media;
    MediaPlayer mediaPlayer;

    DirectoryChooser chooser;

    Map<String,String> userMap =new HashMap<>();

    String mp3Dir;

    Timer timer;

    TimerTask task;

    File userProperties;

    double volume;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userProperties = new File("user.properties");
        if (!userProperties.exists()){
            try {
                userProperties.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Scanner scanner = new Scanner(userProperties);
            while(scanner.hasNextLine()){
                String string = scanner.nextLine();
                if (string!=null){
                    String[] split = string.split("=");
                    userMap.put(split[0],split[1]);

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (userMap.get(Constant.MP3_DIR)==null){
            chooser = new DirectoryChooser();
            chooser.setTitle("choose user dir");

            File mp3dir = chooser.showDialog(new Stage());
            mp3Dir = mp3dir.getAbsolutePath();
            userMap.put(Constant.MP3_DIR,mp3Dir);
            userMap.put(Constant.MP3_INDEX,"1");
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userProperties)));
                writer.append("mp3dir=" + mp3dir.getAbsolutePath());
                writer.newLine();
                writer.append("mp3index=1");
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            mp3Dir = (String) userMap.get(Constant.MP3_DIR);
        }

        File dir = new File(mp3Dir);
        if (dir.listFiles().length>0){
            listFile(dir);
            faceUpdate();
        }else {
            face.setText("no achieve");
            playBtn.setDisable(true);
            nextBtn.setDisable(true);
            pauseBtn.setDisable(true);
            restBtn.setDisable(true);
            previousBtn.setDisable(true);
        }
        volume = 100;
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(t1.intValue()*0.01);
                volume = t1.doubleValue();
            }
        });

        exitTask();

    }



    public void listFile(File file){
        File[] files = file.listFiles();
        for (File f:files) {
            if (f.isDirectory()){
                if (!f.getName().equals("store")){
                    listFile(f);
                }
            }else {
                musicList.add(f);
            }
        }
    }

    @FXML
    public void playHandler(ActionEvent actionEvent) {
        if (mediaPlayer!=null && !mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)){
            mediaPlayer.play();
            if (timer!=null){
                timer.cancel();
            }
            beginTimer();
            return;
        }
        if (mediaPlayer!=null && mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)){
            return;
        }
        media = new Media(musicList.get(Integer.valueOf(userMap.get(Constant.MP3_INDEX))-1).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(((int)volume)*0.01);
        mediaPlayer.setOnEndOfMedia(()->{
            System.out.println("END");
            updateIndex(UpdateIndexEnum.PLUS);
        });
        mediaPlayer.play();
        if (timer!=null){
            timer.cancel();
        }
        beginTimer();
    }

    @FXML
    public void pauseHandler(ActionEvent actionEvent) {
        mediaPlayer.pause();
        stopTimer();
    }
    @FXML
    public void resetHandler(ActionEvent actionEvent) {
        mediaPlayer.seek(Duration.ZERO);
    }
    @FXML
    public void nextHandler(ActionEvent actionEvent) {
        updateIndex(UpdateIndexEnum.PLUS);
    }
    @FXML
    public void previousHandler(ActionEvent actionEvent) {
        updateIndex(UpdateIndexEnum.MINUS);
    }
    @FXML
    public void progressClick(MouseEvent mouseEvent) {
        if (mediaPlayer!=null){
            double width = progressBar.getWidth();
            double x = mouseEvent.getX();
            progressBar.setProgress(x/width);
            mediaPlayer.seek(Duration.seconds(media.getDuration().toSeconds()*(x/width)));

        }
    }


    public void beginTimer(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double total = media.getDuration().toSeconds();
                progressBar.setProgress(current/total);
            }
        };
        timer.scheduleAtFixedRate(task,1000,2000);
    }

    public void stopTimer(){
        if (timer!=null){
            timer.cancel();
        }
    }

    public void updateIndex(UpdateIndexEnum update){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        int index = Integer.valueOf(userMap.get(Constant.MP3_INDEX));
        int size = musicList.size();
        if (update.equals(UpdateIndexEnum.PLUS)){
            index = (index<size) ? (index+1) : 1;
        }else {
            index = index==1 ? musicList.size() : index-1;
        }
        userMap.put(Constant.MP3_INDEX,String.valueOf(index));
        media = new Media(musicList.get(index-1).toURI().toString());
        mediaPlayer=new MediaPlayer(media);
        mediaPlayer.setVolume(((int)volume)*0.01);
        mediaPlayer.setOnEndOfMedia(()->{
            System.out.println("END");
            updateIndex(UpdateIndexEnum.PLUS);
        });
        mediaPlayer.play();
        if (timer!=null){
            timer.cancel();
        }
        beginTimer();
        faceUpdate();

    }

    public void faceUpdate(){
        int index = Integer.valueOf(userMap.get(Constant.MP3_INDEX));
        face.setText(musicList.get(index - 1).getName()
                + "--------"+index+" of "+musicList.size());
    }

    public void exitTask(){
        Platform.runLater(()->{
            Stage stage = (Stage) layout.getScene().getWindow();
            stage.setOnCloseRequest(e->{
                System.out.println("exit");
                if (timer != null){
                    timer.cancel();
                }
                if (task!=null){
                    task.cancel();
                }

                try {

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userProperties)));
                    writer.write("");
                    writer.append("mp3dir=" + userMap.get(Constant.MP3_DIR));
                    writer.newLine();
                    writer.append("mp3index=" + userMap.get(Constant.MP3_INDEX));
                    writer.newLine();
                    writer.flush();
                    writer.close();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                System.exit(1);
            });
        });
    }


}