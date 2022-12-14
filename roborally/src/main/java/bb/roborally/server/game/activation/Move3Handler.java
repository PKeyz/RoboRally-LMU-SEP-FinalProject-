package bb.roborally.server.game.activation;


import bb.roborally.protocol.Orientation;
import bb.roborally.protocol.Position;
import bb.roborally.protocol.game_events.Movement;
import bb.roborally.protocol.game_events.Reboot;
import bb.roborally.server.Server;
import bb.roborally.server.game.*;
import java.io.IOException;


/**
 * @author Veronika Heckel
 * @author tolgaengin
 */
public class Move3Handler {

    Server server;
    Game game;
    User user;

    public Move3Handler(Server server, Game game, User user) {
        this.server = server;
        this.game = game;
        this.user = user;
    }

    /**
     * Class manages the movements of  aRobot for one step. It considers the Pt-Case and the Off-Board Case.
     * In the case of having multiple Robots in one row - the moving Robot is capable of pushing other Robots. Walls inf front of a Robot in the single- and multi- Robot-Moving case
     * are built in. Walls between neighboring Robots are also handled.
     * @throws IOException
     */

    public void handle() throws IOException {
        Robot robot = user.getRobot();
        Position position = user.getRobot().getPosition();
        Orientation orientation = user.getRobot().getRobotOrientation();
        int x = position.getX();
        int y = position.getY();
        MovementCheck movementCheck = new MovementCheck(game.getBoard(), game);
        if (movementCheck.checkIfBlockedAlt(position, orientation, 0)) {
            Movement movement = new Movement(user.getClientID(), x, y);                                                                                          // first initial check if one Robot can move
            server.broadcast(movement);
        } else {
            if (robot.getRobotOrientation() == Orientation.TOP) {

                Position currentField = new Position(position.getX(), position.getY() - 2);                                                                     //current field - is two steps ahead of Robot
                if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                    // Move 3
                    Orientation orientationFirst = Orientation.TOP;                                                                                                 //if Movement is possible -- iterating over Robot Orientations
                    if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                        for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                break;
                            }else{
                                if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 2) && (!movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, - 2))){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                    }
                                }
                            }
                        }
                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {                                     //check if all Neighbors are blocked (last neighbor is blocked -> no movement
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                    server.broadcast(movement);
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,-1))) {          //if there is one field free ahead -->> all members go one step ahead
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {                                                                                                    //try-catch-Block: handling Off-board --> Reboot
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {                      //handling pit --> Reboot
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                            server.broadcast(movement);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 2) && ((!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,-1)) ||(!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,-2)))) {          //if tehre is a wall two steps ahead of the last member --> all Robots move two steps ahead
                           int oldPositionY = 0;
                           int newPositionY = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i),0,-1)) {
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else{
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionY - oldPositionY == -1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2);
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else {
                            int oldPositionY = 0;
                            int newPositionY = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {             //if three steps are possible --> go three stepss for all Robots
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0,-1)){
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, -2)){                                               //if there is a pit after only two steps
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else{
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 3));
                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionY - oldPositionY == -1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                                server.broadcast(movement);
                                            }else if(newPositionY - oldPositionY == -2){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2);
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 3);
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        }
                    } else {
                        try  {
                            robot.setPosition(new Position(x, y - 3));                                          //handling one single User
                            if (movementCheck.fallingInPit(user, 0, 0)){
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }else{
                                Movement movement = new Movement(user.getClientID(), x , y - 3);
                                server.broadcast(movement);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            Reboot reboot = new Reboot(user.getClientID());
                            server.broadcast(reboot);
                        }

                    }
                } else if (!movementCheck.checkIfBlockedAlt(new Position(position.getX(), position.getY() - 1), orientation, 0)) {
                    //Move only 2
                    currentField = new Position(position.getX(), position.getY() - 1);
                    if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                        Orientation orientationFirst = Orientation.TOP;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                    }
                                }
                            }

                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,-1))) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)){
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            } else {
                                int oldPositionY = 0;
                                int newPositionY = 0;
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0,-1)){
                                                oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                                newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            }else {
                                                oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2));
                                                newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            }
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                if(newPositionY - oldPositionY == -1){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                                    server.broadcast(movement);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 2);
                                                    server.broadcast(movement);
                                                }
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            // Move 2x
                            robot.setPosition(new Position(x, y - 2));
                            try {
                                if (movementCheck.fallingInPit(user, 0, 0)){
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x, y -2);
                                    server.broadcast(movement);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }

                        }
                    } else {
                        // Move only 1
                        Orientation orientationFirst = Orientation.TOP;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                    break;
                                }else{
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.BOTTOM, -1);
                                }
                            }
                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() - 1);
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            try {
                                robot.setPosition(new Position(x, y - 1));
                                if (movementCheck.fallingInPit(user, 0, 0)){
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x, y - 1);
                                    server.broadcast(movement);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }

                        }
                    }
                }
            }else if (user.getRobot().getRobotOrientation() == Orientation.LEFT) {
                Position currentField;
                //Move 3
                if (!movementCheck.checkIfBlockedAlt(new Position(position.getX() - 2, position.getY()), orientation, 0)) {
                    Orientation orientationFirst = Orientation.LEFT;
                    if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                        for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                break;
                            }else{
                                if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 2) && (!movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), -2,0))){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                    }
                                }
                            }
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                    server.broadcast(movement);
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),-1,0))) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try{
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                            server.broadcast(movement);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 2) && ((!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),-1,0)) || (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),-2,0)))) {
                            int oldPositionX = 0;
                            int newPositionX = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try{
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i),-1,0)) {
                                            oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                        }else{
                                            oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionX - oldPositionX == -1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1 , game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2 , game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else {
                            int oldPositionX = 0;
                            int newPositionX = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try{
                                        if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), -1, 0)){
                                            oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                        }else if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), -2,0)){
                                            oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                        }else {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 3, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionX - oldPositionX == -1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }else if(newPositionX - oldPositionX == -2){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 3, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        }
                    } else {
                        try{
                            robot.setPosition(new Position(x - 3, y));
                            if (movementCheck.fallingInPit(user, 0, 0)){
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }else{
                                Movement movement = new Movement(user.getClientID(), x - 3, y);
                                server.broadcast(movement);
                            }
                        }catch(IndexOutOfBoundsException e){
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            Reboot reboot = new Reboot(user.getClientID());
                            server.broadcast(reboot);
                        }
                    }
                } else if (!movementCheck.checkIfBlockedAlt(new Position(position.getX() - 1, position.getY()), orientation, 0)) {
                    //Move only 2
                    currentField = new Position(position.getX(), position.getY() - 1);
                    if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                        Orientation orientationFirst = Orientation.LEFT;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                    }
                                }
                            }

                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),-1,0))) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try{
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            } else {
                                int oldPositionX = 0;
                                int newPositionX = 0;
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try{
                                            if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), -1,0)){
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }else {
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                if(newPositionX - oldPositionX == -1){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            // Move 2x
                            try{
                                robot.setPosition(new Position(x - 2, y));
                                if (movementCheck.fallingInPit(user, 0, 0)) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x - 2, y);
                                    server.broadcast(movement);
                                }
                            }catch(IndexOutOfBoundsException e){
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }
                        }
                    } else {
                        // Move only 1
                        Orientation orientationFirst = Orientation.LEFT;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                    break;
                                }else{
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.RIGHT, -1);
                                }
                            }
                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try{
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() - 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            try{
                                robot.setPosition(new Position(x - 1, y));
                                if (movementCheck.fallingInPit(user, 0, 0)) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x -1, y);
                                    server.broadcast(movement);
                                }
                            }catch(IndexOutOfBoundsException e){
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }
                        }
                    }
                }
            } else if (user.getRobot().getRobotOrientation() == Orientation.BOTTOM) {
                Position currentField;
                if (!movementCheck.checkIfBlockedAlt(new Position(position.getX(), position.getY() + 2), orientation, 0)) {
                    // Move 3
                    Orientation orientationFirst = Orientation.BOTTOM;
                    if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                        for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                            if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                break;
                            }else{
                                if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 2) && (!movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0,  2))){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                    }
                                }
                            }
                        }

                        if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                    server.broadcast(movement);
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,1))) {
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));

                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1);
                                            server.broadcast(movement);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 2) && ((!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,1)) || (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,2)))) {
                            int oldPositionY = 0;
                            int newPositionY = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i),0,1)) {
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() , game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else{
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() , game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionY - oldPositionY == 1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1);
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2);
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        } else {
                            int oldPositionY = 0;
                            int newPositionY = 0;
                            for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                    try {
                                        if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 1)){
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0,2)){
                                            oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2));
                                            newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                        }else {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 3));

                                        }
                                        if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }else{
                                            if(newPositionY - oldPositionY == 1){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1);
                                                server.broadcast(movement);
                                            }else if(newPositionY - oldPositionY == 2){
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2);
                                                server.broadcast(movement);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 3);
                                                server.broadcast(movement);
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                        server.broadcast(reboot);
                                    }
                                }
                            }
                        }
                    } else {
                        try {
                            robot.setPosition(new Position(x, y + 3));
                            if (movementCheck.fallingInPit(user, 0, 0)) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }else{
                                Movement movement = new Movement(user.getClientID(), x, y + 3);
                                server.broadcast(movement);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            RebootHandler rebootHandler = new RebootHandler(server, game, user);
                            rebootHandler.reboot();
                            Reboot reboot = new Reboot(user.getClientID());
                            server.broadcast(reboot);
                        }
                    }
                } else if (!movementCheck.checkIfBlockedAlt(new Position(position.getX(), position.getY() + 1), orientation, 0)) {
                    //Move only 2
                    currentField = new Position(position.getX(), position.getY() + 1);
                    if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                        Orientation orientationFirst = Orientation.BOTTOM;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                    }
                                }
                            }

                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {

                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),0,1))) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            } else {
                                int oldPositionY = 0;
                                int newPositionY = 0;
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0,1)){
                                                oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                                newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            }else {
                                                oldPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2));
                                                newPositionY = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY();
                                            }
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                if(newPositionY - oldPositionY == 1){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1);
                                                    server.broadcast(movement);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 2);
                                                    server.broadcast(movement);
                                                }
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            // Move 2x
                            try {
                                robot.setPosition(new Position(x, y + 2));
                                if (movementCheck.fallingInPit(user, 0, 0)) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x, y + 2);
                                    server.broadcast(movement);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }
                        }
                    } else {
                        // Move only 1
                        Orientation orientationFirst = Orientation.BOTTOM;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                    break;
                                }else{
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.TOP, -1);
                                }
                            }
                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY() + 1);
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            try {
                                robot.setPosition(new Position(x, y + 1));
                                if (movementCheck.fallingInPit(user, 0, 0)) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x , y + 1);
                                    server.broadcast(movement);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }
                        }
                    }
                }
            } else if (user.getRobot().getRobotOrientation() == Orientation.RIGHT) {
                Position currentField;
                if (!movementCheck.checkIfBlockedAlt(new Position(position.getX() + 2, position.getY()), orientation, 0)) {
                    // Move 3
                    if (!movementCheck.checkIfBlockedAlt(new Position(position.getX() + 2, position.getY()), orientation, 0)) {
                        Orientation orientationFirst = Orientation.RIGHT;
                        if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                            for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                    movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                    break;
                                }else{
                                    if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                        break;
                                    }else{
                                        if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 2) && (!movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 2, 0))){
                                            movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                            break;
                                        }else{
                                            movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                        }
                                    }
                                }
                            }
                            if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                        server.broadcast(movement);
                                    }
                                }
                            } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),1,0))) {
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                server.broadcast(movement);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 2) && ((!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),1,0)) || (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),2,0))))  {
                                int oldPositionX = 0;
                                int newPositionX = 0;
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i),1,0)) {
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }else{
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                if(newPositionX - oldPositionX == 1){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            } else {
                                int oldPositionX = 0;
                                int newPositionX = 0;
                                for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                    if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                        try {
                                            if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 1,0)){
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }else if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i),2,0)){
                                                oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                            }else {
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 3, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            }
                                            if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }else{
                                                if(newPositionX - oldPositionX == 1){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }else if(newPositionX - oldPositionX == 2){
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 3, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                            rebootHandler.reboot();
                                            Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                            server.broadcast(reboot);
                                        }
                                    }
                                }
                            }
                        } else {
                            try {
                                robot.setPosition(new Position(x + 3, y));
                                if (movementCheck.fallingInPit(user, 0, 0)) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }else{
                                    Movement movement = new Movement(user.getClientID(), x + 3, y);
                                    server.broadcast(movement);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                rebootHandler.reboot();
                                Reboot reboot = new Reboot(user.getClientID());
                                server.broadcast(reboot);
                            }
                        }
                    }
                } else if (!movementCheck.checkIfBlockedAlt(new Position(position.getX() + 1, position.getY()), orientation, 0)) {
                        //Move only 2
                        currentField = new Position(position.getX() + 1, position.getY());
                        if (!movementCheck.checkIfBlockedAlt(currentField, orientation, 0)) {
                            Orientation orientationFirst = Orientation.RIGHT;
                            if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                                for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                    if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                        break;
                                    }else{
                                        if(movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 1)){
                                            movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                            break;
                                        }else{
                                            movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                        }
                                    }
                                }

                                if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                    for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                        if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                            server.broadcast(movement);
                                        }
                                    }
                                } else if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 1) && (!movementCheck.fallingInPit(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size()-1),1,0))) {
                                    for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                        if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                            try {
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                    RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                    rebootHandler.reboot();
                                                    Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                    server.broadcast(reboot);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }
                                        }
                                    }
                                } else {
                                    int oldPositionX = 0;
                                    int newPositionX = 0;
                                    for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                        if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                            try {
                                                if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 1,0)){
                                                    oldPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                    newPositionX = game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX();
                                                }else {
                                                    game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                }
                                                if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                    RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                    rebootHandler.reboot();
                                                    Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                    server.broadcast(reboot);
                                                }else{
                                                    if(newPositionX - oldPositionX == 1){
                                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                        server.broadcast(movement);
                                                    }else if(movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 1,0)){
                                                        Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 2, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                        server.broadcast(movement);
                                                    }
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Move 2x
                                try {
                                    robot.setPosition(new Position(x + 2, y));
                                    if (movementCheck.fallingInPit(user, 0, 0)) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(user.getClientID());
                                        server.broadcast(reboot);
                                    }else{
                                        Movement movement = new Movement(user.getClientID(),x + 2, y);
                                        server.broadcast(movement);
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);;
                                }
                            }
                        } else {
                            // Move only 1
                            Orientation orientationFirst = Orientation.RIGHT;
                            if (movementCheck.robotForwardCheck(game.getPlayerQueue().getUsers().get(0), game.getPlayerQueue().getUsers().get(1), orientationFirst, 1) && (!movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(0).getRobot().getPosition(),orientationFirst,0))) {
                                for (int i = 1; i < game.getPlayerQueue().getUsers().size(); i++) {           //check if Players are neighbors - store them in extra list
                                    if (movementCheck.checkIfBlockedAlt(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition(), orientationFirst, 0)){
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                        break;
                                    }else{
                                        movementCheck.checkIfLastTwoAreNeighbors(game.getPlayerQueue().getUsers().get(i-1), game.getPlayerQueue().getUsers().get(i), Orientation.LEFT, -1);
                                    }
                                }
                                if (movementCheck.checkIfBlockedAlt(movementCheck.getNeighbors().get(movementCheck.getNeighbors().size() - 1).getRobot().getPosition(), orientationFirst, 0)) {
                                    for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                        if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                            game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                            Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                            server.broadcast(movement);
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < game.getPlayerQueue().getUsers().size(); i++) {
                                        if (movementCheck.getNeighbors().contains(game.getPlayerQueue().getUsers().get(i))) {
                                            try {
                                                game.getPlayerQueue().getUsers().get(i).getRobot().setPosition(new Position(game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY()));
                                                if (movementCheck.fallingInPit(game.getPlayerQueue().getUsers().get(i), 0, 0)) {
                                                    RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                    rebootHandler.reboot();
                                                    Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                    server.broadcast(reboot);
                                                }else{
                                                    Movement movement = new Movement(game.getPlayerQueue().getUsers().get(i).getClientID(), game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getX() + 1, game.getPlayerQueue().getUsers().get(i).getRobot().getPosition().getY());
                                                    server.broadcast(movement);
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                RebootHandler rebootHandler = new RebootHandler(server, game, game.getPlayerQueue().getUsers().get(i));
                                                rebootHandler.reboot();
                                                Reboot reboot = new Reboot(game.getPlayerQueue().getUsers().get(i).getClientID());
                                                server.broadcast(reboot);
                                            }
                                        }
                                    }
                                }
                            } else {
                                try {
                                    robot.setPosition(new Position(x + 1, y));
                                    if (movementCheck.fallingInPit(user, 0, 0)) {
                                        RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                        rebootHandler.reboot();
                                        Reboot reboot = new Reboot(user.getClientID());
                                        server.broadcast(reboot);
                                    }else{
                                        Movement movement = new Movement(user.getClientID(), x + 1, y);
                                        server.broadcast(movement);
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    RebootHandler rebootHandler = new RebootHandler(server, game, user);
                                    rebootHandler.reboot();
                                    Reboot reboot = new Reboot(user.getClientID());
                                    server.broadcast(reboot);
                                }
                            }
                        }
                    }
                }
            }
        }
    }







