package org.main.recipeapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("add-recipe-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("Inteligentna Spiżarka");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
