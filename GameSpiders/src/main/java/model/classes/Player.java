package model.classes;

import core.Coordinates;
import core.PlayField;
import core.TCell;
import javafx.scene.control.TableView;
import model.enums.Character;
import model.enums.MovingDirection;
import model.interfaces.PlayerActions;
import javax.swing.*;
import static core.Coordinates.coordinatesAreValid;
import static utils.UtilGeneral.cellIsEmpty;
import static utils.UtilGeneral.sleep;
import static utils.UtilGeneral.cellIsExit;
import static core.Coordinates.fillCoordinatesOfNextCell;

public class Player implements PlayerActions {
    private Coordinates coordinates;
    private MovingDirection movingDirection = MovingDirection.RIGHT;
    private boolean isKilled = false;
    private boolean isWon = false;
    private final PlayField playField;
    private final TableView<TCell[]> table;

    public Player(Coordinates coordinates, PlayField playField, TableView<TCell[]> table) {
        this.coordinates = coordinates;
        this.playField = playField;
        this.table = table;

        setPlayerInPlayField();
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public MovingDirection getMovingDirection() {
        return movingDirection;
    }

    public void setIsKilled() {
        isKilled = true;
        JOptionPane.showMessageDialog(null, "Вы проиграли!", "Уведомление", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean isKilled() {
        return isKilled;
    }

    public boolean isWon() {
        return isWon;
    }

    public void movePlayer(MovingDirection newMovingDirection) {
        if (newMovingDirection != movingDirection) {
            movingDirection = newMovingDirection;
            playField.setMovingDirection(movingDirection, coordinates.get(1), coordinates.get(2));
            table.refresh();
            return;
        }

        Coordinates newCoordinates = new Coordinates();
        fillCoordinatesOfNextCell(newCoordinates, coordinates, movingDirection);

        boolean success = move(coordinates, newCoordinates);
        if (success) {
            setCoordinates(newCoordinates);
        }
    }

    private boolean move(Coordinates oldCoordinates, Coordinates newCoordinates) {
        if (!coordinatesAreValid(newCoordinates, playField.getHeight(), playField.getWidth())) {
            return false;
        }

        if (!cellIsEmpty(newCoordinates, playField) && !cellIsExit(newCoordinates, playField)) {
            return false;
        }

        synchronized (playField.getMonitor()) {
            if (cellIsExit(newCoordinates, playField)) {
                setIsWon();
            } else {
                playField.setCharacter(Character.PLAYER, newCoordinates.get(1), newCoordinates.get(2));
                playField.setMovingDirection(movingDirection, newCoordinates.get(1), newCoordinates.get(2));
                if (oldCoordinates != null) {
                    playField.setCharacter(Character.EMPTY, oldCoordinates.get(1), oldCoordinates.get(2));
                }
            }
        }

        table.refresh();
        sleep(Character.PLAYER);

        return true;
    }

    private void setPlayerInPlayField() {
        playField.setCharacter(Character.PLAYER, coordinates.get(1), coordinates.get(2));
        playField.setMovingDirection(movingDirection, coordinates.get(1), coordinates.get(2));
    }

    private void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    private void setIsWon() {
        isWon = true;
        JOptionPane.showMessageDialog(null, "Вы победили!", "Поздравление", JOptionPane.INFORMATION_MESSAGE);
    }
}
