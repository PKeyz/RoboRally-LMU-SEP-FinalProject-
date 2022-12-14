import bb.roborally.map.ExtraCrispyBuilder;
import bb.roborally.server.Server;
import bb.roborally.server.game.Game;
import bb.roborally.protocol.Orientation;
import bb.roborally.protocol.Position;
import bb.roborally.server.game.User;
import bb.roborally.server.game.activation.BlueConveyorBeltActivator;
import bb.roborally.server.game.activation.GreenConveyorBeltActivator;
import bb.roborally.server.game.board.ServerBoard;
import bb.roborally.map.DizzyHighwayBuilder;
import bb.roborally.map.LostBearingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConveyorBeltActivatorsTests {
    @Test
    public void testGreenConveyorBeltNormal() throws IOException {
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(2, 9));
        user1.getRobot().setRobotOrientation(Orientation.RIGHT);
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(3, user1.getRobot().getPosition().getX());
        assertEquals(9, user1.getRobot().getPosition().getY());
    }

    @Test
    public void testGreenConveyorBeltNoTurning() throws IOException {
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new LostBearingsBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 1));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(3, user1.getRobot().getPosition().getX());
        assertEquals(1, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testGreenConveyorBeltWithFirstTurning() throws IOException {
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new LostBearingsBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 1));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(3, user1.getRobot().getPosition().getX());
        assertEquals(1, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testGreenConveyorBeltWithEndTurning() throws IOException {
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new LostBearingsBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 0));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(4, user1.getRobot().getPosition().getX());
        assertEquals(1, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.LEFT, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testGreenConveyorBeltsTurningOrientation() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new LostBearingsBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(3, 8));
        user1.getRobot().setRobotOrientation(Orientation.RIGHT);
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(4, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testGreenConveyorBeltsIsOffBoard() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new LostBearingsBuilder().build().board()));
        game.setSelectedMap("LostBearings");

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 9));
        user1.setStartingPoint(new Position(1,1));
        game.getPlayerQueue().add(user1);
        GreenConveyorBeltActivator greenConveyorBeltActivator = new GreenConveyorBeltActivator(server, game);
        greenConveyorBeltActivator.activate();
        assertEquals(0, user1.getRobot().getPosition().getX());
        assertEquals(0, user1.getRobot().getPosition().getY());
    }

    @Test
    public void testBlueConveyorBeltsTurningCurvePassedBy() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(3, 8));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(5, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsWithFirstTurning() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 8));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(6, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsNoTurning() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 8));
        user1.getRobot().setRobotOrientation(Orientation.TOP);
        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(6, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.TOP, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsWithEndTurning1() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(3, 7));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(4, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsWithEndTurning2() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(4, 6));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);
        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(4, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.RIGHT, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsWithStartTurning() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(10, 8));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);

        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(11, user1.getRobot().getPosition().getX());
        assertEquals(7, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.RIGHT, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsWith2Turnings1() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(10, 9));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);

        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(11, user1.getRobot().getPosition().getX());
        assertEquals(8, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBelts2Turnings2() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(12, 2));
        user1.getRobot().setRobotOrientation(Orientation.BOTTOM);

        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(11, user1.getRobot().getPosition().getX());
        assertEquals(1, user1.getRobot().getPosition().getY());
        assertEquals(Orientation.BOTTOM, user1.getRobot().getRobotOrientation());
    }

    @Test
    public void testBlueConveyorBeltsPit() throws IOException{
        Server server = new Server();
        Game game = server.getGame();
        game.setBoard(new ServerBoard(new ExtraCrispyBuilder().build().board()));
        game.setSelectedMap("ExtraCrispy");

        User user1 = new User(0);
        user1.setRobot(game.getRobotList().getRobotByFigureId(1));
        user1.getRobot().setPosition(new Position(5, 3));
        user1.setStartingPoint(new Position(1,1));

        game.getPlayerQueue().add(user1);
        BlueConveyorBeltActivator blueConveyorBeltActivator = new BlueConveyorBeltActivator(server, game);
        blueConveyorBeltActivator.activate();
        assertEquals(0, user1.getRobot().getPosition().getX());
        assertEquals(0, user1.getRobot().getPosition().getY());
    }
}
