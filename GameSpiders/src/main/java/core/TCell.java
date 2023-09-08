package core;

import model.enums.Character;
import model.enums.MovingDirection;

public class TCell {
    private Character character;
    private MovingDirection movingDirection;
    private boolean characterIsKilled;

    public TCell(Character character) {
        this.character = character;
    }

    public Character getCharacter() {
        return character;
    }

    public MovingDirection getMovingDirection() {
        return movingDirection;
    }

    public boolean characterIsKilled() {
        return characterIsKilled;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public void setMovingDirection(MovingDirection movingDirection) {
        this.movingDirection = movingDirection;
    }

    public void setCharacterIsKilled() {
        characterIsKilled = true;
    }
}
