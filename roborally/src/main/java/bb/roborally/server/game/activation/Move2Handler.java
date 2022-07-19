package bb.roborally.server.game.activation;


import bb.roborally.protocol.game_events.Movement;
import bb.roborally.protocol.game_events.Reboot;
import bb.roborally.server.Server;
import bb.roborally.server.game.*;
import java.io.IOException;


/**
 * @author Veronika Heckel
 */
public class Move2Handler {

    Server server;
    Game game;
    User user;

    public Move2Handler(Server server, Game game, User user) {
        this.server = server;
        this.game = game;
        this.user = user;
    }

    public void handleAlt() throws IOException, IndexOutOfBoundsException {
        Robot robot = user.getRobot();
        Position position = user.getRobot().getPosition();
        Orientation orientation = user.getRobot().getRobotOrientation();
        int x = position.getX();
        int y = position.getY();
        MovementCheck movementCheck = new MovementCheck(game.getBoard(), game);
        if (movementCheck.checkIfBlockedAlt(position, orientation, 0)) {
            server.broadcast(new Movement(user.getClientID(), x, y));
        } else {
            if (robot.getRobotOrientation() == Orientation.TOP) {
                Position currentField = new Position(position.getX(), position.getY() - 1);
                if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                    Orientation orientationFirst = Orientation.TOP;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) { //check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            //robot.setPosition(new Position(currentField.getX(), currentField.getY()));
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }

                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        // Move 2x
                        try {
                            robot.setPosition(new Position(x, y - 2));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x, y - 2));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }

                    }
                } else {
                    //only 1 step
                    Orientation orientationFirst = Orientation.TOP;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) {//check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }
                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {

                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            robot.setPosition(new Position(x, y - 1));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x, y - 1));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                }
            } else if (user.getRobot().getRobotOrientation() == Orientation.LEFT) {
                if (!movementCheck.checkIfBlockedAlt(position, orientation, 1)) {
                    Orientation orientationFirst = Orientation.LEFT;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) {//check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            //robot.setPosition(new Position(currentField.getX(), currentField.getY()));
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        // Move2x
                        try {
                            robot.setPosition(new Position(x - 2, y));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x - 2, y));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                } else {
                    // Move only 1
                    Orientation orientationFirst = Orientation.LEFT;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) {//check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            robot.setPosition(new Position(x - 1, y));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x - 1, y));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                }
            } else if (user.getRobot().getRobotOrientation() == Orientation.BOTTOM) {
                Position currentField = new Position(position.getX(), position.getY() + 1);
                if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                    Orientation orientationFirst = Orientation.BOTTOM;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) { //check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {

                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));

                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        // Move 2x
                        try {
                            robot.setPosition(new Position(x, y + 2));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x, y + 2));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                } else {
                    // Move only 1
                    Orientation orientationFirst = Orientation.BOTTOM;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) { //check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method


                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            //robot.setPosition(new Position(currentField.getX(), currentField.getY()));
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            robot.setPosition(new Position(x, y + 1));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x, y + 1));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                }
            } else if (user.getRobot().getRobotOrientation() == Orientation.RIGHT) {
                Position currentField = new Position(position.getX() + 1, position.getY());
                if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                    Orientation orientationFirst = Orientation.RIGHT;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) { //check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            //robot.setPosition(new Position(currentField.getX(), currentField.getY()));
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        // Move 2x
                        try {
                            robot.setPosition(new Position(x + 2, y));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x + 2, y));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                } else {
                    // Move only 1
                    Orientation orientationFirst = Orientation.RIGHT;
                    if (movementCheck.checkIfFirstTwoAreNeighbors(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(), orientationFirst, 1)) {//check if first two are neighbors and store the first one in same list - extra method because the first one will not be stored in first method

                        for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1);
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            //robot.setPosition(new Position(currentField.getX(), currentField.getY()));
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                }
                            }
                        } else {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        if (!(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i)))) {
                                            server.broadcast(new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                        } else {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        server.broadcast(new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID()));
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            robot.setPosition(new Position(x + 1, y));
                            if (!(movementCheck.fallingInPit(user))) {
                                server.broadcast(new Movement(user.getClientID(), x + 1, y));
                            } else {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                server.broadcast(new Reboot(user.getClientID()));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            server.broadcast(new Reboot(user.getClientID()));
                        }
                    }
                }
            }
        }
    }
}






