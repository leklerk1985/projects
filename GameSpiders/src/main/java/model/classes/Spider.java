package model.classes;

import core.Coordinates;
import core.PlayField;
import core.DirectionsManager;
import core.TCell;
import javafx.scene.control.TableView;
import model.enums.Character;
import model.enums.MovingDirection;
import model.enums.RoutePassing;
import model.interfaces.SpiderActions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import static core.Coordinates.*;
import static utils.UtilGeneral.sleep;
import static utils.UtilGeneral.cellIsEmpty;
import static utils.UtilGeneral.cellIsEmptyOrPlayer;

public class Spider implements SpiderActions {
    private final Coordinates[] routeCoordinates;
    private final Coordinates[] boundaryCoordinates;
    private final RoutePassing routePassing;
    private final Player player;
    private final ExecutorService charactersThreadPool;
    private final PlayField playField;
    private final TableView<TCell[]> table;

    public Spider(Coordinates[] routeCoordinates, Coordinates[] boundaryCoordinates, RoutePassing routePassing, Player player, ExecutorService charactersThreadPool,
                  PlayField playField, TableView<TCell[]> table) {
        this.routeCoordinates = routeCoordinates;
        this.boundaryCoordinates = boundaryCoordinates;
        this.routePassing = routePassing;
        this.player = player;
        this.charactersThreadPool = charactersThreadPool;
        this.playField = playField;
        this.table = table;
    }

    public void launchSpider() {
        charactersThreadPool.submit(this::spiderIsRunning);
    }

    private void spiderIsRunning() {
        Coordinates lastCoordinates = new Coordinates();

        spiderIsRunningAlongBoundary(lastCoordinates);
        spiderIsHuntingPlayer(lastCoordinates);
    }

    private boolean boundaryIsComplete() {
        boolean isComplete = true;

        for (Coordinates cellCoordinates: boundaryCoordinates) {
            if (playField.getCharacter(cellCoordinates.get(1), cellCoordinates.get(2)) == Character.EMPTY) {
                isComplete = false;
                break;
            }
        }

        return isComplete;
    }

    private void move(Coordinates oldCoordinates, Coordinates newCoordinates) {
        if (!coordinatesAreValid(newCoordinates, playField.getHeight(), playField.getWidth()) || !cellIsEmptyOrPlayer(newCoordinates, playField)) {
            return;
        }

        synchronized (playField.getMonitor()) {
            if (cellIsEmpty(newCoordinates, playField)) {
                playField.setCharacter(Character.SPIDER, newCoordinates.get(1), newCoordinates.get(2));
                playField.setMovingDirection(null, newCoordinates.get(1), newCoordinates.get(2));
                if (oldCoordinates != null) {
                    playField.setCharacter(Character.EMPTY, oldCoordinates.get(1), oldCoordinates.get(2));
                }
            } else {
                playField.setCharacterKilled(newCoordinates.get(1), newCoordinates.get(2));
                player.setIsKilled();
            }
        }

        table.refresh();
        sleep(Character.SPIDER);
    }

    private void spiderIsRunningAlongBoundary(Coordinates lastCoordinates) {
        Coordinates prevCoordinates = null;
        Coordinates currCoordinates = null;
        int currIndex = 0;
        int routeLength = routeCoordinates.length;
        boolean forwardDirection = true;

        while (boundaryIsComplete() && !player.isKilled() && !player.isWon()) {
            currCoordinates = routeCoordinates[currIndex];
            move(prevCoordinates, currCoordinates);

            if (forwardDirection) {
                if (currIndex < routeLength - 1) {
                    currIndex++;
                } else {
                    if (routePassing == RoutePassing.TO_THE_END_AND_BACK) {
                        currIndex--;
                        forwardDirection = false;
                    } else {
                        currIndex = 0;
                    }
                }
            } else {
                if (currIndex > 0) {
                    currIndex--;
                } else {
                    currIndex++;
                    forwardDirection = true;
                }
            }

            prevCoordinates = currCoordinates;
        }
        lastCoordinates.setCoordinate(currCoordinates != null ? currCoordinates.get(1) : routeCoordinates[0].get(1), 1);
        lastCoordinates.setCoordinate(currCoordinates != null ? currCoordinates.get(2) : routeCoordinates[0].get(2), 2);
    }

