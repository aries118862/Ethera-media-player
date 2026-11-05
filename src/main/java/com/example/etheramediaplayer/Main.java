package com.example.etheramediaplayer;

import com.example.etheramediaplayer.controller.MainController;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private MainController controller;

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        this.controller = new MainController(this);

        stage.setTitle("Ethera Media Player");
        stage.setResizable(true);
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        stage.setScene(controller.createMainScene(stage));
        stage.show();
    }

    public void showMainInterface(Stage stage) {
        stage.setScene(controller.createMainScene(stage));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

