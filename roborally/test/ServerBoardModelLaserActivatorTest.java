import bb.roborally.protocol.map.Board;
import bb.roborally.server.Server;
import bb.roborally.server.game.Game;
import bb.roborally.server.game.Orientation;
import bb.roborally.server.game.Position;
import bb.roborally.server.game.User;
import bb.roborally.server.game.activation.ActivationPhaseHandler;
import bb.roborally.server.game.activation.BoardLaserActivator;
import bb.roborally.server.game.board.ServerBoard;
import bb.roborally.map.DizzyHighwayBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static bb.roborally.server.game.Orientation.LEFT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerBoardModelLaserActivatorTest {
	private static Server server;
	private static Game game;

	/*@BeforeAll
	public static void init(){
		Server server = new Server();
		Game game = server.getGame();
		game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));
	}*/

	/*
	is BoardLaserActivator ready for each
	 */
	@Test
	public void testRobotInsideLaser() throws IOException{
		Server server = new Server();
		Game game = server.getGame();
		game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

		User user1 = new User(0);
		user1.setName("user1");
		user1.setRobot(game.getRobotList().getRobotByFigureId(1));
		user1.getRobot().setPosition(new Position(6, 3));

		User user2 = new User(1);
		user2.setName("user2");
		user2.setRobot(game.getRobotList().getRobotByFigureId(2));
		user2.getRobot().setPosition(new Position(7, 4));


		game.getPlayerQueue().add(user1);
		game.getPlayerQueue().add(user2);

		BoardLaserActivator boardLaserActivator = new BoardLaserActivator(server,game);
		boardLaserActivator.activate();

		assertTrue(boardLaserActivator.isShootingEnded);
		assertEquals(1, user1.getProgrammingDeck().getDiscardPile().size());
		assertEquals("Spam", user1.getProgrammingDeck().getDiscardPile().get(0).getName());
	}

	@Test
	public void testRobotOutside() throws IOException{
		Server server = new Server();
		Game game = server.getGame();
		game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));
		User user1 = new User(0);
		user1.setName("user1");
		user1.setRobot(game.getRobotList().getRobotByFigureId(1));
		user1.getRobot().setPosition(new Position(6, 2));
		game.getPlayerQueue().add(user1);

		User user2 = new User(1);
		user2.setName("user2");
		user2.setRobot(game.getRobotList().getRobotByFigureId(2));
		user2.getRobot().setPosition(new Position(7, 4));
		game.getPlayerQueue().add(user2);

		BoardLaserActivator boardLaserActivator = new BoardLaserActivator(server,game);
		boardLaserActivator.activate();

		assertTrue(boardLaserActivator.isShootingEnded);
		//assertEquals(1, user1.getProgrammingDeck().getDiscardPile().size());
		//assertEquals("Spam", user1.getProgrammingDeck().getDiscardPile().get(0).getName());
	}

	@Test
	public void testBoardLaserShootWallOnly() throws IOException{
		Server server = new Server();
		Game game = server.getGame();
		game.setBoard(new ServerBoard(new DizzyHighwayBuilder().build().board()));

		User user1 = new User(0);
		user1.setName("user1");
		user1.setRobot(game.getRobotList().getRobotByFigureId(1));
		user1.getRobot().setPosition(new Position(5, 3));

		User user2 = new User(1);
		user2.setName("user2");
		user2.setRobot(game.getRobotList().getRobotByFigureId(2));
		user2.getRobot().setPosition(new Position(7, 4));

		game.getPlayerQueue().add(user1);
		game.getPlayerQueue().add(user2);

		game.getBoard().getWall().get(0).setPosition(3,3);
		game.getBoard().getAntenna().setPosition(0,3);
		game.getBoard().getBoardLaser().get(0).setPosition(4,3);

		ArrayList<Orientation> orientationLeft = new ArrayList<>();
		orientationLeft.add(LEFT);
		game.getBoard().getBoardLaser().get(0).getTile("Laser").setOrientations(orientationLeft);

		ActivationPhaseHandler.getRegister();
		BoardLaserActivator boardLaserActivator = new BoardLaserActivator(server,game);

		for(int i = 0; i <= 3; i++){
			boardLaserActivator.activate();
		}

		assertTrue(boardLaserActivator.isShootingEnded);
	}

	@Test
	public void testRobotTwoRobotsInsideLaserTest() throws IOException {
		Server server = new Server();
		Game game = server.getGame();
		game.setBoard(new ServerBoard(new Board("DizzyHighway")));

		User user1 = new User(0);
		user1.setName("user1");
		user1.setRobot(game.getRobotList().getRobotByFigureId(1));
		user1.getRobot().setPosition(new Position(7,6));

		User user2 = new User(1);
		user2.setName("user2");
		user2.setRobot(game.getRobotList().getRobotByFigureId(2));
		user2.getRobot().setPosition(new Position(6, 3));

		game.getPlayerQueue().add(user1);
		game.getPlayerQueue().add(user2);

		BoardLaserActivator boardLaserActivator = new BoardLaserActivator(server,game);
		boardLaserActivator.activate();

		assertTrue(boardLaserActivator.isShootingEnded);

		assertEquals(1, user1.getProgrammingDeck().getDiscardPile().size());
		assertEquals("Spam", user1.getProgrammingDeck().getDiscardPile().get(0).getName());

		//assertEquals(0, user2.getProgrammingDeck().getDiscardPile().size());

		assertEquals(1, user2.getProgrammingDeck().getDiscardPile().size());
		assertEquals("Spam", user2.getProgrammingDeck().getDiscardPile().get(0).getName());


	}


}
/*
create map
create user
set wall antenna robot position

laser antenna						isShootingEnd true + no drawDamage
laser wall same orientation			isShootingEnd true +  drawDamage
laser wall opposite orientation		isShootingEnd true + no drawDamage
laser wall both other or.			isShootingEnd true +  drawDamage

laser robot 						isShootingEnd true +  drawDamage
laser wall no robot					isShootingEnd true +  no drawDamage

 */
