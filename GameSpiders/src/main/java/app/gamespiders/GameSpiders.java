package app.gamespiders;

import core.Coordinates;
import core.PlayField;
import core.TCell;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.classes.Patron;
import model.enums.MovingDirection;
import model.classes.Player;
import model.enums.RoutePassing;
import model.classes.Spider;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.yaml.snakeyaml.Yaml;
import static utils.UtilCellFactory.Cell;

public class GameSpiders extends Application {
    private final PlayField playField;
    private final TableView<TCell[]> table;
    private final Player player;
    private final Map<String, Image> images;
    private final ExecutorService charactersThreadPool;
    private final Map<String, Map<String, Object>> spidersInfo;
    private static final String YAML_FOLDER = "src/main/resources/yml";

    {
        var yamlProperties = getYamlProperties();

        var playFieldProperties = (Map<String, Integer>) yamlProperties.get("play-field");
        int numberOfSpiders = (int) yamlProperties.get("number-of-spiders");
        var playerCoordinates = (List<Integer>) yamlProperties.get("player-coordinates");
        var wallsCoordinates = (List<List<Integer>>) yamlProperties.get("walls-coordinates");
        var exitCoordinates = (List<Integer>) yamlProperties.get("exit-coordinates");

        int height = playFieldProperties.get("height");
        int width = playFieldProperties.get("width");
        int maxFlyingPatrons = height + width - 2; // max по вертикали (height - 1) + max по горизонтали (width - 1)

        spidersInfo = (Map<String, Map<String, Object>>) yamlProperties.get("spiders");
        playField = new PlayField(height, width, wallsCoordinates, exitCoordinates);
        table = new TableView<>();
        player = new Player(new Coordinates(playerCoordinates), playField, table);
        images = new HashMap<>();
        charactersThreadPool = Executors.newFixedThreadPool(maxFlyingPatrons + numberOfSpiders);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        hideHeaders();
        initializeImages();
        initializeColumns();
        initializeRows();
        launchSpiders();
        configureControlsAndShowStage(stage);
    }

    private void configureControlsAndShowStage(Stage stage) {
        Scene scene = new Scene(new Group());
        scene.getStylesheets().add("css/style.css");

        table.setEditable(true);
        table.setFocusTraversable(false);
        ((Group) scene.getRoot()).getChildren().addAll(table);

        scene.setOnKeyPressed(this::onKeyPressed);


        stage.setScene(scene);
        stage.show();
    }

    private void hideHeaders() {
        table.skinProperty().addListener((a, b, newSkin) ->
        {
            Pane header = (Pane) table.lookup("TableHeaderRow");
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setMaxHeight(0);
            header.setVisible(false);
        });
    }

