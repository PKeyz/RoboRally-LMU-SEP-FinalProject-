package bb.roborally.server.game;


import bb.roborally.server.game.activation.ActivationPhaseHandler;
import bb.roborally.server.game.board.Board;
import bb.roborally.server.game.deck.SpamDeck;
import bb.roborally.server.game.deck.TrojanDeck;
import bb.roborally.server.game.deck.VirusDeck;
import bb.roborally.server.game.deck.WormDeck;

/**
 * main class to initialize the game and to follow general game logic
 * @author Veronika Heckel
 * @author Muqiu Wang
 * @author Tolga Engin
 * @author Zeynab Baiani
 * @author Bence Ament
 * @autor  Philipp Keyzman
 */
public class Game {

    private final PlayerQueue playerQueue;
    private final RobotList robotList = new RobotList();
    private final String[] availableMaps = new String[] {"DizzyHighway"};
    private boolean mapSelected = false;
    private String selectedMap;
    private Board board = null;
    private SpamDeck spamDeck = null;
    private TrojanDeck trojanDeck = null;

    private VirusDeck virusDeck = null;

    private WormDeck wormDeck = null;

    public Game(int minPlayer) {
        playerQueue = new PlayerQueue(minPlayer);
    }

    public PlayerQueue getPlayerQueue() {
        return playerQueue;
    }

    public String[] getAvailableMaps() {
        return availableMaps;
    }

    public boolean isMapSelected() {
        return mapSelected;
    }

    public void setMapSelected(boolean mapSelected) {
        this.mapSelected = mapSelected;
    }

    public String getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(String selectedMap) {
        this.selectedMap = selectedMap;
    }

    public RobotList getRobotList() {
        return robotList;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public SpamDeck getSpamDeck() {
        return spamDeck;
    }

    public TrojanDeck getTrojanDeck() {
        return trojanDeck;
    }

    public VirusDeck getVirusDeck() {
        return virusDeck;
    }

    public WormDeck getWormDeck() {
        return wormDeck;
    }
}