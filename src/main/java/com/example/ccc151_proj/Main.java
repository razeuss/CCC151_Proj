package com.example.ccc151_proj;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage login_stage) throws IOException {
        login_stage.getIcons().add(new Image(new File("src/src/app-logo.jpg").toURI().toString()));
        //starts with the login scene
        FXMLLoader login_view = new FXMLLoader(Main.class.getResource("login-frame.fxml"));
        Scene login_scene = new Scene(login_view.load());

        login_stage.setTitle("Login");
        login_stage.setScene(login_scene);
        login_stage.setResizable(false);
        login_stage.show();
    }
}
