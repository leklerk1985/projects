package utils;

import core.Coordinates;
import core.PlayField;
import model.enums.Character;

public class UtilGeneral {
    public static boolean cellIsEmpty(Coordinates coordinates, PlayField playField) {
        return playField.getCharacter(coordinates.get(1), coordinates.get(2)) == Character.EMPTY;
    }

    public static boolean cellIsEmptyOrPlayer(Coordinates coordinates, PlayField playField) {
        return playField.getCharacter(coordinates.get(1), coordinates.get(2)).isEmptyOrPlayer();
    }

    public static boolean cellIsExit(Coordinates coordinates, PlayField playField) {
        return playField.getCharacter(coordinates.get(1), coordinates.get(2)) == Character.EXIT;
    }

    public static void sleep(Character character) {
        try {
            Thread.sleep(getMillisForCharacter(character));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static long getMillisForCharacter(Character character) {
        if (character == Character.PLAYER) {
            return 200L;
        } else if (character == Character.PATRON) {
            return 100L;
        } else { // если character == Character.Spider
            return 750L;
        }
    }

}




