package com.example.etheramediaplayer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.example.etheramediaplayer.service.FavoritesDatabase;
import com.example.etheramediaplayer.service.MediaLibraryService;
import com.example.etheramediaplayer.service.MediaNavigator;
import com.example.etheramediaplayer.service.SubtitleCue;
import com.example.etheramediaplayer.service.SubtitleService;
import com.example.etheramediaplayer.view.PlayerView;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {
    private final com.example.etheramediaplayer.Main application;
    private final MediaLibraryService mediaLibraryService = new MediaLibraryService();
    private final MediaNavigator mediaNavigator = new MediaNavigator();
    private final SubtitleService subtitleService = new SubtitleService();
    private final FavoritesDatabase favoritesDatabase = new FavoritesDatabase();
    private final ObservableList<File> playlist = FXCollections.observableArrayList();
    private final ListView<File> playlistView = new ListView<>(playlist);
    private final Slider timelineSlider = new Slider(0, 100, 0);
    private final Slider volumeSlider = new Slider(0, 100, 70);
    private final Label currentTimeLabel = new Label("00:00");
    private final Label totalTimeLabel = new Label("00:00");
    private final Label mainTitleLabel = new Label("Ethera Media Player");
    private final Label mediaMetaLabel = new Label("Nothing is playing");
    private final Label statusLabel = new Label("Ready");
    private final Label statusBarState = new Label();
    private final Label sectionLabel = new Label("HOME");
    private final TextField searchField = new TextField();
    private final ListView<File> searchResults = new ListView<>();
    private final Popup searchPopup = new Popup();
    private final StackPane mediaDisplay = new StackPane();
    private final Label playbackHud = new Label();
    private final Label mediaLoadingLabel = new Label("Loading local media 1%");
    private Label playlistPanelTitle;
    private final MediaView mediaView = new MediaView();
    private final ImageView artworkView = new ImageView();
    private final Label defaultArtwork = new Label("♫");
    private final StackPane musicDisc = new StackPane();
    private final Circle discSurface = new Circle(180, Color.web("#162331"));
    private final Circle discRing = new Circle(180, Color.TRANSPARENT);
    private final Circle discHighlight = new Circle(166, Color.TRANSPARENT);
    private final Circle discCenter = new Circle(20, Color.web("#dbe8f5"));
    private final Label subtitleLabel = new Label();
    private final VBox emptyState = new VBox(12);
    private final StackPane pageHost = new StackPane();
    private final Map<String, Label> navigationCounts = new HashMap<>();
    private final Canvas audioLevelMeter = new Canvas(520, 42);
    private final Label audioLevelLabel = new Label("-∞ dB");
    private Button playPauseButton;
    private Button fullscreenPlayPauseButton;
    private Button fullscreenMuteButton;
    private Button fullscreenFavoriteButton;
    private Button muteButton;
    private Button playbackFavoriteButton;
    private Button subtitleButton;
    private VBox playerPage;
    private StackPane mediaPane;
    private HBox infoCard;
    private HBox seekRow;
    private HBox controlRow;
    private HBox audioMeterRow;
    private ScrollPane navigationPane;
    private HBox fullscreenControlsOverlay;
    private Slider fullscreenVolumeSlider;
    private final PauseTransition fullscreenControlsHide = new PauseTransition(Duration.seconds(3));
    private FadeTransition fullscreenControlsFade;

    private MediaPlayer mediaPlayer;
    private int currentIndex = -1;
    private double playerVolume = 0.7;
    private double audioMeterLevelDb = -60;
    private boolean muted = false;
    private boolean autoPlayNext = true;
    private File selectedSubtitleFile;
    private List<File> detectedSubtitleFiles = List.of();
    private List<SubtitleCue> subtitleCues = List.of();
    private boolean videoMediaActive;
    private boolean videoFullscreenMode;
    private boolean shuffleEnabled;
    private boolean repeatEnabled;
    private String activeSection = "HOME";
    private long playbackGeneration;
    private Scene mainScene;
    private String selectedTheme = "soft-light";
    private double windowDragOffsetX;
    private double windowDragOffsetY;
    private double resizeStartX;
    private double resizeStartY;
    private double resizeStartWidth;
    private double resizeStartHeight;
    private double resizeStartStageX;
    private double resizeStartStageY;
    private boolean resizingWindow;
    private boolean resizeLeft;
    private boolean resizeRight;
    private boolean resizeTop;
    private boolean resizeBottom;
    private RotateTransition musicDiscRotation;

    public MainController(com.example.etheramediaplayer.Main application) {
        this.application = application;
        configurePlaylistView();
        configureTimeline();
        configureVolume();
        loadAutoDetectedMediaAsync();
    }

    public Scene createMainScene(Stage stage) {
        Scene scene = PlayerView.createScene(stage, this);
        mainScene = scene;
        installWindowResizeHandlers(scene, stage);
        applyTheme(scene.getRoot(), selectedTheme);
        stage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            BorderPane root = findPlayerRoot(scene);
            if (!isFullScreen && videoFullscreenMode && root != null) {
                setChromeVisible(root, true);
                videoFullscreenMode = false;
                setVideoFullscreenLayout(false);
            }
        });
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            if (navigationPane != null) {
                navigationPane.setPrefWidth(width < 1000 ? 210 : 250);
            }
            searchField.setPrefWidth(Math.max(150, Math.min(260, width * 0.2)));
            Node sceneRoot = scene.getRoot();
            sceneRoot.getStyleClass().removeAll("responsive-compact", "responsive-small", "responsive-short");
            if (width < 960) {
                sceneRoot.getStyleClass().add("responsive-small");
            } else if (width < 1120) {
                sceneRoot.getStyleClass().add("responsive-compact");
            }
            if (scene.getHeight() < 650) {
                sceneRoot.getStyleClass().add("responsive-short");
            }
        });
        scene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            Node sceneRoot = scene.getRoot();
            if (newHeight.doubleValue() < 650) {
                sceneRoot.getStyleClass().add("responsive-short");
            } else {
                sceneRoot.getStyleClass().remove("responsive-short");
            }
        });
        scene.getRoot().getStyleClass().add("responsive-wide");
        return scene;
    }

    private void installWindowResizeHandlers(Scene scene, Stage stage) {
        final double resizeMargin = 8;
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (stage.isMaximized() || stage.isFullScreen()) {
                scene.setCursor(Cursor.DEFAULT);
                return;
            }
            boolean left = event.getSceneX() <= resizeMargin;
            boolean right = event.getSceneX() >= scene.getWidth() - resizeMargin;
            boolean top = event.getSceneY() <= resizeMargin;
            boolean bottom = event.getSceneY() >= scene.getHeight() - resizeMargin;
            scene.setCursor(cursorForResize(left, right, top, bottom));
        });
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY
                    || stage.isMaximized() || stage.isFullScreen()) {
                return;
            }
            resizeLeft = event.getSceneX() <= resizeMargin;
            resizeRight = event.getSceneX() >= scene.getWidth() - resizeMargin;
            resizeTop = event.getSceneY() <= resizeMargin;
            resizeBottom = event.getSceneY() >= scene.getHeight() - resizeMargin;
            resizingWindow = resizeLeft || resizeRight || resizeTop || resizeBottom;
            if (resizingWindow) {
                resizeStartX = event.getScreenX();
                resizeStartY = event.getScreenY();
                resizeStartWidth = stage.getWidth();
                resizeStartHeight = stage.getHeight();
                resizeStartStageX = stage.getX();
                resizeStartStageY = stage.getY();
                event.consume();
            }
        });
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!resizingWindow) {
                return;
            }
            double deltaX = event.getScreenX() - resizeStartX;
            double deltaY = event.getScreenY() - resizeStartY;
            double minWidth = stage.getMinWidth();
            double minHeight = stage.getMinHeight();
            double width = resizeStartWidth;
            double height = resizeStartHeight;
            double x = resizeStartStageX;
            double y = resizeStartStageY;
            if (resizeRight) {
                width = Math.max(minWidth, resizeStartWidth + deltaX);
            }
            if (resizeBottom) {
                height = Math.max(minHeight, resizeStartHeight + deltaY);
            }
            if (resizeLeft) {
                width = Math.max(minWidth, resizeStartWidth - deltaX);
                x = resizeStartStageX + resizeStartWidth - width;
            }
            if (resizeTop) {
                height = Math.max(minHeight, resizeStartHeight - deltaY);
                y = resizeStartStageY + resizeStartHeight - height;
            }
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
            event.consume();
        });
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> resizingWindow = false);
    }

    private static Cursor cursorForResize(boolean left, boolean right, boolean top, boolean bottom) {
        if ((left && top) || (right && bottom)) {
            return left ? Cursor.NW_RESIZE : Cursor.SE_RESIZE;
        }
        if ((right && top) || (left && bottom)) {
            return right ? Cursor.NE_RESIZE : Cursor.SW_RESIZE;
        }
        if (left || right) {
            return Cursor.H_RESIZE;
        }
        if (top || bottom) {
            return Cursor.V_RESIZE;
        }
        return Cursor.DEFAULT;
    }

    private void applyTheme(Node root, String theme) {
        if (root == null) {
            return;
        }
        root.getStyleClass().removeIf(styleClass -> styleClass.startsWith("theme-"));
        root.getStyleClass().add("theme-" + theme);
        boolean useWhiteIcons = "modern-dark".equals(theme);
        updateIconTint(root, useWhiteIcons);
    }

    private static void updateIconTint(Node node, boolean useWhiteIcons) {
        if (node instanceof ImageView imageView && imageView.getStyleClass().contains("ethera-icon-image")) {
            imageView.setEffect(useWhiteIcons ? createWhiteIconEffect() : null);
        }
        if (node instanceof javafx.scene.Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> updateIconTint(child, useWhiteIcons));
        }
    }

    private static ColorAdjust createWhiteIconEffect() {
        ColorAdjust whiteTint = new ColorAdjust();
        whiteTint.setSaturation(-1);
        whiteTint.setBrightness(1);
        return whiteTint;
    }

    public com.example.etheramediaplayer.Main getApplication() {
        return application;
    }

    public void installDragAndDrop(Node dropTarget) {
        dropTarget.setOnDragOver(event -> {
            if (containsSupportedMedia(event.getDragboard())) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        dropTarget.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean accepted = containsSupportedMedia(dragboard);
            if (accepted) {
                addFilesToPlaylist(dragboard.getFiles());
            }
            event.setDropCompleted(accepted);
            event.consume();
        });
    }

    private boolean containsSupportedMedia(Dragboard dragboard) {
        return dragboard != null && dragboard.hasFiles()
                && dragboard.getFiles().stream().anyMatch(mediaLibraryService::isSupportedMedia);
    }

    public HBox createTopBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(18, 22, 12, 22));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("top-bar");
        bar.setOnMousePressed(event -> {
            if (event.getTarget() instanceof Button) {
                return;
            }
            Stage stage = (Stage) bar.getScene().getWindow();
            if (!stage.isMaximized() && !stage.isFullScreen()) {
                windowDragOffsetX = event.getScreenX() - stage.getX();
                windowDragOffsetY = event.getScreenY() - stage.getY();
            }
        });
        bar.setOnMouseDragged(event -> {
            Stage stage = (Stage) bar.getScene().getWindow();
            if (!stage.isMaximized() && !stage.isFullScreen()) {
                stage.setX(event.getScreenX() - windowDragOffsetX);
                stage.setY(event.getScreenY() - windowDragOffsetY);
            }
        });

        VBox titleWrap = new VBox();
        titleWrap.setAlignment(Pos.CENTER_LEFT);
        titleWrap.getStyleClass().add("title-wrap");
        Label appName = new Label("Ethera Media Player");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleWrap.getChildren().add(appName);

        // Thinned, lighter/white search bar styling and structure
        HBox searchWrap = new HBox(6);
        searchWrap.setAlignment(Pos.CENTER_LEFT);
        searchWrap.setMinHeight(22);
        searchWrap.setMaxHeight(26);
        searchWrap.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 2 10 2 10; -fx-border-color: #d0d7de; -fx-border-radius: 12;");
        searchWrap.getStyleClass().add("top-search-wrap");

        Node searchGlyph = loadIconImage("magnifying-glass.png", 14);
        searchGlyph.setStyle("-fx-opacity: 0.6;");

        searchField.setPromptText("Search music or videos");
        searchField.setFocusTraversable(false);
        searchField.setPrefWidth(260);
        searchField.setPrefHeight(22);
        searchField.setMinHeight(20);
        searchField.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2328; -fx-prompt-text-fill: #656d76; -fx-padding: 0;");
        searchField.getStyleClass().add("top-search-field");
        configureSearchPopup();
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue;
            filterPlaylist(query);
            updateSearchPopup(query, searchWrap);
        });
        searchField.setOnMouseClicked(event -> updateSearchPopup(searchField.getText(), searchWrap));
        searchWrap.getChildren().addAll(searchGlyph, searchField);

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(false);
        menuBar.getStyleClass().add("app-menu-bar");

        Menu mediaMenu = new Menu("Media");
        MenuItem openMediaItem = new MenuItem("Open Media...");
        openMediaItem.setOnAction(event -> addMediaFiles());
        MenuItem openFolderItem = new MenuItem("Open Folder...");
        openFolderItem.setOnAction(event -> addFolder());
        mediaMenu.getItems().addAll(openMediaItem, openFolderItem);

        Menu playbackMenu = new Menu("Playback");
        MenuItem playPauseItem = new MenuItem("Play / Pause");
        playPauseItem.setOnAction(event -> togglePlayPause());
        MenuItem stopItem = new MenuItem("Stop");
        stopItem.setOnAction(event -> stopPlayback());
        MenuItem previousItem = new MenuItem("Previous");
        previousItem.setOnAction(event -> previousTrack());
        MenuItem nextItem = new MenuItem("Next");
        nextItem.setOnAction(event -> nextTrack());
        playbackMenu.getItems().addAll(playPauseItem, stopItem, previousItem, nextItem);

        Menu audioMenu = new Menu("Audio");
        MenuItem muteItem = new MenuItem("Mute / Unmute");
        muteItem.setOnAction(event -> toggleMute());
        MenuItem volumeUpItem = new MenuItem("Increase Volume");
        volumeUpItem.setOnAction(event -> increaseVolume());
        MenuItem volumeDownItem = new MenuItem("Decrease Volume");
        volumeDownItem.setOnAction(event -> decreaseVolume());
        audioMenu.getItems().addAll(muteItem, volumeUpItem, volumeDownItem);

        Menu videoMenu = new Menu("Video");
        MenuItem fullscreenItem = new MenuItem("Toggle Fullscreen");
        fullscreenItem.setOnAction(event -> toggleFullscreen(menuBar.getScene()));
        videoMenu.getItems().add(fullscreenItem);

        Menu subtitleMenu = new Menu("Subtitle");
        MenuItem subtitleItem = new MenuItem("Load Subtitle...");
        subtitleItem.setOnAction(event -> openSubtitleMenu(menuBar.getScene()));
        subtitleMenu.getItems().add(subtitleItem);

        Menu toolsMenu = new Menu("Tools");
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(event -> openSettingsDialog());
        toolsMenu.getItems().add(settingsItem);

        Menu viewMenu = new Menu("View");
        MenuItem homeItem = new MenuItem("Home");
        homeItem.setOnAction(event -> showSection("HOME"));
        viewMenu.getItems().add(homeItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About Ethera Media Player");
        aboutItem.setOnAction(event -> statusLabel.setText("Ethera Media Player is ready"));
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(mediaMenu, playbackMenu, audioMenu, videoMenu,
                subtitleMenu, toolsMenu, viewMenu, helpMenu);
        bar.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            boolean compact = newWidth.doubleValue() < 980;
            menuBar.setVisible(!compact);
            menuBar.setManaged(!compact);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerActions = new HBox(12, searchWrap);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        Button minimizeButton = createWindowButton("—");
        minimizeButton.getStyleClass().add("minimize-window-button");
        minimizeButton.setOnAction(event -> ((Stage) minimizeButton.getScene().getWindow()).setIconified(true));
        Button maximizeButton = createWindowButton("□");
        maximizeButton.getStyleClass().add("maximize-window-button");
        maximizeButton.setOnAction(event -> {
            Stage stage = (Stage) maximizeButton.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        Button closeButton = createWindowButton("×");
        closeButton.getStyleClass().add("close-window-button");
        closeButton.setOnAction(event -> confirmExit((Stage) closeButton.getScene().getWindow()));
        HBox windowActions = new HBox(4, minimizeButton, maximizeButton, closeButton);
        windowActions.getStyleClass().add("window-actions");

        bar.getChildren().addAll(titleWrap, menuBar, spacer, headerActions, windowActions);
        return bar;
    }

    private void configureSearchPopup() {
        searchResults.setPrefSize(380, 280);
        searchResults.setPlaceholder(new Label("No matching media"));
        searchResults.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item.getName());
                setTooltip(new Tooltip(item.getAbsolutePath()));
            }
        });
        searchResults.setOnMouseClicked(event -> {
            if (event.getClickCount() != 1) {
                return;
            }
            File selected = searchResults.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            int index = playlist.indexOf(selected);
            if (index >= 0) {
                currentIndex = index;
                searchPopup.hide();
                searchField.getParent().requestFocus();
                showSection("HOME");
                playSelectedMedia(selected, index);
            }
        });

        VBox popupContent = new VBox(searchResults);
        popupContent.setPrefWidth(380);
        popupContent.setStyle("-fx-background-color: #20252d; -fx-border-color: #596474; -fx-border-width: 1;");
        popupContent.getStylesheets().add(resolveStylesheetUrl());
        popupContent.getStyleClass().add("search-popup");
        searchPopup.getContent().add(popupContent);
        searchPopup.setAutoHide(true);
        searchPopup.setHideOnEscape(true);
    }

    private void updateSearchPopup(String query, HBox searchWrap) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty() || searchWrap.getScene() == null) {
            searchPopup.hide();
            return;
        }

        String lowerQuery = trimmed.toLowerCase(Locale.ROOT);
        ObservableList<File> matches = FXCollections.observableArrayList();
        for (File file : playlist) {
            String name = file.getName().toLowerCase(Locale.ROOT);
            String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
            if (name.contains(lowerQuery) || path.contains(lowerQuery)) {
                matches.add(file);
            }
        }
        searchResults.setItems(matches);
        if (!matches.isEmpty()) {
            searchResults.getSelectionModel().selectFirst();
        } else {
            searchResults.getSelectionModel().clearSelection();
        }

        javafx.geometry.Point2D location = searchWrap.localToScreen(0, searchWrap.getHeight());
        if (location != null) {
            searchPopup.show(searchWrap, location.getX(), location.getY() + 6);
        }
    }

    private Button createWindowButton(String symbol) {
        Button button = new Button(symbol);
        button.getStyleClass().add("window-button");
        return button;
    }

    private void confirmExit(Stage owner) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Exit Ethera Media Player");
        confirmation.setHeaderText("Do you want to exit?");
        confirmation.setContentText("Your current playback session will be closed.");
        confirmation.initOwner(owner);
        confirmation.getDialogPane().getStyleClass().add("exit-confirmation");
        confirmation.getDialogPane().setMinWidth(380);
        confirmation.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirmation.getDialogPane().getStylesheets().add(resolveStylesheetUrl());
        applyTheme(confirmation.getDialogPane(), selectedTheme == null ? "soft-light" : selectedTheme);
        confirmation.showAndWait().filter(ButtonType.YES::equals).ifPresent(button -> {
            owner.close();
            Platform.exit();
        });
    }

    public HBox createStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar");
        statusBarState.textProperty().bind(statusLabel.textProperty());
        statusBarState.getStyleClass().add("status-live");
        Label separator = new Label("|");
        Label engine = new Label("Ethera Media Player  •  JavaFX Media Engine");
        engine.getStyleClass().add("status-detail");
        statusBar.getChildren().addAll(statusBarState, separator, engine);
        return statusBar;
    }

    public ScrollPane createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(6, 0, 16, 0));
        sidebar.setPrefWidth(220);
        sidebar.getStyleClass().add("sidebar");

        Label sectionTitle = new Label("HOME");
        sectionTitle.setPadding(new Insets(12, 20, 8, 20));
        sectionTitle.setTextFill(Color.web("#7f91a6"));
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Button homeItem = createNavItem("Home", true);
        Label playlistsTitle = createSidebarHeading("PLAYLISTS");
        Button myPlaylistItem = createCountNavItem("playlist.png", "My Playlist", false);
        Button recentlyAddedItem = createCountNavItem("clock.png", "Recently Added", false);
        Button recentlyPlayedItem = createCountNavItem("play-button-arrowhead.png", "Recently Played", false);
        Button topRatedItem = createCountNavItem("bookmark.png", "Top Rated", false);
        Label libraryTitle = createSidebarHeading("LIBRARY");
        Button musicItem = createCountNavItem("music-album.png", "Music", false);
        Button videosItem = createCountNavItem("video-camera.png", "Videos", false);
        Button imagesItem = createCountNavItem("image.png", "Images", false);
        Button settingsItem = createNavItem("Settings", false);

        homeItem.setOnAction(event -> showSection("HOME"));
        myPlaylistItem.setOnAction(event -> showSection("PLAYLIST"));
        recentlyAddedItem.setOnAction(event -> showSection("LIBRARY"));
        recentlyPlayedItem.setOnAction(event -> showSection("RECENT"));
        topRatedItem.setOnAction(event -> showSection("FAVORITES"));
        musicItem.setOnAction(event -> showSection("MUSIC"));
        videosItem.setOnAction(event -> showSection("VIDEOS"));
        imagesItem.setOnAction(event -> statusLabel.setText("Image library is ready"));
        settingsItem.setOnAction(event -> openSettingsDialog());
        refreshNavigationCounts();

        sidebar.getChildren().addAll(sectionTitle, homeItem, createSidebarDivider(), playlistsTitle,
                myPlaylistItem, recentlyAddedItem,
                recentlyPlayedItem, topRatedItem, createSidebarDivider(), libraryTitle, musicItem,
                videosItem, imagesItem, createSidebarDivider(), settingsItem);
        Label playbackLevelTitle = createSidebarHeading("PLAYBACK LEVEL");
        playbackLevelTitle.setPadding(new Insets(14, 16, 6, 16));
        audioLevelMeter.setWidth(130);
        audioMeterRow.setMaxWidth(Double.MAX_VALUE);
        audioMeterRow.setPadding(new Insets(4, 8, 8, 8));
        sidebar.getChildren().addAll(createSidebarDivider(), playbackLevelTitle, audioMeterRow);
        navigationPane = new ScrollPane(sidebar);
        navigationPane.setFitToWidth(true);
        navigationPane.setPannable(true);
        navigationPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navigationPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navigationPane.setPrefWidth(250);
        navigationPane.getStyleClass().add("navigation-scroll");
        final Timeline[] smoothScroll = new Timeline[1];
        navigationPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentHeight = sidebar.getBoundsInLocal().getHeight();
            double viewportHeight = navigationPane.getViewportBounds().getHeight();
            double maxScroll = contentHeight - viewportHeight;
            if (maxScroll <= 0 || event.getDeltaY() == 0) {
                return;
            }

            double currentPixels = navigationPane.getVvalue() * maxScroll;
            double targetPixels = Math.max(0, Math.min(maxScroll, currentPixels - event.getDeltaY()));
            if (smoothScroll[0] != null) {
                smoothScroll[0].stop();
            }
            smoothScroll[0] = new Timeline(new KeyFrame(Duration.millis(110),
                    new KeyValue(navigationPane.vvalueProperty(), targetPixels / maxScroll)));
            smoothScroll[0].play();
            event.consume();
        });
        return navigationPane;
    }

    private Label createSidebarHeading(String text) {
        Label heading = new Label(text);
        heading.setPadding(new Insets(14, 20, 6, 20));
        heading.getStyleClass().add("sidebar-heading");
        return heading;
    }

    private Region createSidebarDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.getStyleClass().add("sidebar-divider");
        return divider;
    }

    public VBox createCenterPanel() {
        VBox centerPanel = new VBox();
        centerPanel.setPadding(Insets.EMPTY);
        centerPanel.setSpacing(0);
        centerPanel.getStyleClass().add("player-area");

        mediaPane = new StackPane();
        mediaPane.setPrefHeight(420);
        mediaPane.getStyleClass().addAll("media-pane", "video-screen");
        mediaPane.setAlignment(Pos.CENTER);

        mediaDisplay.setAlignment(Pos.CENTER);
        mediaDisplay.setPrefSize(900, 420);
        mediaDisplay.getStyleClass().add("media-display");

        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(800);
        mediaView.setFitHeight(360);
        mediaView.setVisible(false);
        mediaDisplay.getChildren().add(mediaView);

        artworkView.setPreserveRatio(true);
        artworkView.setFitWidth(330);
        artworkView.setFitHeight(330);
        artworkView.setClip(new Circle(165, 165, 165));
        artworkView.setVisible(false);
        artworkView.getStyleClass().add("artwork-image");

        defaultArtwork.setFont(Font.font("Segoe UI Symbol", FontWeight.BOLD, 104));
        defaultArtwork.setTextFill(Color.web("#eaf3ff"));
        defaultArtwork.setMinSize(330, 330);
        defaultArtwork.setPrefSize(330, 330);
        defaultArtwork.setAlignment(Pos.CENTER);
        defaultArtwork.setVisible(false);
        defaultArtwork.getStyleClass().add("default-artwork");

        discRing.setStroke(Color.web("#7891a8"));
        discRing.setStrokeWidth(2.5);
        discHighlight.setStroke(Color.web("rgba(255, 255, 255, 0.18)"));
        discHighlight.setStrokeWidth(1.2);
        discCenter.setStroke(Color.web("#7891a8"));
        discCenter.setStrokeWidth(2);
        musicDisc.getStyleClass().add("music-disc");
        musicDisc.setMaxSize(360, 360);
        musicDisc.getChildren().addAll(discSurface, artworkView, discRing, discHighlight, defaultArtwork, discCenter);
        musicDisc.setVisible(false);
        musicDiscRotation = new RotateTransition(Duration.seconds(9), musicDisc);
        musicDiscRotation.setByAngle(360);
        musicDiscRotation.setCycleCount(RotateTransition.INDEFINITE);
        musicDiscRotation.setInterpolator(Interpolator.LINEAR);
        mediaDisplay.getChildren().add(musicDisc);

        subtitleLabel.setVisible(false);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(760);
        subtitleLabel.setMouseTransparent(true);
        subtitleLabel.getStyleClass().add("subtitle-overlay");
        mediaDisplay.getChildren().add(subtitleLabel);
        StackPane.setAlignment(subtitleLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(subtitleLabel, new Insets(0, 24, 24, 24));

        playbackHud.setVisible(false);
        playbackHud.setMouseTransparent(true);
        playbackHud.getStyleClass().add("playback-hud");
        mediaDisplay.getChildren().add(playbackHud);
        StackPane.setAlignment(playbackHud, Pos.CENTER);

        fullscreenPlayPauseButton = createControlButton("Play/Pause", true);
        fullscreenMuteButton = createControlButton("Mute", false);
        fullscreenFavoriteButton = createFullscreenFavoriteButton();
        fullscreenVolumeSlider = new Slider(0, 100, volumeSlider.getValue());
        fullscreenVolumeSlider.valueProperty().bindBidirectional(volumeSlider.valueProperty());
        fullscreenVolumeSlider.setPrefWidth(120);
        fullscreenVolumeSlider.setMaxWidth(120);
        fullscreenVolumeSlider.getStyleClass().add("control-volume-slider");
        Button fullscreenPlaylistButton = createControlButton("Playlist", false);
        fullscreenPlaylistButton.setOnAction(event -> {
            if (videoFullscreenMode) {
                toggleFullscreen(fullscreenControlsOverlay.getScene());
            }
            showSection("PLAYLIST");
        });
        Region fullscreenSpacer = new Region();
        HBox.setHgrow(fullscreenSpacer, Priority.ALWAYS);
        fullscreenControlsOverlay = new HBox(6,
                createControlButton("Shuffle", false),
                createControlButton("Repeat", false),
                fullscreenPlaylistButton,
                fullscreenFavoriteButton,
                fullscreenMuteButton,
                fullscreenVolumeSlider,
                fullscreenSpacer,
                createControlButton("Previous", false),
                fullscreenPlayPauseButton,
                createControlButton("Next", false));
        fullscreenControlsOverlay.setAlignment(Pos.CENTER);
        fullscreenControlsOverlay.getStyleClass().add("fullscreen-controls");
        fullscreenControlsOverlay.setMaxWidth(980);
        fullscreenControlsOverlay.setPrefWidth(960);
        fullscreenControlsOverlay.setMaxHeight(44);
        for (Node control : fullscreenControlsOverlay.getChildren()) {
            if (control instanceof Button button) {
                button.setPrefSize(34, 34);
            }
        }
        fullscreenPlayPauseButton.setPrefSize(38, 34);
        fullscreenVolumeSlider.setMaxHeight(20);
        fullscreenControlsOverlay.setVisible(false);
        fullscreenControlsOverlay.setManaged(false);
        mediaDisplay.getChildren().add(fullscreenControlsOverlay);
        mediaDisplay.widthProperty().addListener((obs, oldWidth, newWidth) ->
                fullscreenControlsOverlay.setPrefWidth(Math.min(960, Math.max(0, newWidth.doubleValue() - 56))));
        StackPane.setAlignment(fullscreenControlsOverlay, Pos.BOTTOM_CENTER);
        StackPane.setMargin(fullscreenControlsOverlay, new Insets(0, 28, 5, 28));
        fullscreenControlsHide.setOnFinished(event -> {
            if (videoFullscreenMode) {
                fullscreenControlsFade.playFromStart();
            }
        });
        fullscreenControlsFade = new FadeTransition(Duration.millis(250), fullscreenControlsOverlay);
        fullscreenControlsFade.setFromValue(1);
        fullscreenControlsFade.setToValue(0);
        fullscreenControlsFade.setOnFinished(event -> {
            if (!videoFullscreenMode) {
                return;
            }
            fullscreenControlsOverlay.setVisible(false);
            fullscreenControlsOverlay.setManaged(false);
        });
        mediaDisplay.addEventFilter(MouseEvent.MOUSE_MOVED, event -> showFullscreenControls());
        mediaDisplay.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> showFullscreenControls());
        mediaDisplay.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> showFullscreenControls());
        mediaDisplay.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> showFullscreenControls());

        playPauseButton = createControlButton("Play/Pause", true);
        mediaPane.getChildren().add(mediaDisplay);
        VBox.setVgrow(mediaPane, Priority.ALWAYS);
        mediaDisplay.setOnMouseClicked(event -> {
            if (videoMediaActive && event.getClickCount() == 2) {
                toggleFullscreen(mediaDisplay.getScene());
                event.consume();
            }
        });

        emptyState.setAlignment(Pos.CENTER);
        emptyState.setMouseTransparent(true);
        emptyState.setVisible(true);

        Label emptyIcon = new Label("◉");
        emptyIcon.setTextFill(Color.web("#ff4d4d"));
        emptyIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 64));

        Label emptyTitle = new Label("Nothing is playing");
        emptyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        emptyTitle.setTextFill(Color.WHITE);

        Label emptySubtitle = new Label("Open a media file to begin.");
        emptySubtitle.setTextFill(Color.web("#b6b6b6"));
        emptySubtitle.setFont(Font.font("Segoe UI", 14));

        Button openMediaButton = createIconTextButton("Open Media", "play-button-arrowhead.png", "ethera-secondary-button");
        openMediaButton.setOnAction(event -> addMediaFiles());

        Button openFolderButton = createIconTextButton("Open Folder", "folder.png", "ethera-secondary-button");
        openFolderButton.setOnAction(event -> addFolder());

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle, openMediaButton, openFolderButton);
        mediaDisplay.getChildren().add(emptyState);

        Label sectionHeader = new Label("HOME");
        sectionHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        sectionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        infoCard = new HBox(14);
        infoCard.setPadding(new Insets(10, 20, 10, 20));
        infoCard.setAlignment(Pos.CENTER_LEFT);
        infoCard.getStyleClass().addAll("control-bar", "video-header");

        mainTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        mediaLoadingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        mediaLoadingLabel.setTextFill(Color.web("#718096"));
        mediaLoadingLabel.setVisible(true);
        mediaMetaLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setTextFill(Color.web("#55a8ff"));
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        infoCard.getChildren().addAll(mainTitleLabel, mediaLoadingLabel, titleSpacer, mediaMetaLabel, statusLabel);

        HBox utilityControls = new HBox(12);
        utilityControls.setAlignment(Pos.CENTER_LEFT);
        utilityControls.getChildren().addAll(
                createControlButton("Shuffle", false),
                createControlButton("Repeat", false),
            createControlButton("Playlist", false));
        playbackFavoriteButton = createPlaybackFavoriteButton();
        utilityControls.getChildren().add(playbackFavoriteButton);
        utilityControls.getChildren().add(
            muteButton = createControlButton("Mute", false));

        seekRow = new HBox(12);
        seekRow.setAlignment(Pos.CENTER_LEFT);
        seekRow.getStyleClass().add("timeline-row");
        timelineSlider.setPrefWidth(600);
        timelineSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timelineSlider, Priority.ALWAYS);
        seekRow.getChildren().addAll(currentTimeLabel, timelineSlider, totalTimeLabel);

        audioLevelMeter.getStyleClass().add("audio-level-meter");
        audioLevelLabel.getStyleClass().add("audio-level-value");
        audioLevelLabel.setMinWidth(58);
        audioLevelLabel.setAlignment(Pos.CENTER_RIGHT);
        audioMeterRow = new HBox(10, audioLevelMeter, audioLevelLabel);
        audioMeterRow.setAlignment(Pos.CENTER);
        audioMeterRow.setMaxWidth(700);
        HBox.setHgrow(audioLevelMeter, Priority.ALWAYS);
        audioLevelMeter.widthProperty().addListener((obs, oldWidth, newWidth) -> drawAudioLevelMeter());
        drawAudioLevelMeter();

        volumeSlider.setPrefWidth(90);
        volumeSlider.getStyleClass().add("control-volume-slider");
        utilityControls.getChildren().add(volumeSlider);

        HBox transportControls = new HBox(18);
        transportControls.setAlignment(Pos.CENTER);
        transportControls.getChildren().addAll(
                createControlButton("Previous", false),
                playPauseButton,
                createControlButton("Next", false));

        HBox displayControls = new HBox(10);
        displayControls.setAlignment(Pos.CENTER_RIGHT);
        subtitleButton = createControlButton("Subtitles", false);
        subtitleButton.setVisible(false);
        subtitleButton.setManaged(false);
        displayControls.getChildren().addAll(
                subtitleButton,
                createControlButton("Settings", false),
                createControlButton("Video", false),
                createControlButton("Fullscreen", false));

        controlRow = new HBox(16);
        controlRow.getStyleClass().add("controls");
        controlRow.setPadding(new Insets(10, 20, 10, 20));
        controlRow.setAlignment(Pos.CENTER_LEFT);
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        controlRow.getChildren().addAll(utilityControls, leftSpacer, transportControls,
                rightSpacer, displayControls);

        centerPanel.getChildren().addAll(infoCard, mediaPane, seekRow, controlRow);
        VBox.setVgrow(mediaPane, Priority.ALWAYS);
        playerPage = centerPanel;
        pageHost.getChildren().setAll(playerPage);

        VBox pageShell = new VBox(pageHost);
        pageShell.getStyleClass().addAll("content-shell", "player-area");
        VBox.setVgrow(pageHost, Priority.ALWAYS);
        return pageShell;
    }

    public VBox createPlaylistPanel() {
        VBox playlistPanel = new VBox(12);
        playlistPanel.setPadding(new Insets(18, 10, 18, 10));
        playlistPanel.setPrefWidth(300);
        playlistPanel.getStyleClass().add("playlist-panel");

        HBox playlistHeader = new HBox();
        playlistHeader.setAlignment(Pos.CENTER_LEFT);
        playlistPanelTitle = new Label("PLAYLIST");
        playlistPanelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Region pad = new Region();
        HBox.setHgrow(pad, Priority.ALWAYS);
        Button addButton = createIconTextButton("Add", "add-to-folder.png", "ethera-secondary-button");
        addButton.setOnAction(event -> addMediaFiles());
        playlistHeader.getChildren().addAll(playlistPanelTitle, pad, addButton);

        Button removeButton = createIconTextButton("Remove", "playlist.png", "ethera-danger-button");
        removeButton.setOnAction(event -> removeSelectedMedia());

        playlistView.setPrefHeight(520);
        playlistView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                String fileName = item.getName();
                boolean isCurrent = currentIndex >= 0 && playlist.indexOf(item) == currentIndex;
                StackPane thumbnail = createThumbnail(item);
                loadEmbeddedThumbnail(item, thumbnail);
                Label fileLabel = new Label(fileName);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                    HBox row = new HBox(8, thumbnail, fileLabel, spacer,
                        createFavoriteButton(item), createRemoveButton(item));
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
                setTextFill(isCurrent ? Color.WHITE : Color.web("#dfe5eb"));
                setStyle(isCurrent ? "-fx-background-color: rgba(212,21,21,0.35); -fx-padding: 8;" : "-fx-padding: 8;");
            }
        });

        playlistView.setOnMouseClicked(event -> {
            File selected = playlistView.getSelectionModel().getSelectedItem();
            if (selected != null && event.getClickCount() == 2) {
                currentIndex = playlist.indexOf(selected);
                playSelectedMedia(selected, currentIndex);
            }
        });

        playlistPanel.getChildren().addAll(playlistHeader, playlistView, removeButton);
        return playlistPanel;
    }

    private Button createFavoriteButton(File mediaFile) {
        Button favoriteButton = new Button();
        favoriteButton.getStyleClass().add("favorite-button");
        boolean initiallyFavorite = favoritesDatabase.isFavorite(mediaFile);
        updateFavoriteButton(favoriteButton, initiallyFavorite);
        favoriteButton.setOnAction(event -> {
            if (!favoritesDatabase.isAvailable()) {
                statusLabel.setText("Favorites unavailable: SQLite driver not loaded");
                return;
            }
            boolean favorite = !favoritesDatabase.isFavorite(mediaFile);
            favoritesDatabase.setFavorite(mediaFile, favorite);
            updateFavoriteButton(favoriteButton, favorite);
            refreshNavigationCounts();
            if ("FAVORITES".equals(activeSection) && !favorite) {
                showSection("FAVORITES");
            }
        });
        return favoriteButton;
    }

    private Button createPlaybackFavoriteButton() {
        Button favoriteButton = new Button();
        favoriteButton.getStyleClass().addAll("favorite-button", "playback-favorite-button");
        favoriteButton.setDisable(true);
        updateFavoriteButton(favoriteButton, false);
        favoriteButton.setOnAction(event -> {
            File playingFile = getPlayingFile();
            if (playingFile == null) {
                return;
            }
            toggleFavorite(playingFile, favoriteButton);
        });
        return favoriteButton;
    }

    private Button createFullscreenFavoriteButton() {
        Button favoriteButton = new Button();
        favoriteButton.getStyleClass().addAll("favorite-button", "playback-favorite-button");
        favoriteButton.setDisable(true);
        updateFavoriteButton(favoriteButton, false);
        favoriteButton.setOnAction(event -> {
            File playingFile = getPlayingFile();
            if (playingFile != null) {
                toggleFavorite(playingFile, favoriteButton);
            }
        });
        return favoriteButton;
    }

    private void toggleFavorite(File mediaFile, Button favoriteButton) {
        if (!favoritesDatabase.isAvailable()) {
            statusLabel.setText("Favorites unavailable: SQLite driver not loaded");
            return;
        }
        boolean favorite = !favoritesDatabase.isFavorite(mediaFile);
        favoritesDatabase.setFavorite(mediaFile, favorite);
        updateFavoriteButton(favoriteButton, favorite);
        if (playbackFavoriteButton != null && playbackFavoriteButton != favoriteButton) {
            updateFavoriteButton(playbackFavoriteButton, favorite);
        }
        if (fullscreenFavoriteButton != null && fullscreenFavoriteButton != favoriteButton) {
            updateFavoriteButton(fullscreenFavoriteButton, favorite);
        }
        refreshNavigationCounts();
        if ("FAVORITES".equals(activeSection) && !favorite) {
            showSection("FAVORITES");
        }
    }

    private File getPlayingFile() {
        return currentIndex >= 0 && currentIndex < playlist.size() ? playlist.get(currentIndex) : null;
    }

    private void updateFavoriteButton(Button favoriteButton, boolean favorite) {
        favoriteButton.setText(null);
        Node favoriteIcon = loadIconImage("bookmark.png", 19);
        if (favoriteIcon instanceof ImageView imageView && favorite) {
            imageView.setEffect(createFavoriteTintEffect());
        }
        favoriteButton.setGraphic(favoriteIcon);
        favoriteButton.getStyleClass().remove("favorite-active");
        if (favorite) {
            favoriteButton.getStyleClass().add("favorite-active");
        }
        favoriteButton.setTooltip(new Tooltip(favorite ? "Remove from favorites" : "Add to favorites"));
    }

    private static Blend createFavoriteTintEffect() {
        Blend yellowTint = new Blend(BlendMode.SRC_ATOP);
        yellowTint.setTopInput(new ColorInput(0, 0, 19, 19, Color.web("#ffd43b")));
        return yellowTint;
    }

    private Button createRemoveButton(File mediaFile) {
        Button removeButton = new Button("×");
        removeButton.getStyleClass().add("delete-button");
        removeButton.setTooltip(new Tooltip("Remove from playlist"));
        removeButton.setOnAction(event -> {
            removeMedia(mediaFile);
            event.consume();
        });
        return removeButton;
    }

    private Button createControlButton(String tooltipText, boolean isPrimary) {
        Button button = new Button();
        button.getStyleClass().add(isPrimary ? "ethera-icon-button playback-primary" : "ethera-icon-button");
        button.setTooltip(new Tooltip(tooltipText));

        Node icon = createControlIcon(tooltipText, isPrimary ? 30 : 23);
        if (icon != null) {
            button.setGraphic(icon);
        }

        switch (tooltipText) {
            case "Previous" -> button.setOnAction(event -> previousTrack());
            case "Play/Pause" -> button.setOnAction(event -> togglePlayPause());
            case "Stop" -> button.setOnAction(event -> stopPlayback());
            case "Next" -> button.setOnAction(event -> nextTrack());
            case "Mute" -> button.setOnAction(event -> toggleMute());
            case "Fullscreen" -> button.setOnAction(event -> toggleFullscreen(button.getScene()));
            case "Subtitles" -> button.setOnAction(event -> openSubtitleMenu(button.getScene()));
            case "Settings" -> button.setOnAction(event -> openSettingsDialog());
            case "Playlist" -> button.setOnAction(event -> showSection("PLAYLIST"));
            case "Shuffle" -> button.setOnAction(event -> toggleShuffle());
            case "Repeat" -> button.setOnAction(event -> toggleRepeat());
            case "Video" -> button.setOnAction(event -> statusLabel.setText("Video display is ready"));
            default -> { }
        }

        return button;
    }

    private Node createControlIcon(String tooltipText, double size) {
        String iconFileName = switch (tooltipText) {
            case "Previous" -> "previous.png";
            case "Play/Pause" -> "play-button-arrowhead.png";
            case "Next" -> "forward-button.png";
            case "Shuffle" -> "shuffle.png";
            case "Repeat" -> "repeat.png";
            case "Playlist" -> "playlist.png";
            case "Mute" -> muted ? "volume-mute.png" : "speaker-filled-audio-tool.png";
            case "Settings" -> "setting.png";
            case "Video" -> "video-camera.png";
            case "Fullscreen" -> "expand.png";
            case "Subtitles" -> "subtitle.png";
            default -> null;
        };

        if (iconFileName == null) {
            return null;
        }
        return loadIconImage(iconFileName, size);
    }

    private Button createNavItem(String label, boolean selected) {
        String iconName = switch (label) {
            case "Home" -> "home.png";
            case "Playlist" -> "playlist.png";
            case "Local Network" -> "web.png";
            case "Internet Radio" -> "radio.png";
            case "Devices" -> "electronics.png";
            case "Library" -> "video-camera.png";
            case "Music" -> "music-album.png";
            case "Videos" -> "video-camera.png";
            case "Images" -> "image.png";
            case "Playlists" -> "playlist.png";
            case "Favorites" -> "bookmark.png";
            case "Settings" -> "setting.png";
            default -> "home.png";
        };
        Button item = new Button();
        item.setGraphic(loadIconImage(iconName, 18));
        item.setText(label);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add(selected ? "ethera-nav-item active" : "ethera-nav-item");
        return item;
    }

    private Node loadIconImage(String iconIdentifier, double size) {
        String fileName = iconIdentifier == null ? "" : iconIdentifier.trim();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".png")) {
            fileName = fileName + ".png";
        }

        String resourcePath = "/com/example/etheramediaplayer/icons/" + fileName;
        var resource = MainController.class.getResource(resourcePath);
        if (resource != null) {
            Image image = new Image(resource.toExternalForm(), size, size, true, true, true);
            if (!image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setSmooth(true);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("ethera-icon-image");
                if ("modern-dark".equals(selectedTheme)) {
                    imageView.setEffect(createWhiteIconEffect());
                }
                return imageView;
            }
        }

        Label label = new Label("•");
        label.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: white; -fx-alignment: center;");
        label.setMinSize(size, size);
        label.setPrefSize(size, size);
        label.setMaxSize(size, size);
        return label;
    }

    private StackPane createThumbnail(File mediaFile) {
        StackPane thumbnailBox = new StackPane();
        thumbnailBox.setPrefSize(72, 40);
        thumbnailBox.setMinSize(72, 40);
        thumbnailBox.setMaxSize(72, 40);
        thumbnailBox.getStyleClass().add(isVideoFile(mediaFile) ? "video-thumbnail" : "media-thumbnail");
        thumbnailBox.getProperties().put("media-file", mediaFile);

        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(72);
        thumbnail.setFitHeight(40);
        thumbnail.setPreserveRatio(true);

        File imageFile = findNearbyArtwork(mediaFile);
        if (imageFile != null) {
            thumbnail.setImage(new Image(imageFile.toURI().toString(), 96, 64, true, true, true));
            thumbnailBox.getChildren().add(thumbnail);
        } else if (isVideoFile(mediaFile)) {
            thumbnailBox.getChildren().add(loadIconImage("video-camera.png", 18));
        } else {
            thumbnailBox.getChildren().add(loadIconImage("music-album.png", 18));
        }
        return thumbnailBox;
    }

    private void loadEmbeddedThumbnail(File mediaFile, StackPane thumbnailBox) {
        if (findNearbyArtwork(mediaFile) != null || isVideoFile(mediaFile)) {
            return;
        }
        try {
            Media metadataMedia = new Media(mediaFile.toURI().toString());
            metadataMedia.setOnError(() -> { });
            metadataMedia.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
                Object imageValue = metadataMedia.getMetadata().get("image");
                if (imageValue instanceof Image image && !image.isError()) {
                    Platform.runLater(() -> {
                        if (mediaFile.equals(thumbnailBox.getProperties().get("media-file"))) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(72);
                            imageView.setFitHeight(40);
                            imageView.setPreserveRatio(true);
                            thumbnailBox.getChildren().setAll(imageView);
                        }
                    });
                }
            });
        } catch (RuntimeException ignored) {
        }
    }

    private File findNearbyArtwork(File mediaFile) {
        String name = mediaFile.getName();
        int extensionIndex = name.lastIndexOf('.');
        String stem = extensionIndex > 0 ? name.substring(0, extensionIndex) : name;
        File directory = mediaFile.getParentFile();
        if (directory == null) {
            return null;
        }
        for (String extension : List.of(".jpg", ".jpeg", ".png", ".webp")) {
            File artwork = new File(directory, stem + extension);
            if (artwork.isFile()) {
                return artwork;
            }
        }
        return null;
    }

    private Button createCountNavItem(String iconFile, String label, boolean selected) {
        Button item = createNavItem(label, selected);
        Label count = new Label("0");
        count.getStyleClass().add("nav-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox content = new HBox(10, loadIconImage(iconFile, 17), new Label(label), spacer, count);
        content.setAlignment(Pos.CENTER_LEFT);
        item.setText(null);
        item.setGraphic(content);
        navigationCounts.put(label, count);
        return item;
    }

    private void refreshNavigationCounts() {
        long musicCount = playlist.stream().filter(file -> !isVideoFile(file)).count();
        long videoCount = playlist.stream().filter(this::isVideoFile).count();
        setNavigationCount("My Playlist", playlist.size());
        setNavigationCount("Recently Added", playlist.size());
        setNavigationCount("Recently Played", 0);
        setNavigationCount("Top Rated", favoritesDatabase.findFavorites().size());
        setNavigationCount("Music", musicCount);
        setNavigationCount("Videos", videoCount);
        setNavigationCount("Images", 0);
    }

    private void setNavigationCount(String label, long count) {
        Label countLabel = navigationCounts.get(label);
        if (countLabel != null) {
            countLabel.setText(Long.toString(count));
        }
    }

    private void configurePlaylistView() {
        playlistView.setFocusTraversable(false);
    }

    private void configureTimeline() {
        timelineSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (mediaPlayer == null || !timelineSlider.isValueChanging()) {
                return;
            }
            if (mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });

        timelineSlider.setOnMouseReleased(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
                mediaPlayer.seek(Duration.seconds(timelineSlider.getValue()));
            }
        });
    }

    private void configureVolume() {
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            playerVolume = newValue.doubleValue() / 100.0;
            if (mediaPlayer != null) {
                if (!muted) {
                    mediaPlayer.setVolume(playerVolume);
                }
            }
            statusLabel.setText(String.format("Volume: %d%%", newValue.intValue()));
            showPlaybackHud("Volume " + newValue.intValue() + "%");
        });
    }

    public void installKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (isTextInputFocused(scene)) {
                return;
            }

            switch (event.getCode()) {
                case SPACE -> {
                    event.consume();
                    togglePlayPause();
                }
                case S -> {
                    event.consume();
                    stopPlayback();
                }
                case T -> {
                    event.consume();
                    openSubtitleMenu(scene);
                }
                case N -> {
                    event.consume();
                    nextTrack();
                }
                case P -> {
                    event.consume();
                    previousTrack();
                }
                case UP -> {
                    event.consume();
                    increaseVolume();
                }
                case DOWN -> {
                    event.consume();
                    decreaseVolume();
                }
                case LEFT -> {
                    event.consume();
                    seekBy(-10);
                }
                case RIGHT -> {
                    event.consume();
                    seekBy(10);
                }
                case M -> {
                    event.consume();
                    toggleMute();
                }
                case F -> {
                    event.consume();
                    toggleFullscreen(scene);
                }
                case DELETE -> {
                    event.consume();
                    removeSelectedMedia();
                }
                default -> { }
            }
        });
    }

    private boolean isTextInputFocused(Scene scene) {
        Node focusOwner = scene.getFocusOwner();
        return focusOwner instanceof TextField;
    }

    private void filterPlaylist(String query) {
        String trimmed = query == null ? "" : query.trim();
        Predicate<File> sectionFilter = switch (activeSection) {
            case "MUSIC" -> file -> !isVideoFile(file);
            case "VIDEOS" -> this::isVideoFile;
            case "FAVORITES" -> favoritesDatabase::isFavorite;
            default -> file -> true;
        };

        if (trimmed.isEmpty()) {
            playlistView.setItems(playlist.filtered(sectionFilter));
            return;
        }

        String lowerQuery = trimmed.toLowerCase(Locale.ROOT);
        ObservableList<File> filtered = FXCollections.observableArrayList();
        for (File file : playlist) {
            if (sectionFilter.test(file) && file.getName().toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                filtered.add(file);
            }
        }
        playlistView.setItems(filtered);
    }

    private void showSection(String section) {
        activeSection = section;
        if (playlistPanelTitle != null) {
            playlistPanelTitle.setText(section.equals("MUSIC") ? "MUSIC"
                    : section.equals("VIDEOS") ? "VIDEOS"
                    : section.equals("FAVORITES") ? "FAVORITES" : "PLAYLIST");
        }
        refreshPlaylistRail();
        sectionLabel.setText(section);
        statusLabel.setText("Viewing " + section.toLowerCase());
        switch (section) {
            case "HOME" -> pageHost.getChildren().setAll(playerPage);
            case "LIBRARY", "MUSIC", "VIDEOS", "PLAYLIST", "FAVORITES" ->
                    pageHost.getChildren().setAll(createMediaPage(section));
            case "RECENT" -> pageHost.getChildren().setAll(createEmptyPage(section));
            default -> { }
        }
    }

    private VBox createMediaPage(String section) {
        Predicate<File> filter = switch (section) {
            case "MUSIC" -> file -> !isVideoFile(file);
            case "VIDEOS" -> this::isVideoFile;
            case "FAVORITES" -> file -> favoritesDatabase.isFavorite(file);
            default -> file -> true;
        };

        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.getStyleClass().add("library-page");

        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        String iconFile = "LIBRARY".equals(section) ? "video-camera.png"
                : ("VIDEOS".equals(section) ? "video-camera.png"
                : ("PLAYLIST".equals(section) ? "playlist.png"
                : ("FAVORITES".equals(section) ? "bookmark.png" : "music-album.png")));
        Node iconView = loadIconImage(iconFile, 24);
        iconView.getStyleClass().add("page-icon");
        Label title = new Label(section);
        title.getStyleClass().add("page-title");
        heading.getChildren().addAll(iconView, title);

        Label subtitle = new Label("Choose a media item to open it in the player.");
        subtitle.getStyleClass().add("page-subtitle");

        ListView<File> mediaList = new ListView<>();
        mediaList.setItems(playlist.filtered(filter));
        mediaList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                StackPane thumbnail = createThumbnail(item);
                loadEmbeddedThumbnail(item, thumbnail);
                Label fileLabel = new Label(item.getName());
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox row = new HBox(8, thumbnail, fileLabel, spacer,
                    createFavoriteButton(item), createRemoveButton(item));
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
        mediaList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File selected = mediaList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    currentIndex = playlist.indexOf(selected);
                    showSection("HOME");
                    playSelectedMedia(selected, currentIndex);
                }
            }
        });
        VBox.setVgrow(mediaList, Priority.ALWAYS);

        Button openButton = createIconTextButton("Open Media", "play-button-arrowhead.png", "ethera-secondary-button");
        openButton.setOnAction(event -> addMediaFiles());

        Button openFolderButton = createIconTextButton("Open Folder", "folder.png", "ethera-secondary-button");
        openFolderButton.setOnAction(event -> addFolder());
        page.getChildren().addAll(heading, subtitle, mediaList, openButton, openFolderButton);
        return page;
    }

    private Button createIconTextButton(String text, String iconName, String styleClass) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(loadIconImage(iconName, 15), new Label(text));
        button.setGraphic(content);
        return button;
    }

    private void refreshPlaylistRail() {
        if ("MUSIC".equals(activeSection)) {
            playlistView.setItems(playlist.filtered(file -> !isVideoFile(file)));
        } else if ("VIDEOS".equals(activeSection)) {
            playlistView.setItems(playlist.filtered(this::isVideoFile));
        } else {
            playlistView.setItems(playlist);
        }
    }

    private VBox createEmptyPage(String section) {
        VBox page = new VBox(14);
        page.setAlignment(Pos.CENTER);
        page.getStyleClass().add("library-page");

        Node iconView = loadIconImage("RECENT".equals(section) ? "clock.png" : "bookmark.png", 36);
        iconView.getStyleClass().add("empty-page-icon");
        Label title = new Label("FAVORITES".equals(section) ? "No favorites yet" : "Nothing played yet");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Your listening history will appear here.");
        subtitle.getStyleClass().add("page-subtitle");
        page.getChildren().addAll(iconView, title, subtitle);
        return page;
    }

    private static String resolveStylesheetUrl() {
        var url = MainController.class.getResource("/com/example/etheramediaplayer/ethera.css");
        if (url == null) {
            url = MainController.class.getClassLoader().getResource("com/example/etheramediaplayer/ethera.css");
        }
        if (url == null) {
            throw new IllegalStateException("Unable to locate stylesheet: com/example/etheramediaplayer/ethera.css");
        }
        return url.toExternalForm();
    }

    private void openSettingsDialog() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Ethera Settings");
        settingsStage.setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("settings-dialog");
        applyTheme(root, selectedTheme == null ? "soft-light" : selectedTheme);

        Label title = new Label("Settings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.getStyleClass().add("settings-title");

        Label themeLabel = new Label("Theme");
        themeLabel.getStyleClass().add("settings-label");
        ComboBox<String> themeChoice = new ComboBox<>();
        themeChoice.getItems().addAll(
                "Ethera Original",
                "Modern Dark",
                "Soft-Light",
                "Golden Hour",
                "iOS 26 Liquid");
        Map<String, String> themeIds = Map.of(
                "Ethera Original", "ethera-original",
                "Modern Dark", "modern-dark",
                "Soft-Light", "soft-light",
                "Golden Hour", "golden-hour",
                "iOS 26 Liquid", "ios-liquid");
        String savedTheme = selectedTheme == null ? "soft-light" : selectedTheme;
        String savedThemeLabel = themeIds.entrySet().stream()
                .filter(entry -> entry.getValue().equals(savedTheme))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Soft-Light");
        themeChoice.getStyleClass().add("theme-choice");
        themeChoice.getSelectionModel().select(savedThemeLabel);
        themeChoice.setPrefWidth(220);
        themeChoice.setOnAction(event -> {
            String previewTheme = themeIds.get(themeChoice.getValue());
            applyTheme(mainScene == null ? null : mainScene.getRoot(), previewTheme);
            applyTheme(root, previewTheme);
        });

        CheckBox autoplayCheck = new CheckBox("Auto-play next");
        autoplayCheck.setSelected(autoPlayNext);
        autoplayCheck.getStyleClass().add("settings-check");

        Label volumeLabel = new Label("Playback volume");
        volumeLabel.getStyleClass().add("settings-label");
        Slider settingsVolumeSlider = new Slider(0, 100, volumeSlider.getValue());
        settingsVolumeSlider.setPrefWidth(220);

        CheckBox loadSystemFilesCheck = new CheckBox("Auto-load media from common laptop folders");
        loadSystemFilesCheck.setSelected(true);
        loadSystemFilesCheck.getStyleClass().add("settings-check");

        Label subtitleTitle = new Label("Subtitles");
        subtitleTitle.getStyleClass().add("settings-section-title");
        ComboBox<String> subtitleChoice = new ComboBox<>();
        subtitleChoice.getItems().add("Off");
        if (selectedSubtitleFile != null) {
            subtitleChoice.getItems().add(selectedSubtitleFile.getName());
            subtitleChoice.getSelectionModel().select(1);
        } else {
            subtitleChoice.getSelectionModel().selectFirst();
        }
        Button chooseSubtitleButton = new Button("Choose subtitle file");
        chooseSubtitleButton.getStyleClass().add("ethera-secondary-button");
        chooseSubtitleButton.setOnAction(event -> chooseSubtitleFile());

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("ethera-primary-button");
        saveButton.setOnAction(event -> {
            autoPlayNext = autoplayCheck.isSelected();
            volumeSlider.setValue(settingsVolumeSlider.getValue());
            selectedTheme = themeIds.get(themeChoice.getValue());
            applyTheme(mainScene == null ? null : mainScene.getRoot(), selectedTheme);
            if (loadSystemFilesCheck.isSelected()) {
                loadAutoDetectedMediaAsync();
            }
            settingsStage.close();
            statusLabel.setText("Settings saved");
        });

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("ethera-secondary-button");
        closeButton.setOnAction(event -> settingsStage.close());

        Button resetButton = new Button("Reset to Default");
        resetButton.getStyleClass().add("settings-reset-button");
        resetButton.setOnAction(event -> {
            selectedTheme = "soft-light";
            themeChoice.getSelectionModel().select("Soft-Light");
            applyTheme(mainScene == null ? null : mainScene.getRoot(), selectedTheme);
            applyTheme(root, selectedTheme);
            statusLabel.setText("Default theme restored");
        });

        HBox actions = new HBox(12, resetButton, saveButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        root.getChildren().addAll(title, themeLabel, themeChoice, autoplayCheck, volumeLabel, settingsVolumeSlider,
                loadSystemFilesCheck, subtitleTitle, subtitleChoice, chooseSubtitleButton, actions);

        Scene scene = new Scene(root, 480, 500);
        scene.getStylesheets().add(resolveStylesheetUrl());
        if ("light".equals(selectedTheme) || "soft-light".equals(selectedTheme)) {
            scene.setFill(javafx.scene.paint.Color.WHITE);
        }
        settingsStage.setScene(scene);
        settingsStage.show();
    }

    private void openSubtitleMenu(Scene ownerScene) {
        Stage subtitleStage = new Stage();
        subtitleStage.setTitle("Subtitle Selection");
        subtitleStage.initOwner(ownerScene.getWindow());
        subtitleStage.setResizable(false);

        VBox root = new VBox(14);
        root.getStyleClass().add("subtitle-dialog");
        root.setPadding(new Insets(20));
        Label title = new Label("Subtitle Selection");
        title.getStyleClass().add("settings-title");
        Label description = new Label("Choose an SRT or WebVTT file to display over the video.");
        description.setWrapText(true);
        description.getStyleClass().add("settings-description");
        VBox detectedOptions = new VBox(6);
        if (detectedSubtitleFiles.isEmpty()) {
            Label noneDetected = new Label("No subtitles detected beside this video.");
            noneDetected.getStyleClass().add("settings-description");
            detectedOptions.getChildren().add(noneDetected);
        } else {
            Label detectedTitle = new Label("Detected subtitles");
            detectedTitle.getStyleClass().add("settings-section-title");
            detectedOptions.getChildren().add(detectedTitle);
            for (File subtitleFile : detectedSubtitleFiles) {
                Button subtitleOption = new Button(subtitleFile.getName());
                subtitleOption.setMaxWidth(Double.MAX_VALUE);
                subtitleOption.setAlignment(Pos.CENTER_LEFT);
                subtitleOption.getStyleClass().add("subtitle-option");
                subtitleOption.setOnAction(event -> {
                    loadSubtitleFile(subtitleFile);
                    subtitleStage.close();
                });
                detectedOptions.getChildren().add(subtitleOption);
            }
        }
        Button choose = new Button("Choose subtitle file");
        choose.getStyleClass().add("ethera-primary-button");
        choose.setOnAction(event -> {
            chooseSubtitleFile();
            subtitleStage.close();
        });
        Button disable = new Button("Disable subtitles");
        disable.getStyleClass().add("ethera-secondary-button");
        disable.setOnAction(event -> {
            selectedSubtitleFile = null;
            subtitleCues = List.of();
            subtitleLabel.setText("");
            subtitleLabel.setVisible(false);
            subtitleStage.close();
            statusLabel.setText("Subtitles disabled");
        });
        root.getChildren().addAll(title, description, detectedOptions, choose, disable);
        Scene scene = new Scene(root, 430, detectedSubtitleFiles.isEmpty() ? 260 : 320);
        scene.getStylesheets().add(resolveStylesheetUrl());
        subtitleStage.setScene(scene);
        subtitleStage.show();
    }

    private void chooseSubtitleFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Subtitle File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Subtitle Files", "*.srt", "*.vtt"));
        File file = chooser.showOpenDialog(mediaDisplay.getScene().getWindow());
        if (file == null) {
            return;
        }
        loadSubtitleFile(file);
    }

    private void loadSubtitleFile(File file) {
        try {
            selectedSubtitleFile = file;
            subtitleCues = subtitleService.parse(file);
            statusLabel.setText("Loaded subtitles: " + file.getName());
            showPlaybackHud("Subtitles on");
        } catch (Exception exception) {
            selectedSubtitleFile = null;
            subtitleCues = List.of();
            statusLabel.setText("Could not read subtitle file");
        }
    }

    private void updateSubtitle(double seconds) {
        SubtitleCue activeCue = subtitleCues.stream()
                .filter(cue -> cue.contains(seconds))
                .findFirst()
                .orElse(null);
        subtitleLabel.setText(activeCue == null ? "" : activeCue.text());
        subtitleLabel.setVisible(activeCue != null && videoMediaActive);
    }

    private void loadMatchingSubtitle(File mediaFile) {
        selectedSubtitleFile = null;
        subtitleCues = List.of();
        detectedSubtitleFiles = List.of();
        String name = mediaFile.getName();
        int extensionIndex = name.lastIndexOf('.');
        String stem = extensionIndex > 0 ? name.substring(0, extensionIndex) : name;
        String normalizedStem = stem.toLowerCase(Locale.ROOT);
        File directory = mediaFile.getParentFile();
        if (directory == null || directory.listFiles() == null) {
            return;
        }

        List<File> matches = new ArrayList<>();
        for (File candidate : directory.listFiles()) {
            if (!candidate.isFile() || !subtitleService.isSupported(candidate)) {
                continue;
            }
            String candidateName = candidate.getName().toLowerCase(Locale.ROOT);
            int candidateExtensionIndex = candidateName.lastIndexOf('.');
            String candidateStem = candidateExtensionIndex > 0
                    ? candidateName.substring(0, candidateExtensionIndex)
                    : candidateName;
            if (candidateStem.equals(normalizedStem)
                    || candidateStem.startsWith(normalizedStem + ".")
                    || candidateStem.startsWith(normalizedStem + " ")) {
                matches.add(candidate);
            }
        }
        matches.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        detectedSubtitleFiles = List.copyOf(matches);

        for (File candidate : detectedSubtitleFiles) {
            try {
                selectedSubtitleFile = candidate;
                subtitleCues = subtitleService.parse(candidate);
                statusLabel.setText("Loaded subtitles: " + candidate.getName());
                return;
            } catch (Exception ignored) {
                selectedSubtitleFile = null;
                subtitleCues = List.of();
            }
        }
    }

    private void loadAutoDetectedMediaAsync() {
        Task<List<File>> scanTask = new Task<>() {
            @Override
            protected List<File> call() {
                updateProgress(0, 1);
                return mediaLibraryService.discoverLocalMedia(Integer.MAX_VALUE,
                        progress -> updateProgress(Math.max(0.01, Math.min(0.99, progress)), 1));
            }
        };
        scanTask.progressProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0) {
                int percent = Math.max(1, Math.min(99,
                        (int) Math.round(newValue.doubleValue() * 100)));
                mediaLoadingLabel.setText("Loading local media " + percent + "%");
                mediaLoadingLabel.setVisible(true);
            }
        });
        scanTask.setOnSucceeded(event -> {
            int added = 0;
            for (File file : scanTask.getValue()) {
                if (!playlist.contains(file)) {
                    playlist.add(file);
                    added++;
                }
            }
            refreshNavigationCounts();
            mediaLoadingLabel.setText("Local media loaded 100%");
            mediaLoadingLabel.setVisible(true);
            if (added > 0) {
                statusLabel.setText("Found " + added + " local media files");
            }
        });
        scanTask.setOnFailed(event -> {
            mediaLoadingLabel.setVisible(false);
            statusLabel.setText("Local media scan unavailable");
        });
        Thread scanner = new Thread(scanTask, "ethera-media-scan");
        scanner.setDaemon(true);
        scanner.start();
    }

    private void addMediaFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Media");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
            "Supported Media (MP4 video)", "*.mp3", "*.mp4", "*.m4a", "*.wav", "*.aac",
            "*.flac", "*.ogg", "*.oga", "*.opus", "*.wma", "*.aiff"));

        List<File> files = chooser.showOpenMultipleDialog(null);
        if (files == null || files.isEmpty()) {
            return;
        }

        addFilesToPlaylist(files);
    }

    private void addFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open Media Folder");
        File folder = chooser.showDialog(null);
        if (folder == null || !folder.isDirectory()) {
            return;
        }

        List<File> folderFiles = new ArrayList<>();
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child != null && child.isFile() && mediaLibraryService.isSupportedMedia(child)) {
                    folderFiles.add(child);
                }
            }
        }

        if (folderFiles.isEmpty()) {
            statusLabel.setText("No supported music or video files found in this folder");
            return;
        }

        addFilesToPlaylist(folderFiles);
    }

    private void addFilesToPlaylist(List<File> files) {
        File fileToPlay = null;
        for (File file : files) {
            if (file == null || !mediaLibraryService.isSupportedMedia(file) || playlist.contains(file)) {
                continue;
            }
            playlist.add(file);
            if (fileToPlay == null) {
                fileToPlay = file;
            }
        }

        refreshNavigationCounts();
        if (fileToPlay != null) {
            int index = playlist.indexOf(fileToPlay);
            playlistView.getSelectionModel().select(index);
            playSelectedMedia(fileToPlay, index);
        } else {
            statusLabel.setText("Media already added");
        }
    }

    private void removeSelectedMedia() {
        File selected = playlistView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        removeMedia(selected);
    }

    private void removeMedia(File mediaFile) {
        if (mediaFile == null) {
            return;
        }

        int removedIndex = playlist.indexOf(mediaFile);
        if (removedIndex < 0) {
            return;
        }
        boolean removingCurrent = removedIndex == currentIndex;
        playlist.remove(removedIndex);
        refreshNavigationCounts();
        if (playlist.isEmpty()) {
            stopPlayback();
            currentIndex = -1;
            mainTitleLabel.setText("Ethera Media Player");
            mediaMetaLabel.setText("Nothing is playing");
            return;
        }

        if (!removingCurrent) {
            if (removedIndex < currentIndex) {
                currentIndex--;
            }
            playlistView.getSelectionModel().select(currentIndex);
            statusLabel.setText("Removed from playlist");
            return;
        }

        if (removedIndex >= playlist.size()) {
            currentIndex = playlist.size() - 1;
        } else {
            currentIndex = removedIndex;
        }

        playlistView.getSelectionModel().select(currentIndex);
        playSelectedMedia(playlist.get(currentIndex), currentIndex);
    }

    private void playSelectedMedia(File file, int index) {
        if (file == null || !file.exists()) {
            statusLabel.setText("Media file not found");
            return;
        }

        currentIndex = index;
        if (playbackFavoriteButton != null) {
            playbackFavoriteButton.setDisable(false);
            updateFavoriteButton(playbackFavoriteButton, favoritesDatabase.isFavorite(file));
        }
        if (fullscreenFavoriteButton != null) {
            fullscreenFavoriteButton.setDisable(false);
            updateFavoriteButton(fullscreenFavoriteButton, favoritesDatabase.isFavorite(file));
        }
        long generation = ++playbackGeneration;
        if (mediaPlayer != null) {
            mediaPlayer.setOnReady(null);
            mediaPlayer.setOnError(null);
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        boolean video = isVideoFile(file);
        mediaView.setVisible(false);
        musicDisc.setVisible(false);
        musicDiscRotation.stop();
        emptyState.setVisible(true);
        videoMediaActive = video;
        updateSubtitleButtonVisibility();
        playSelectedMediaWithSource(file, index, generation, file.toURI().toString());
    }

    private void playSelectedMediaWithSource(File file, int index, long generation, String mediaSource) {
        try {
            boolean video = isVideoFile(file);
            Media media = new Media(mediaSource);
            media.setOnError(() -> {
                if (mediaPlayer != null && playbackGeneration == generation) {
                    showPlaybackFailure(playbackErrorMessage(file));
                }
            });
            MediaPlayer player = new MediaPlayer(media);
            mediaPlayer = player;
            player.setVolume(playerVolume);
            player.setMute(muted);
            player.setRate(1.0);
            player.setBalance(0.0);
            player.setOnReady(() -> {
                if (!isActivePlayer(player, generation)) {
                    return;
                }
                Duration duration = player.getTotalDuration();
                totalTimeLabel.setText(formatDuration(duration));
                timelineSlider.setMax(duration.toSeconds());
                updateArtwork(media, file);
                mediaView.setVisible(video);
                musicDisc.setVisible(!video);
                emptyState.setVisible(false);
                statusLabel.setText("Ready");
                updatePlayPauseButtons(false);
                player.play();
            });
            player.setAudioSpectrumNumBands(64);
            player.setAudioSpectrumInterval(0.05);
            player.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                if (!isActivePlayer(player, generation)
                        || player.getStatus() != MediaPlayer.Status.PLAYING) {
                    return;
                }
                double peakDb = -60;
                double volumeGain = muted ? 0 : playerVolume;
                if (volumeGain > 0) {
                    double volumeDb = 20 * Math.log10(volumeGain);
                    for (float magnitude : magnitudes) {
                        if (Float.isFinite(magnitude)) {
                            peakDb = Math.max(peakDb, magnitude + volumeDb);
                        }
                    }
                }
                audioMeterLevelDb = Math.max(-60, Math.min(0, peakDb));
                drawAudioLevelMeter();
            });

            player.setOnPlaying(() -> {
                if (!isActivePlayer(player, generation)) {
                    return;
                }
                statusLabel.setText("Playing");
                updatePlayPauseButtons(true);
                if (!video) {
                    musicDiscRotation.play();
                }
                mainTitleLabel.setText(file.getName());
                mediaMetaLabel.setText(formatMediaMetadata(media, file));
            });

            player.setOnPaused(() -> {
                if (isActivePlayer(player, generation)) {
                    resetAudioLevelMeter();
                    statusLabel.setText("Paused");
                    updatePlayPauseButtons(false);
                    musicDiscRotation.pause();
                }
            });
            player.setOnStopped(() -> {
                if (isActivePlayer(player, generation)) {
                    resetAudioLevelMeter();
                    statusLabel.setText("Stopped");
                    musicDiscRotation.stop();
                }
            });
            player.setOnEndOfMedia(() -> {
                if (!isActivePlayer(player, generation)) {
                    return;
                }
                if (repeatEnabled) {
                    player.seek(Duration.ZERO);
                    player.play();
                    return;
                }
                if (autoPlayNext && !playlist.isEmpty()) {
                    nextTrack();
                } else {
                    stopPlayback();
                }
            });
            player.setOnError(() -> {
                if (isActivePlayer(player, generation)) {
                    showPlaybackFailure(playbackErrorMessage(file));
                }
            });

            media.getMetadata().addListener((MapChangeListener<String, Object>) change ->
                    Platform.runLater(() -> {
                        if (isActivePlayer(player, generation)) {
                            updateArtwork(media, file);
                        }
                    }));

            player.currentTimeProperty().addListener((obs, old, value) -> {
                if (!isActivePlayer(player, generation)) {
                    return;
                }
                currentTimeLabel.setText(formatDuration(value));
                if (!timelineSlider.isValueChanging()) {
                    timelineSlider.setValue(value.toSeconds());
                }
                updateSubtitle(value.toSeconds());
            });

            player.totalDurationProperty().addListener((obs, oldValue, newValue) -> {
                if (!isActivePlayer(player, generation)) {
                    return;
                }
                if (newValue != null && !newValue.isUnknown()) {
                    totalTimeLabel.setText(formatDuration(newValue));
                    timelineSlider.setMax(newValue.toSeconds());
                }
            });

            mediaView.setMediaPlayer(mediaPlayer);
            videoMediaActive = video;
            updateSubtitleButtonVisibility();
            loadMatchingSubtitle(file);
            currentIndex = index;
            playlistView.getSelectionModel().select(index);
            statusLabel.setText(video ? "Preparing video..." : "Loading media...");
        } catch (Exception ex) {
            if (isActivePlayer(mediaPlayer, generation)) {
                mediaPlayer = null;
                String message = ex.getMessage();
                showPlaybackFailure(message == null || message.isBlank()
                    ? "Unable to start media playback"
                        : message);
            }
        }
    }

    private void showPlaybackFailure(String message) {
        resetAudioLevelMeter();
        if (mediaPlayer != null) {
            mediaPlayer.setOnReady(null);
            mediaPlayer.setOnError(null);
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        mediaView.setVisible(false);
        musicDisc.setVisible(false);
        musicDiscRotation.stop();
        emptyState.setVisible(true);
        videoMediaActive = false;
        updateSubtitleButtonVisibility();
        mainTitleLabel.setText("Ethera Media Player");
        mediaMetaLabel.setText("Nothing is playing");
        statusLabel.setText(message);
    }

    private boolean isActivePlayer(MediaPlayer player, long generation) {
        return player != null && mediaPlayer == player && playbackGeneration == generation;
    }

    private void resetAudioLevelMeter() {
        audioMeterLevelDb = -60;
        drawAudioLevelMeter();
    }

    private void drawAudioLevelMeter() {
        GraphicsContext graphics = audioLevelMeter.getGraphicsContext2D();
        double width = audioLevelMeter.getWidth();
        double height = audioLevelMeter.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        graphics.clearRect(0, 0, width, height);
        double barY = 1;
        double barHeight = 13;
        graphics.setFill(Color.web("#17212b"));
        graphics.fillRoundRect(0, barY, width, barHeight, 5, 5);

        double levelWidth = width * (audioMeterLevelDb + 60) / 60;
        if (levelWidth > 0) {
            javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.web("#31d17c")),
                    new javafx.scene.paint.Stop(0.72, Color.web("#a8db43")),
                    new javafx.scene.paint.Stop(0.88, Color.web("#ffbd3d")),
                    new javafx.scene.paint.Stop(1, Color.web("#f04444")));
            graphics.setFill(gradient);
            graphics.fillRoundRect(0, barY, levelWidth, barHeight, 5, 5);
        }

        graphics.setFont(javafx.scene.text.Font.font("Segoe UI", 9));
        graphics.setFill(Color.web("#9eacc0"));
        for (int db = -60; db <= 0; db += 12) {
            double x = width * (db + 60) / 60;
            graphics.setStroke(Color.web("rgba(210, 225, 240, 0.42)"));
            graphics.setLineWidth(1);
            graphics.strokeLine(x, barY + barHeight + 2, x, barY + barHeight + 6);
            String tick = Integer.toString(db);
            double textX = Math.max(0, Math.min(width - 22, x - 9));
            graphics.fillText(tick, textX, height - 2);
        }

        audioLevelLabel.setText(audioMeterLevelDb <= -60
                ? "-∞ dB"
                : String.format(Locale.ROOT, "%.0f dB", audioMeterLevelDb));
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            if (!playlist.isEmpty()) {
                playSelectedMedia(playlist.get(0), 0);
            }
            return;
        }

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            showPlaybackHud("Ⅱ  Paused");
        } else {
            mediaPlayer.play();
            showPlaybackHud("▶  Playing");
        }
    }

    private void updatePlayPauseButtons(boolean playing) {
        if (playPauseButton != null) {
            String iconFile = playing ? "pause.png" : "play-button-arrowhead.png";
            Node iconView = loadIconImage(iconFile, 30);
            playPauseButton.setGraphic(iconView);
        }
        if (fullscreenPlayPauseButton != null) {
            String iconFile = playing ? "pause.png" : "play-button-arrowhead.png";
            fullscreenPlayPauseButton.setGraphic(loadIconImage(iconFile, 30));
        }
    }

    private void stopPlayback() {
        playbackGeneration++;
        resetAudioLevelMeter();
        if (mediaPlayer != null) {
            mediaPlayer.setOnReady(null);
            mediaPlayer.setOnError(null);
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        mediaView.setVisible(false);
        videoMediaActive = false;
        updateSubtitleButtonVisibility();
        subtitleLabel.setText("");
        subtitleLabel.setVisible(false);
        musicDisc.setVisible(false);
        musicDiscRotation.stop();
        emptyState.setVisible(true);
        mainTitleLabel.setText("Ethera Media Player");
        mediaMetaLabel.setText("Nothing is playing");
        currentTimeLabel.setText("00:00");
        totalTimeLabel.setText("00:00");
        timelineSlider.setValue(0);
        if (playbackFavoriteButton != null) {
            playbackFavoriteButton.setDisable(true);
            updateFavoriteButton(playbackFavoriteButton, false);
        }
        if (fullscreenFavoriteButton != null) {
            fullscreenFavoriteButton.setDisable(true);
            updateFavoriteButton(fullscreenFavoriteButton, false);
        }
        updatePlayPauseButtons(false);
        statusLabel.setText("Stopped");
    }

    private void updateSubtitleButtonVisibility() {
        if (subtitleButton != null) {
            subtitleButton.setVisible(videoMediaActive);
            subtitleButton.setManaged(videoMediaActive);
        }
    }

    private void nextTrack() {
        if (playlist.isEmpty()) {
            stopPlayback();
            return;
        }

        int nextIndex;
        if (shuffleEnabled && playlist.size() > 1) {
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * playlist.size());
            } while (randomIndex == currentIndex);
            nextIndex = randomIndex;
        } else {
            nextIndex = mediaNavigator.nextIndex(currentIndex, playlist.size());
        }
        playSelectedMedia(playlist.get(nextIndex), nextIndex);
    }

    private void previousTrack() {
        if (playlist.isEmpty()) {
            stopPlayback();
            return;
        }

        int previousIndex;
        if (shuffleEnabled && playlist.size() > 1) {
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * playlist.size());
            } while (randomIndex == currentIndex);
            previousIndex = randomIndex;
        } else {
            previousIndex = mediaNavigator.previousIndex(currentIndex, playlist.size());
        }
        playSelectedMedia(playlist.get(previousIndex), previousIndex);
    }

    private void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        statusLabel.setText(shuffleEnabled ? "Shuffle enabled" : "Shuffle disabled");
        showPlaybackHud(shuffleEnabled ? "Shuffle on" : "Shuffle off");
    }

    private void toggleRepeat() {
        repeatEnabled = !repeatEnabled;
        statusLabel.setText(repeatEnabled ? "Repeat enabled" : "Repeat disabled");
        showPlaybackHud(repeatEnabled ? "Repeat on" : "Repeat off");
        if (mediaPlayer != null) {
            mediaPlayer.setCycleCount(repeatEnabled ? MediaPlayer.INDEFINITE : 1);
        }
    }

    private void increaseVolume() {
        double next = Math.min(100, volumeSlider.getValue() + 10);
        volumeSlider.setValue(next);
        showPlaybackHud("Volume " + (int) next + "%");
    }

    private void decreaseVolume() {
        double next = Math.max(0, volumeSlider.getValue() - 10);
        volumeSlider.setValue(next);
        showPlaybackHud("Volume " + (int) next + "%");
    }

    private void toggleMute() {
        muted = !muted;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(muted);
        }
        updateMuteButton(muteButton);
        updateMuteButton(fullscreenMuteButton);
        statusLabel.setText(muted ? "Muted" : "Volume: " + (int) volumeSlider.getValue() + "%");
        showPlaybackHud(muted ? "Muted" : "Volume " + (int) volumeSlider.getValue() + "%");
    }

    private void updateMuteButton(Button button) {
        if (button != null) {
            button.setGraphic(loadIconImage(
                    muted ? "volume-mute.png" : "speaker-filled-audio-tool.png", 23));
        }
    }

    private void toggleFullscreen(Scene scene) {
        if (scene == null || scene.getWindow() == null) {
            return;
        }
        Stage stage = (Stage) scene.getWindow();
        boolean entering = !stage.isFullScreen();
        BorderPane root = findPlayerRoot(scene);
        if (!entering) {
            stage.setFullScreen(false);
            videoFullscreenMode = false;
        }
        if (entering) {
            videoFullscreenMode = videoMediaActive;
            if (root != null && videoMediaActive) {
                setChromeVisible(root, false);
            }
            stage.setFullScreen(true);
        } else if (root != null) {
            setChromeVisible(root, true);
        }
        if (videoMediaActive) {
            setVideoFullscreenLayout(entering);
            if (entering) {
                showFullscreenControls();
            }
        }
    }

    private BorderPane findPlayerRoot(Scene scene) {
        if (scene == null) {
            return null;
        }
        if (scene.getRoot() instanceof BorderPane root) {
            return root;
        }
        if (scene.getRoot() instanceof StackPane shell) {
            return shell.getChildren().stream()
                    .filter(BorderPane.class::isInstance)
                    .map(BorderPane.class::cast)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void setVideoFullscreenLayout(boolean fullscreen) {
        infoCard.setVisible(!fullscreen);
        infoCard.setManaged(!fullscreen);
        seekRow.setVisible(!fullscreen);
        seekRow.setManaged(!fullscreen);
        controlRow.setVisible(!fullscreen);
        controlRow.setManaged(!fullscreen);
        playerPage.setPadding(fullscreen ? Insets.EMPTY : new Insets(18, 18, 0, 18));
        mediaPane.setPadding(fullscreen ? Insets.EMPTY : new Insets(16));
        mediaDisplay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mediaPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mediaPane.getStyleClass().remove("video-fullscreen");
        mediaDisplay.getStyleClass().remove("video-fullscreen");
        if (fullscreen) {
            mediaPane.getStyleClass().add("video-fullscreen");
            mediaDisplay.getStyleClass().add("video-fullscreen");
        } else {
            fullscreenControlsHide.stop();
            fullscreenControlsFade.stop();
            fullscreenControlsOverlay.setVisible(false);
            fullscreenControlsOverlay.setManaged(false);
            fullscreenControlsOverlay.setOpacity(1);
        }

        if (fullscreen) {
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            Platform.runLater(() -> {
                if (videoFullscreenMode) {
                    mediaView.setFitWidth(Math.max(1, mediaDisplay.getWidth()));
                    mediaView.setFitHeight(Math.max(1, mediaDisplay.getHeight()));
                }
            });
        } else {
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(360);
        }
    }

    private void showFullscreenControls() {
        if (!videoFullscreenMode || fullscreenControlsOverlay == null) {
            return;
        }
        fullscreenControlsFade.stop();
        fullscreenControlsOverlay.setManaged(true);
        fullscreenControlsOverlay.setVisible(true);
        fullscreenControlsOverlay.setOpacity(1);
        fullscreenControlsHide.playFromStart();
    }

    private void setChromeVisible(BorderPane root, boolean visible) {
        if (root.getTop() != null) {
            root.getTop().setVisible(visible);
            root.getTop().setManaged(visible);
        }
        if (root.getLeft() != null) {
            root.getLeft().setVisible(visible);
            root.getLeft().setManaged(visible);
        }
        if (root.getRight() != null) {
            root.getRight().setVisible(visible);
            root.getRight().setManaged(visible);
        }
        if (root.getBottom() != null) {
            root.getBottom().setVisible(visible);
            root.getBottom().setManaged(visible);
        }
    }

    private void seekBy(double seconds) {
        if (mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED) {
            return;
        }
        Duration current = mediaPlayer.getCurrentTime();
        Duration total = mediaPlayer.getTotalDuration();
        if (current == null || total == null || total.isUnknown()) {
            return;
        }
        double target = Math.max(0, Math.min(total.toSeconds(), current.toSeconds() + seconds));
        mediaPlayer.seek(Duration.seconds(target));
        timelineSlider.setValue(target);
        showPlaybackHud((seconds < 0 ? "-" : "+") + (int) Math.abs(seconds) + "s");
    }

    private void showPlaybackHud(String message) {
        if (playbackHud.getScene() == null) {
            return;
        }
        playbackHud.setText(message);
        playbackHud.setOpacity(1.0);
        playbackHud.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.6));
        FadeTransition fade = new FadeTransition(Duration.seconds(0.45), playbackHud);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(event -> playbackHud.setVisible(false));
        delay.setOnFinished(event -> fade.play());
        delay.play();
    }

    private void updateArtwork(Media media, File file) {
        if (isVideoFile(file)) {
            musicDisc.setVisible(false);
            musicDiscRotation.stop();
            return;
        }

        Object imageMetadata = media.getMetadata().get("image");
        if (imageMetadata instanceof Image image && !image.isError()) {
            artworkView.setImage(image);
            artworkView.setVisible(true);
            defaultArtwork.setVisible(false);
        } else {
            artworkView.setImage(null);
            artworkView.setVisible(false);
            defaultArtwork.setVisible(true);
        }
        musicDisc.setVisible(true);
    }

    private String formatMediaMetadata(Media media, File file) {
        Object artist = media.getMetadata().get("artist");
        Object album = media.getMetadata().get("album");
        Object title = media.getMetadata().get("title");
        String displayTitle = title instanceof String && !((String) title).isBlank()
                ? (String) title : file.getName();
        String artistText = artist instanceof String && !((String) artist).isBlank()
                ? (String) artist : "Local media";
        String albumText = album instanceof String && !((String) album).isBlank()
                ? "  •  " + album : "  •  " + mediaLibraryService.detectAlbumName(file);
        return displayTitle + "  •  " + artistText + albumText;
    }

    private boolean isVideoFile(File file) {
        return mediaLibraryService.isVideoMedia(file);
    }

    private String playbackErrorMessage(File file) {
        return "This file's codec is not supported: " + file.getName();
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }

        long seconds = Math.max(0, (long) duration.toSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        }
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}