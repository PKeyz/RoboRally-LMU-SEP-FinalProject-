package bb.roborally.server.game;


import bb.roborally.server.game.activation.ActivationPhaseHandler;
import bb.roborally.server.game.board.Board;
import bb.roborally.server.game.board.Cell;
import bb.roborally.server.game.deck.SpamDeck;
import bb.roborally.server.game.deck.TrojanDeck;
import bb.roborally.server.game.deck.VirusDeck;
import bb.roborally.server.game.deck.WormDeck;

import java.util.ArrayList;
import java.util.HashMap;

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
    private final String[] availableMaps = new String[] {"DizzyHighway", "DeathTrap", "ExtraCrispy",
            "LostBearings", "Twister"};
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

    public ArrayList<User> getAlreadyOnBelts(){
        ArrayList<User> alreadyOnBelts = new ArrayList<>();
        for(User user: playerQueue.getUsers()){
            Position position = user.getRobot().getPosition();
            if(board.get(position.getX(), position.getY()).getTile("ConveyorBelt") != null){
                alreadyOnBelts.add(user);
            }
        }
        return alreadyOnBelts;
    }

    public ArrayList<User> getUsersOrderedByDistance() {
        ArrayList<User> userOrderedByDistance = new ArrayList<>();
        HashMap<User, Integer> usersWithDistancePower = new HashMap<>();
        int[] usersDistancesPower = new int[playerQueue.getUsers().size()];
        Cell antenna = board.getAntenna();
        int xAntenna = antenna.getPosition().getX();
        int yAntenna = antenna.getPosition().getY();

        //save the users with corresponding distances(power) from Antenna
        for(int i = 0; i<playerQueue.getUsers().size(); i++){
            int x = playerQueue.getUsers().get(i).getRobot().getPosition().getX();
            int y = playerQueue.getUsers().get(i).getRobot().getPosition().getY();
            int xDifferToPower = (int) Math.pow(x - xAntenna, 2);
            int yDifferToPower = (int) Math.pow(y - yAntenna, 2);
            usersWithDistancePower.put(playerQueue.getUsers().get(i), xDifferToPower + yDifferToPower);
            usersDistancesPower[i] = xDifferToPower + yDifferToPower;
        }
        //sort the distances in ascending order
        for (int i = 0; i < usersDistancesPower.length; i++) {
            for (int j = 0; j < usersDistancesPower.length; j++) {
                if (usersDistancesPower[i] < usersDistancesPower[j]) {
                    int temp = usersDistancesPower[i];
                    usersDistancesPower[i] = usersDistancesPower[j];
                    usersDistancesPower[j] = temp;
                }
            }
        }
        //add the users into the list ordered by distance
        for(int i = 0; i<usersDistancesPower.length; i++){
            for(User key: usersWithDistancePower.keySet()){
                if(usersWithDistancePower.get(key) == usersDistancesPower[i]){
                    userOrderedByDistance.add(key);
                }
            }
        }
        return userOrderedByDistance;
    }

    public ArrayList<Position> getUsersPositions(){
        ArrayList<Position> usersPositions = new ArrayList<>();
        for(User user: playerQueue.getUsers()){
            usersPositions.add(user.getRobot().getPosition());
        }
        return usersPositions;
    }
}