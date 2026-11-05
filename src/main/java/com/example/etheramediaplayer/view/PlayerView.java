package com.example.etheramediaplayer.view;

import com.example.etheramediaplayer.controller.MainController;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlayerView {
    private static String resolveStylesheetUrl() {
        var url = PlayerView.class.getResource("/com/example/etheramediaplayer/ethera.css");
        if (url == null) {
            url = PlayerView.class.getClassLoader().getResource("com/example/etheramediaplayer/ethera.css");
        }
        if (url == null) {
            throw new IllegalStateException("Unable to locate stylesheet: com/example/etheramediaplayer/ethera.css");
        }
        return url.toExternalForm();
    }

    public static Scene createScene(Stage stage, MainController controller) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("ethera-root");

        root.setTop(controller.createTopBar());
        root.setCenter(controller.createCenterPanel());
        root.setLeft(controller.createSidebar());
        root.setBottom(controller.createStatusBar());

        Region liquidAmbient = new Region();
        liquidAmbient.getStyleClass().add("liquid-ambient");
        liquidAmbient.setMouseTransparent(true);

        StackPane shell = new StackPane(liquidAmbient, root);
        shell.setAlignment(Pos.CENTER);
        shell.getStyleClass().add("liquid-shell");
        controller.installDragAndDrop(shell);

        FadeTransition ambientPulse = new FadeTransition(Duration.seconds(7), liquidAmbient);
        ambientPulse.setFromValue(0.72);
        ambientPulse.setToValue(1.0);
        ambientPulse.setAutoReverse(true);
        ambientPulse.setCycleCount(FadeTransition.INDEFINITE);
        ambientPulse.play();

        Scene scene = new Scene(shell, 1280, 760);
        scene.getStylesheets().add(resolveStylesheetUrl());
        controller.installKeyboardShortcuts(scene);
        return scene;
    }
}
