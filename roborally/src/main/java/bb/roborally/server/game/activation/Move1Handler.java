package bb.roborally.server.game.activation;


import bb.roborally.protocol.game_events.Movement;
import bb.roborally.protocol.game_events.Reboot;
import bb.roborally.server.Server;
import bb.roborally.server.game.*;
import java.io.IOException;


public class Move1Handler {

    Server server;
    Game game;
    User user;

    public Move1Handler(Server server, Game game, User user) {
        this.server = server;
        this.game = game;
        this.user = user;
    }

    public void handle() throws IOException {
        Robot robot = user.getRobot();
        Position position = user.getRobot().getPosition();
        Orientation orientation = user.getRobot().getRobotOrientation();
        int x = position.getX();
        int y = position.getY();

        MovementCheck movementCheck = new MovementCheck(game.getBoard(), game);

        if (movementCheck.checkIfBlockedAlt(position, orientation) || movementCheck.robotForwardCheck(user.getRobot().getPosition(), user.getRobot().getRobotOrientation())) {
            server.broadcast(new Movement(user.getClientID(), x, y));
        } else {
            if (user.getRobot().getRobotOrientation() == Orientation.TOP) {
                robot.setPosition(new Position(x, y - 1));
                server.broadcast(new Movement(user.getClientID(), x, y - 1));
            } else if (user.getRobot().getRobotOrientation() == Orientation.LEFT) {
                robot.setPosition(new Position(x - 1, y));
                server.broadcast(new Movement(user.getClientID(), x - 1, y));
            } else if (user.getRobot().getRobotOrientation() == Orientation.BOTTOM) {
                robot.setPosition(new Position(x, y + 1));
                server.broadcast(new Movement(user.getClientID(), x, y + 1));
            } else if (user.getRobot().getRobotOrientation() == Orientation.RIGHT) {
                robot.setPosition(new Position(x + 1, y));
                server.broadcast(new Movement(user.getClientID(), x + 1, y)); //Fehler hier geht er rein nachdem er Bottom abgearbeitet hat
            }
            if (movementCheck.fallingInPit(user) || movementCheck.robotIsOffBoard(user)) {
                server.broadcast(new Reboot(user.getClientID()));
            } else {
                movementCheck.pushRobot(server, game, user, orientation);
            }
        }
    }
}


