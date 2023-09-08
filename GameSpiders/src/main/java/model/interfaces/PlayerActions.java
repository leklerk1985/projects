package model.interfaces;

import core.Coordinates;
import model.enums.MovingDirection;

public interface PlayerActions {
    void movePlayer(MovingDirection newMovingDirection);
    Coordinates getCoordinates();
    MovingDirection getMovingDirection();
    boolean isKilled();
    boolean isWon();
    void setIsKilled();
}
