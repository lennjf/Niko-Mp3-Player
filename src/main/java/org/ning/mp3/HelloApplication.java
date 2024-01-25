package org.ning.mp3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 602, 170);

        stage.setTitle("Niko Robin Mp3 player");
        stage.setScene(scene);
        Image image16 = new Image(getClass().getResourceAsStream("/icon16.png"));
        Image image32 = new Image(getClass().getResourceAsStream("/icon32.png"));
        stage.getIcons().addAll(image16,image32);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}