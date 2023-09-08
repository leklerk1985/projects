package model.classes;

import core.Coordinates;
import core.PlayField;
import core.TCell;
import javafx.scene.control.TableView;
import model.enums.Character;
import model.enums.MovingDirection;
import model.interfaces.PatronActions;
import java.util.concurrent.ExecutorService;
import static core.Coordinates.*;
import static utils.UtilGeneral.sleep;
import static utils.UtilGeneral.cellIsEmpty;

public class Patron implements PatronActions {
    private final Coordinates playerCoordinates;
    private final Coordinates initialCoordinates = new Coordinates();
    private final MovingDirection movingDirection;
    private boolean endWasReached = false;
    private final ExecutorService charactersThreadPool;
    private final PlayField playField;
    private final TableView<TCell[]> table;

    public Patron(Coordinates playerCoordinates, MovingDirection movingDirection, ExecutorService charactersThreadPool, PlayField playField, TableView<TCell[]> table) {
        this.playerCoordinates = playerCoordinates;
        this.movingDirection = movingDirection;
        this.charactersThreadPool = charactersThreadPool;
        this.playField = playField;
        this.table = table;

        setInitialCoordinatesByPlayerCoordinates();
    }

    public void launchPatron() {
        charactersThreadPool.submit(this::patronIsFlying);
    }

    private void setEndWasReached() {
        endWasReached = true;
    }

    private void setInitialCoordinatesByPlayerCoordinates() {
        fillCoordinatesOfNextCell(initialCoordinates, playerCoordinates, movingDirection);
    }

    private void patronIsFlying() {
        Coordinates prevCoordinates = null;
        Coordinates currCoordinates = initialCoordinates;

        while (!endWasReached) {
            move(prevCoordinates, currCoordinates);

            prevCoordinates = currCoordinates;
            currCoordinates = calculateCoordinatesOfNextCell(currCoordinates, movingDirection);
        }
    }

    private void move(Coordinates oldCoordinates, Coordinates newCoordinates) {
        synchronized (playField.getMonitor()) {
            if (!coordinatesAreValid(newCoordinates, playField.getHeight(), playField.getWidth())) {
                if (oldCoordinates != null) {
                    playField.setCharacter(Character.EMPTY, oldCoordinates.get(1), oldCoordinates.get(2));
                }

                setEndWasReached();
            } else if (cellIsEmpty(newCoordinates, playField)) {
                playField.setCharacter(Character.PATRON, newCoordinates.get(1), newCoordinates.get(2));
                playField.setMovingDirection(movingDirection, newCoordinates.get(1), newCoordinates.get(2));
                if (oldCoordinates != null) {
                    playField.setCharacter(Character.EMPTY, oldCoordinates.get(1), oldCoordinates.get(2));
                }
            } else {
                if (playField.getCharacter(newCoordinates.get(1), newCoordinates.get(2)) == Character.WALL) {
                    playField.setCharacter(Character.EMPTY, newCoordinates.get(1), newCoordinates.get(2));
                }
                if (oldCoordinates != null) {
                    playField.setCharacter(Character.EMPTY, oldCoordinates.get(1), oldCoordinates.get(2));
                }

                setEndWasReached();
            }
        }

        table.refresh();
        sleep(Character.PATRON);
    }

}