    private void spiderIsHuntingPlayer(Coordinates initialCoordinates) {
        Coordinates prevCoordinates = initialCoordinates;
        Coordinates currCoordinates;

        while (!player.isKilled() && !player.isWon()) {
            currCoordinates = calculateNewCoordinates(prevCoordinates);
            move(prevCoordinates, currCoordinates);

            prevCoordinates = currCoordinates;
        }
    }

    private Coordinates calculateNewCoordinates(Coordinates spiderCoordinates) {
        List<Coordinates> route = new ArrayList<>();
        buildRoute(route, spiderCoordinates, player.getCoordinates());
        return route.get(1);
    }

    private void buildRoute(List<Coordinates> route, Coordinates spiderCoordinates, Coordinates playerCoordinates) {
        var directionsManager = new DirectionsManager(spiderCoordinates, playerCoordinates);
        continueRoute(spiderCoordinates, playerCoordinates, spiderCoordinates, route, directionsManager);
    }

    private void continueRoute(Coordinates startCoordinates, Coordinates finishCoordinates, Coordinates currentSpiderCoordinates,
                               List<Coordinates> route, DirectionsManager directionsManager) {
        MovingDirection direction;
        boolean directionFound;
        Coordinates coordinates;

        if (startCoordinates.equals(finishCoordinates)) {
            return;
        }

        if (directionsManager.getActualDirection() != null) {
            if (directionsManager.getActualDirectionDiff() <= 0) {
                var lastRouteCoordinates = route.get(route.size()-1);
                var nextActualDirectionCoordinates = calculateCoordinatesOfNextCell(lastRouteCoordinates, directionsManager.getActualDirection());
                boolean nextActualDirectionCoordinatesArePermitted = coordinatesArePermitted(nextActualDirectionCoordinates, currentSpiderCoordinates, playField);

                direction = directionsManager.getNextInPriorityDirection(route, finishCoordinates, nextActualDirectionCoordinatesArePermitted);
            } else if (directionsManager.actualDirectionIsContra()) {
                direction = directionsManager.getNextInPriorityDirection(route, finishCoordinates, null);
            } else {
                direction = directionsManager.getActualDirection();
            }

            directionFound = false;
            do {
                coordinates = calculateCoordinatesOfNextCell(startCoordinates, direction);
                if (coordinatesArePermitted(coordinates, currentSpiderCoordinates, playField)) {
                    if (coordinates.equals(currentSpiderCoordinates)) {
                        route.clear();
                    }

                    route.add(coordinates);

                    if (direction != directionsManager.getActualDirection()) {
                        directionsManager.setActualDirection(direction);
                    }

                    directionsManager.changeActualDirectionDiff();
                    directionsManager.clearFailedDirections();
                    directionFound = true;
                } else {
                    directionsManager.setTestedDirection(direction);
                    directionsManager.addFailedDirection(direction);
                    direction = directionsManager.getNextInPriorityDirection(route, finishCoordinates, null);
                }
            } while (!directionFound);

            continueRoute(coordinates, finishCoordinates, currentSpiderCoordinates, route, directionsManager);
        } else {
            route.add(startCoordinates);

            Coordinates hCoordinates = calculateCoordinatesOfNextCell(startCoordinates, directionsManager.getHorizontalDirection());
            Coordinates vCoordinates = calculateCoordinatesOfNextCell(startCoordinates, directionsManager.getVerticalDirection());
            Coordinates vcCoordinates = calculateCoordinatesOfNextCell(startCoordinates, directionsManager.getVerticalContraDirection());
            Coordinates hcCoordinates = calculateCoordinatesOfNextCell(startCoordinates, directionsManager.getHorizontalContraDirection());
            Coordinates aCoordinates = new Coordinates();

            direction = directionsManager.chooseFirstDirection(hCoordinates, vCoordinates, vcCoordinates, hcCoordinates, aCoordinates, finishCoordinates, playField);

            route.add(aCoordinates);
            directionsManager.setActualDirection(direction);
            directionsManager.changeActualDirectionDiff();
            continueRoute(aCoordinates, finishCoordinates, currentSpiderCoordinates, route, directionsManager);
        }
    }
}
