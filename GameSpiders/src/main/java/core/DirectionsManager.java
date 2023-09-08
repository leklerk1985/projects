package core;

import model.enums.MovingDirection;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import static java.lang.Math.hypot;
import static core.Coordinates.calculateCoordinatesOfNextCell;
import static core.Coordinates.coordinatesAreValid;

public class DirectionsManager {
    private MovingDirection horizontalDir;
    private MovingDirection verticalDir;
    private MovingDirection verticalDirContra;
    private MovingDirection horizontalDirContra;
    private MovingDirection actualDir;
    private MovingDirection testedDir;
    private final List<MovingDirection> failedDirections = new ArrayList<>();
    private boolean horizontalDirectionIsDefault = false;
    private int horizontalDiff;
    private int verticalDiff;
    private static final MovingDirection DEFAULT_HORIZONTAL_DIRECTION = MovingDirection.LEFT;
    private static final MovingDirection DEFAULT_VERTICAL_DIRECTION = MovingDirection.UP;

    public DirectionsManager(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        setHorizontalDirections(spiderCoordinates, playerCoordinates);
        setVerticalDirections(spiderCoordinates, playerCoordinates);
        setDiffs(spiderCoordinates, playerCoordinates);
    }

    public void setHorizontalDirections(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        horizontalDir = getHorizontalDirection(spiderCoordinates, playerCoordinates);
        if (horizontalDir == null) {
            horizontalDir = DEFAULT_HORIZONTAL_DIRECTION;
            horizontalDirectionIsDefault = true;
        }

        horizontalDirContra = getContraDirection(horizontalDir);
    }

    public void setVerticalDirections(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        verticalDir = getVerticalDirection(spiderCoordinates, playerCoordinates);
        if (verticalDir == null) {
            verticalDir = DEFAULT_VERTICAL_DIRECTION;
        }

        verticalDirContra = getContraDirection(verticalDir);
    }

    public void setActualDirection(MovingDirection actDir) {
        actualDir = actDir;
    }

    public void setTestedDirection(MovingDirection direction) {
        testedDir = direction;
    }

    public void addFailedDirection(MovingDirection direction) {
        failedDirections.add(direction);
    }

    public void clearFailedDirections() {
        failedDirections.clear();
    }

    public MovingDirection getActualDirection() {
        return actualDir;
    }

    public MovingDirection getHorizontalDirection() {
        return horizontalDir;
    }

    public MovingDirection getVerticalDirection() {
        return verticalDir;
    }

    public MovingDirection getHorizontalContraDirection() {
        return horizontalDirContra;
    }

    public MovingDirection getVerticalContraDirection() {
        return verticalDirContra;
    }

    public MovingDirection getNextInPriorityDirection(List<Coordinates> route, Coordinates playerCoordinates, Boolean nextActualDirectionCoordinatesArePermitted) {

        MovingDirection nextDirection;

        // Два раза подряд пришлось менять направление (т.е. между первой сменой и второй не прошли ни одной ячейки),
        // причём оба эти направления - главные.

        boolean twoFailsInARowWithMainDirections = (actualDir == horizontalDir && testedDir == verticalDir) || (actualDir == verticalDir && testedDir == horizontalDir);
        boolean failWithMainHorizontalDirection = (testedDir == horizontalDir);
        boolean failWithMainVerticalDirection = (testedDir == verticalDir);

        if (twoFailsInARowWithMainDirections) {
            if (horizontalDiff == 0) {
                if (!failedDirections.contains(horizontalDir)) {
                    nextDirection = horizontalDir;
                } else {
                    nextDirection = horizontalDirContra;
                }
            } else if (verticalDiff == 0) {
                if (!failedDirections.contains(verticalDir)) {
                    nextDirection = verticalDir;
                } else {
                    nextDirection = verticalDirContra;
                }
            } else {
                Coordinates lastRouteCoordinates = route.get(route.size()-1);
                Coordinates hcCoordinates = calculateCoordinatesOfNextCell(lastRouteCoordinates, horizontalDirContra);
                Coordinates vcCoordinates = calculateCoordinatesOfNextCell(lastRouteCoordinates, verticalDirContra);

                double hcDistance = calculateDistance(playerCoordinates, hcCoordinates);
                double vcDistance = calculateDistance(playerCoordinates, vcCoordinates);
                nextDirection = (hcDistance < vcDistance ? horizontalDirContra : verticalDirContra);
            }
        } else if (actualDir == horizontalDir) {
            nextDirection = (verticalDiff > 0 ? verticalDir : verticalDirContra);
        } else if (actualDir == verticalDir) {
            nextDirection = (horizontalDiff > 0 ? horizontalDir : horizontalDirContra);
        } else if (actualDir == verticalDirContra) {
            if (failWithMainHorizontalDirection) {
                nextDirection = actualDir;
            } else {
                nextDirection = horizontalDir;
            }
        } else if (actualDir == horizontalDirContra) {
            if (failWithMainVerticalDirection) {
                nextDirection = actualDir;
            } else {
                nextDirection = verticalDir;
            }
        } else {
            nextDirection = horizontalDir;
        }

        clearTestedDirection();

        boolean nextCoordinatesArePermitted = (nextActualDirectionCoordinatesArePermitted != null ? nextActualDirectionCoordinatesArePermitted : true);
        if (!nextCoordinatesArePermitted && !failedDirections.contains(actualDir)) {
            failedDirections.add(actualDir);
        }

        return nextDirection;
    }

