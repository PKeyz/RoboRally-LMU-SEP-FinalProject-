import bb.roborally.server.Server;
import bb.roborally.server.game.Game;
import bb.roborally.server.game.Orientation;
import bb.roborally.server.game.Position;
import bb.roborally.server.game.User;
import bb.roborally.server.game.activation.Move2Handler;
import bb.roborally.server.game.board.Board;
import bb.roborally.server.game.map.DizzyHighway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Move3HandlerTest {
        private static Server server;
        private static Game game;

        @BeforeAll
        public static void init(){
            server = new Server();
            game = server.getGame();
            game.setBoard(new Board(DizzyHighway.buildDizzyHighway()));
        }

        @Test
        public void testMove3normalMoveForward() throws IOException {           //Right: erkannt, Bottom: Problem, Left: Problem, Top: Problem
            User user1 = new User(0);
            user1.setName("user1");
            user1.setRobot(game.getRobotList().getRobotByFigureId(1));
            user1.getRobot().setPosition(new Position(0,3));
            user1.getRobot().setRobotOrientation(Orientation.RIGHT);
            game.getPlayerQueue().add(user1);
            Move2Handler move2Handler = new Move2Handler(server, game, user1);
            move2Handler.handleAlt();
            assertEquals(3, user1.getRobot().getPosition().getX());










        }

        @Test
        public void testMove3FallingInPit() throws IOException {
            User user1 = new User(0);
            user1.setName("user1");
            user1.setRobot(game.getRobotList().getRobotByFigureId(1));
            user1.getRobot().setPosition(new Position(7,7));
            user1.getRobot().setRobotOrientation(Orientation.LEFT);
            game.getPlayerQueue().add(user1);
            Move2Handler move2Handler = new Move2Handler(server, game, user1);
            move2Handler.handleAlt();
            assertEquals(0, user1.getRobot().getPosition().getX());
            assertEquals(0, user1.getRobot().getPosition().getY());

        }

        @Test
        public void testMove3BlockedByWallOnSameField() throws IOException{
            User user1 = new User(0);
            user1.setName("user1");
            user1.setRobot(game.getRobotList().getRobotByFigureId(1));
            user1.getRobot().setPosition(new Position(6,5));
            user1.getRobot().setRobotOrientation(Orientation.LEFT);
            game.getPlayerQueue().add(user1);
            Move2Handler Move2Handler = new Move2Handler(server, game, user1);
            Move2Handler.handleAlt();
            assertEquals(6, user1.getRobot().getPosition().getX());
            assertEquals(5, user1.getRobot().getPosition().getY());
        }

        @Test
        public void testMove3BlockedByWallOnNextField() throws IOException{
            User user1 = new User(0);
            user1.setName("user1");
            user1.setRobot(game.getRobotList().getRobotByFigureId(1));
            user1.getRobot().setPosition(new Position(3,6));
            user1.getRobot().setRobotOrientation(Orientation.TOP);
            game.getPlayerQueue().add(user1);
            Move2Handler Move2Handler = new Move2Handler(server, game, user1);
            Move2Handler.handleAlt();
            assertEquals(3, user1.getRobot().getPosition().getX());
            assertEquals(6, user1.getRobot().getPosition().getY());
        }
    }