    private void initializeImages() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try {
            try (var streamSpider = classloader.getResourceAsStream("images/spider.jpg");
                 var streamPlayer = classloader.getResourceAsStream("images/player.png");
                 var streamPlayerLeft = classloader.getResourceAsStream("images/playerLeft.png");
                 var streamPlayerUp = classloader.getResourceAsStream("images/playerUp.png");
                 var streamPlayerDown = classloader.getResourceAsStream("images/playerDown.png");
                 var streamPatron = classloader.getResourceAsStream("images/patron.png");
                 var streamPatronLeft = classloader.getResourceAsStream("images/patronLeft.png");
                 var streamPatronUp = classloader.getResourceAsStream("images/patronUp.png");
                 var streamPatronDown = classloader.getResourceAsStream("images/patronDown.png");) {

                if (streamSpider != null && streamPlayer != null && streamPlayerLeft != null && streamPlayerUp != null && streamPlayerDown != null
                        && streamPatron != null && streamPatronLeft != null && streamPatronUp != null && streamPatronDown != null) {

                    images.put("imageSpider", new Image(streamSpider));
                    images.put("imagePlayer", new Image(streamPlayer));
                    images.put("imagePlayerLeft", new Image(streamPlayerLeft));
                    images.put("imagePlayerUp", new Image(streamPlayerUp));
                    images.put("imagePlayerDown", new Image(streamPlayerDown));
                    images.put("imagePatron", new Image(streamPatron));
                    images.put("imagePatronLeft", new Image(streamPatronLeft));
                    images.put("imagePatronUp", new Image(streamPatronUp));
                    images.put("imagePatronDown", new Image(streamPatronDown));
                } else {
                    System.out.println("Не удалось получить изображения!");
                }
            }
        } catch (Exception e) {
            System.out.println("Не удалось получить изображения! " + e.getMessage());
        }
    }

    private void initializeColumns() {
        TableColumn<TCell[], TCell> column;

        for (int i = 0; i < 10; i++) {
            column = new TableColumn<>();
            column.setMinWidth(100);
            column.setCellValueFactory(getCallbackLambda(i));
            column.setCellFactory(col -> new Cell(images));
            table.getColumns().add(column);
        }
    }

    private Callback<TableColumn.CellDataFeatures<TCell[], TCell>, ObservableValue<TCell>> getCallbackLambda(int i) {
        return p -> {
            TCell[] x = p.getValue();
            if (x != null && x.length > 0) {
                return new SimpleObjectProperty<>(x[i]);
            } else {
                return new SimpleObjectProperty<>(null);
            }
        };
    }

    private void initializeRows() {
        var data= FXCollections.observableArrayList(
                playField.getSubarray(0),
                playField.getSubarray(1),
                playField.getSubarray(2),
                playField.getSubarray(3),
                playField.getSubarray(4),
                playField.getSubarray(5),
                playField.getSubarray(6),
                playField.getSubarray(7),
                playField.getSubarray(8),
                playField.getSubarray(9));
        table.setItems(data);
    }

    private void launchSpiders() {
        List<Coordinates> routeList = new ArrayList<>();
        List<Coordinates> boundaryList = new ArrayList<>();
        Coordinates[] route;
        Coordinates[] boundary;

        for (var spiderInfo : spidersInfo.entrySet()) {
            var spiderMap = spiderInfo.getValue();

            for (var element : (List<List<Integer>>) spiderMap.get("route")) {
                routeList.add(new Coordinates(element.get(0), element.get(1)));
            }
            route = routeList.toArray(new Coordinates[0]);
            routeList.clear();

            for (var element : (List<List<Integer>>) spiderMap.get("boundary")) {
                boundaryList.add(new Coordinates(element.get(0), element.get(1)));
            }
            boundary = boundaryList.toArray(new Coordinates[0]);
            boundaryList.clear();

            var routePassing = RoutePassing.valueOf((String) spiderMap.get("route-passing"));

            launchSpider(route, boundary, routePassing);
        }
    }

    private void launchSpider(Coordinates[] routeCoordinates, Coordinates[] boundaryCoordinates, RoutePassing routePassing) {
        new Spider(routeCoordinates, boundaryCoordinates, routePassing, player, charactersThreadPool, playField, table).launchSpider();
    }

    private void onKeyPressed(KeyEvent event) {
        KeyCode keyCodeInit = event.getCode();

        if (keyCodeInit == KeyCode.ENTER) {
            new Patron(player.getCoordinates(), player.getMovingDirection(), charactersThreadPool, playField, table).launchPatron();
        } else if (keyCodeInit == KeyCode.RIGHT || keyCodeInit == KeyCode.LEFT || keyCodeInit == KeyCode.UP || keyCodeInit == KeyCode.DOWN) {
            String keyCode = keyCodeInit.toString().toUpperCase();
            player.movePlayer(MovingDirection.valueOf(keyCode));
        }
    }

    private Map<String, Object> getYamlProperties() {
        var yamlProperties = new HashMap<String, Object>();
        var classLoader = getClass().getClassLoader();
        InputStream inputStream;
        Yaml yaml = new Yaml();

        var yamlFiles = getFilePathsForYamlFolder();
        for (String file : yamlFiles) {
            inputStream = classLoader.getResourceAsStream(file);
            yamlProperties.putAll(yaml.load(inputStream));
        }

        return yamlProperties;
    }

    private List<String> getFilePathsForYamlFolder() {
        var yamlFolder = new File(YAML_FOLDER);
        var yamlFiles = yamlFolder.listFiles();
        if (yamlFiles == null || yamlFiles.length == 0) {
            return new ArrayList<>();
        }

        List<String> yamlPaths = new ArrayList<>();
        for (File file : yamlFiles) {
            yamlPaths.add(file.getParentFile().getName() + "/" + file.getName());
        }

        return yamlPaths;
    }

}