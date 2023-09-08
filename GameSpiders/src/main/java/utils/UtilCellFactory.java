package utils;

import core.TCell;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import java.util.Map;

public class UtilCellFactory {

    public static class Cell extends TableCell<TCell[], TCell> {

        private final ImageView imageView = new ImageView();

        private final Map<String, Image> images;

        {
            imageView.setFitWidth(33.4);
            imageView.setFitHeight(33.4);
            setGraphic(imageView);
        }

        public Cell(Map<String, Image> images) {
            this.images = images;
        }

        @Override
        protected void updateItem(TCell item, boolean empty) {
            if (empty || item == null) {
                imageView.setImage(null);
            } else {
                switch (item.getCharacter()) {
                    case EMPTY -> imageView.setImage(null);
                    case WALL -> setStyle("-fx-background-color: burlywood;");
                    case SPIDER -> imageView.setImage(images.get("imageSpider"));
                    case PLAYER -> {
                        switch (item.getMovingDirection()) {
                            case RIGHT -> imageView.setImage(images.get("imagePlayer"));
                            case LEFT -> imageView.setImage(images.get("imagePlayerLeft"));
                            case UP -> imageView.setImage(images.get("imagePlayerUp"));
                            case DOWN -> imageView.setImage(images.get("imagePlayerDown"));
                        }

                        if (item.characterIsKilled()) {
                            setStyle("-fx-background-color: red;");
                        }
                    }
                    case PATRON -> {
                        switch (item.getMovingDirection()) {
                            case RIGHT -> imageView.setImage(images.get("imagePatron"));
                            case LEFT -> imageView.setImage(images.get("imagePatronLeft"));
                            case UP -> imageView.setImage(images.get("imagePatronUp"));
                            case DOWN -> imageView.setImage(images.get("imagePatronDown"));
                        }
                    }
                    case EXIT -> {
                        setStyle("-fx-background-color: lightskyblue;");
                        setText("EXIT");
                        setFont(new Font("Arial", 14));
                    }
                }
            }
        }
    }

}