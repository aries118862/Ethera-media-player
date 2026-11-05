package com.example.etheramediaplayer.view;

import com.example.etheramediaplayer.controller.MainController;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HomeScreen {
    private static String resolveStylesheetUrl() {
        var url = HomeScreen.class.getResource("/com/example/etheramediaplayer/ethera.css");
        if (url == null) {
            url = HomeScreen.class.getClassLoader().getResource("com/example/etheramediaplayer/ethera.css");
        }
        if (url == null) {
            throw new IllegalStateException("Unable to locate stylesheet: com/example/etheramediaplayer/ethera.css");
        }
        return url.toExternalForm();
    }

    public static Scene createScene(Stage stage, MainController controller) {
        StackPane root = new StackPane();
        root.getStyleClass().add("welcome-screen");

        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(48, 60, 52, 60));
        card.setMaxWidth(760);
        card.setMaxHeight(540);
        card.getStyleClass().add("welcome-card");

        Label brandLabel = new Label("ETHERА");
        brandLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        brandLabel.setTextFill(Color.web("#f4f4f5"));
        brandLabel.setOpacity(0.9);

        StackPane logoRing = new StackPane();
        logoRing.setPrefSize(160, 160);
        Circle outer = new Circle(72, Color.web("#d12525", 0.22));
        Circle inner = new Circle(58, Color.web("#111111"));
        inner.setStroke(Color.web("#ff3b3b"));
        inner.setStrokeWidth(2.5);
        Label playGlyph = new Label("▶");
        playGlyph.setFont(Font.font("Segoe UI", 44));
        playGlyph.setTextFill(Color.web("#f4f4f5"));
        logoRing.getChildren().addAll(outer, inner, playGlyph);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.2), logoRing);
        pulse.setFromX(0.96);
        pulse.setFromY(0.96);
        pulse.setToX(1.04);
        pulse.setToY(1.04);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        Label title = new Label("ETHERA MEDIA PLAYER");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        title.setTextFill(Color.web("#ffffff"));

        Label tagline = new Label("Your Media. Your Experience.");
        tagline.setFont(Font.font("Segoe UI", 17));
        tagline.setTextFill(Color.web("#cfcfcf"));

        Button enterButton = new Button("PLAY / ENTER");
        enterButton.setPrefSize(220, 52);
        enterButton.getStyleClass().add("ethera-primary-button");
        enterButton.setOnAction(event -> controller.getApplication().showMainInterface(stage));
        enterButton.setDefaultButton(true);

        FadeTransition fade = new FadeTransition(Duration.seconds(1), card);
        fade.setFromValue(0.2);
        fade.setToValue(1.0);
        fade.play();

        card.getChildren().addAll(brandLabel, logoRing, title, tagline, enterButton);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(resolveStylesheetUrl());
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                controller.getApplication().showMainInterface(stage);
            }
        });
        return scene;
    }
}
