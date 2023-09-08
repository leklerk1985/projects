package model.enums;

public enum Character {
    EMPTY, WALL, SPIDER, PLAYER, PATRON, EXIT;

    public boolean isEmptyOrPlayer() {
        return (this == EMPTY || this == PLAYER);
    }
}
