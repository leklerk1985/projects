package core;

import model.enums.MovingDirection;
import java.util.List;
import java.util.Objects;

public class Coordinates {
    private int firstCoordinate;
    private int secondCoordinate;

    public Coordinates(int firstCoordinate, int secondCoordinate) {
        this.firstCoordinate = firstCoordinate;
        this.secondCoordinate = secondCoordinate;
    }

    public Coordinates() {
    }

    public Coordinates(List<Integer> coordinatesList) {
        firstCoordinate = coordinatesList.get(0);
        secondCoordinate = coordinatesList.get(1);
    }

    public void copyCoordinates(Coordinates coordinates) {
        setCoordinate(coordinates.get(1), 1);
        setCoordinate(coordinates.get(2), 2);
    }

    public void setCoordinate(int value, int coordinateNumber) {
        if (coordinateNumber == 1) {
            firstCoordinate = value;
        } else {
            secondCoordinate = value;
        }
    }

    public void setCoordinates(int firstValue, int secondValue) {
        firstCoordinate = firstValue;
        secondCoordinate = secondValue;
    }

    public int get(int coordinateNumber) {
        return coordinateNumber == 1 ? firstCoordinate : secondCoordinate;
    }

    public static boolean coordinatesAreValid(Coordinates coordinates, int heightPlayField, int widthPlayField) {
        if (coordinates == null) {
            return false;
        }

        return coordinates.get(1) >= 0 && coordinates.get(1) < heightPlayField && coordinates.get(2) >= 0 && coordinates.get(2) < widthPlayField;
    }

    public static boolean coordinatesArePermitted(Coordinates coordinates, Coordinates currentSpiderCoordinates, PlayField playField) {
        if (!coordinatesAreValid(coordinates, playField.getHeight(), playField.getWidth())) {
            return false;
        }

        boolean isEmptyOrPlayer = playField.getCharacter(coordinates.get(1), coordinates.get(2)).isEmptyOrPlayer();
        boolean isThisSpider = coordinates.equals(currentSpiderCoordinates);

        return isEmptyOrPlayer || isThisSpider;
    }

    public static void fillCoordinatesOfNextCell(Coordinates newCoordinates, Coordinates oldCoordinates, MovingDirection movingDirection) {
        if (movingDirection == null) {
            newCoordinates.copyCoordinates(oldCoordinates);
            return;
        }

        switch (movingDirection) {
            case RIGHT -> {
                newCoordinates.setCoordinates(oldCoordinates.get(1), oldCoordinates.get(2) + 1);
            }
            case LEFT -> {
                newCoordinates.setCoordinates(oldCoordinates.get(1), oldCoordinates.get(2) - 1);
            }
            case UP -> {
                newCoordinates.setCoordinates(oldCoordinates.get(1) - 1, oldCoordinates.get(2));
            }
            case DOWN -> {
                newCoordinates.setCoordinates(oldCoordinates.get(1) + 1, oldCoordinates.get(2));
            }
        }
    }

    public static Coordinates calculateCoordinatesOfNextCell(Coordinates oldCoordinates, MovingDirection movingDirection) {
        if (movingDirection == null) {
            return null;
        }

        Coordinates newCoordinates = new Coordinates();
        fillCoordinatesOfNextCell(newCoordinates, oldCoordinates, movingDirection);
        return newCoordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return firstCoordinate == that.firstCoordinate && secondCoordinate == that.secondCoordinate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstCoordinate, secondCoordinate);
    }
}