    public MovingDirection chooseFirstDirection(Coordinates hCoordinates, Coordinates vCoordinates, Coordinates vcCoordinates, Coordinates hcCoordinates,
                                                Coordinates aCoordinates, Coordinates playerCoordinates, PlayField playField) {
        if (!horizontalDirectionIsDefault
                && coordinatesAreValid(hCoordinates, playField.getHeight(), playField.getWidth())
                && playField.getCharacter(hCoordinates.get(1), hCoordinates.get(2)).isEmptyOrPlayer()) {

            aCoordinates.copyCoordinates(hCoordinates);
            return horizontalDir;
        } else if (coordinatesAreValid(vCoordinates, playField.getHeight(), playField.getWidth())
                && playField.getCharacter(vCoordinates.get(1), vCoordinates.get(2)).isEmptyOrPlayer()) {

            aCoordinates.copyCoordinates(vCoordinates);
            return verticalDir;
        } else {
            if (vcCoordinates != null && hcCoordinates != null) {
                double vcDistance = calculateDistance(playerCoordinates, vcCoordinates);
                double hcDistance = calculateDistance(playerCoordinates, hcCoordinates);
                aCoordinates.copyCoordinates(vcDistance < hcDistance ? vcCoordinates : hcCoordinates);

                return (vcDistance < hcDistance ? verticalDirContra : horizontalDirContra);
            } else if (vcCoordinates != null) {
                aCoordinates.copyCoordinates(vcCoordinates);
                return verticalDirContra;
            } else {
                aCoordinates.copyCoordinates(hcCoordinates);
                return horizontalDirContra;
            }
        }
    }

    public void changeActualDirectionDiff() {
        if (actualDir == horizontalDir) {
            horizontalDiff--;
        } else if (actualDir == verticalDir) {
            verticalDiff--;
        } else if (actualDir == verticalDirContra) {
            verticalDiff++;
        } else {
            horizontalDiff++;
        }
    }

    public int getActualDirectionDiff() {
        if (actualDir == horizontalDir) {
            return horizontalDiff;
        } else if (actualDir == verticalDir) {
            return verticalDiff;
        } else if (actualDir == verticalDirContra) {
            return -verticalDiff;
        } else {
            return -horizontalDiff;
        }
    }

    public boolean actualDirectionIsContra() {
        return actualDir == verticalDirContra || actualDir == horizontalDirContra;
    }

    public double calculateDistance(Coordinates firstCoordinates, Coordinates secondCoordinates) {
        int hDiff = abs(firstCoordinates.get(2) - secondCoordinates.get(2));
        int vDiff = abs(firstCoordinates.get(1) - secondCoordinates.get(1));
        return hypot(hDiff, vDiff);
    }

    private void setDiffs(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        horizontalDiff = abs(spiderCoordinates.get(2) - playerCoordinates.get(2));
        verticalDiff = abs(spiderCoordinates.get(1) - playerCoordinates.get(1));
    }

    private void clearTestedDirection() {
        testedDir = null;
    }

    private MovingDirection getHorizontalDirection(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        if (spiderCoordinates.get(2) < playerCoordinates.get(2)) {
            return MovingDirection.RIGHT;
        } else if (spiderCoordinates.get(2) > playerCoordinates.get(2)) {
            return MovingDirection.LEFT;
        } else {
            return null;
        }
    }

    private MovingDirection getVerticalDirection(Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        if (spiderCoordinates.get(1) < playerCoordinates.get(1)) {
            return MovingDirection.DOWN;
        } else if (spiderCoordinates.get(1) > playerCoordinates.get(1)) {
            return MovingDirection.UP;
        } else {
            return null;
        }
    }

    private MovingDirection getContraDirection(MovingDirection movingDirection) {
        if (movingDirection == null) {
            return null;
        }

        return switch (movingDirection) {
            case RIGHT -> MovingDirection.LEFT;
            case LEFT -> MovingDirection.RIGHT;
            case UP -> MovingDirection.DOWN;
            case DOWN -> MovingDirection.UP;
        };
    }
}
