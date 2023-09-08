package core;

import model.enums.Character;
import model.enums.MovingDirection;
import java.util.List;

public class PlayField {
    private final int height;
    private final int width;
    private final TCell[][] field;
    private final Object monitor = new Object();

    public PlayField(int heightValue, int widthValue, List<List<Integer>> wallsCoordinates, List<Integer> exitCoordinates) {
        height = heightValue;
        width = widthValue;

        field = new TCell[height][width];
        initializeField(wallsCoordinates, exitCoordinates);
    }

    public Object getMonitor() {
        synchronized (monitor) {
            return monitor;
        }
    }

    public int getHeight() {
        synchronized (monitor) {
            return height;
        }
    }

    public int getWidth() {
        synchronized (monitor) {
            return width;
        }
    }

    public Character getCharacter(int firstIndex, int secondIndex) {
        synchronized (monitor) {
            return field[firstIndex][secondIndex].getCharacter();
        }
    }

    public void setCharacter(Character character, int firstIndex, int secondIndex) {
        synchronized (monitor) {
            field[firstIndex][secondIndex].setCharacter(character);
            if (character == Character.EMPTY || character == Character.WALL) {
                setMovingDirection(null, firstIndex, secondIndex);
            }
        }
    }

    public void setMovingDirection(MovingDirection movingDirection, int firstIndex, int secondIndex) {
        synchronized (monitor) {
            field[firstIndex][secondIndex].setMovingDirection(movingDirection);
        }
    }

    public void setCharacterKilled(int firstIndex, int secondIndex) {
        synchronized (monitor) {
            field[firstIndex][secondIndex].setCharacterIsKilled();
        }
    }

    public TCell[] getSubarray(int index) {
        return field[index];
    }

    private void initializeField(List<List<Integer>> wallsCoordinates, List<Integer> exitCoordinates) {
        initializeWallCells(wallsCoordinates);
        initializeExitCell(exitCoordinates);
        initializeEmptyCells();
    }

    private void initializeWallCells(List<List<Integer>> wallsCoordinates) {
        for (var wallCoord : wallsCoordinates) {
            field[wallCoord.get(0)][wallCoord.get(1)] = new TCell(Character.WALL);
        }
    }

    private void initializeExitCell(List<Integer> exitCoordinates) {
        field[exitCoordinates.get(0)][exitCoordinates.get(1)] = new TCell(Character.EXIT);
    }

    private void initializeEmptyCells() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (field[i][j] == null) {
                    field[i][j] = new TCell(Character.EMPTY);
                }
            }
        }
    }
}
