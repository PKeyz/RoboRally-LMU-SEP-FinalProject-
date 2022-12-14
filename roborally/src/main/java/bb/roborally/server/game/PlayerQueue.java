package bb.roborally.server.game;

import bb.roborally.protocol.Message;
import bb.roborally.protocol.lobby.PlayerAdded;
import bb.roborally.protocol.lobby.PlayerStatus;
import bb.roborally.server.game.deck.ProgrammingDeck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * this class implements the logic of the player order in the game
 * @author Veronika Heckel
 * @author Muqiu Wang
 * @author Tolga Engin
 * @author Zeynab Baiani
 * @author Bence Ament
 * @author  Philipp Keyzman
 */
public class PlayerQueue {
    private final int minPlayer;
    private final int NO_MAP_SELECTOR = -1;
    private int mapSelectorClientId = NO_MAP_SELECTOR;
    private boolean isMapSelectorNotified = false;
    private final ArrayList<User> users = new ArrayList<>();
    private int buildUpPhaseCurrentUserId = -1;

    public PlayerQueue(int minPlayer) {
        this.minPlayer = minPlayer;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void add(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public boolean contains(int clientId) {
        for (User user: users) {
            if (user.getClientID() == clientId) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(User user) {
        return contains(user.getClientID());
    }

    /**
     * @param playerStatus message from the client
     *
     */
    public void update(PlayerStatus playerStatus) {
        User user = getUserById(playerStatus.getClientID());
        if (user != null) {
            user.readyProperty().set(playerStatus.isReady());
            if (mapSelectorClientId == -1 && playerStatus.isReady() && !user.isAI()) {
                mapSelectorClientId = playerStatus.getClientID();
                isMapSelectorNotified = false;
            } else if (mapSelectorClientId == user.getClientID() && !playerStatus.isReady()) {
                updateMapSelector();
            }
        }
    }

    public void remove(int clientId) {
        users.removeIf(user -> user.getClientID() == clientId);
        if (clientId == mapSelectorClientId) {
            updateMapSelector();
        }
    }

    public void remove(User user) {
        remove(user.getClientID());
    }

    public boolean isMapSelectorAvailable() {
        return mapSelectorClientId != -1;
    }

    public int getMapSelectorClientId() {
        return mapSelectorClientId;
    }

    /**
     * @return whether the game is ready to start, whether there are enough players
     */
    public boolean isGameReadyToStart() {
        int readyPlayerCount = 0;
        for (User user: users) {
            if (user.isReady()) {
                readyPlayerCount += 1;
            }
        }
        return readyPlayerCount >= minPlayer;
    }

    /**
     * @return whether all players are AIs
     */
    public boolean areAllPlayersAI() {
        for (User user: users) {
            if (!user.isAI()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return A list of messages to bring new clients up-to-date about the current state of the lobby and game
     */
    public ArrayList<Message> generatePlayersUpdate() {
        ArrayList<Message> messages = new ArrayList<>();
        for (User user: users) {
            PlayerAdded playerAdded = new PlayerAdded(user.getClientID(), user.getName(), user.getRobot().getFigureId());
            PlayerStatus playerStatus = new PlayerStatus(user.getClientID(), user.isReady());
            messages.add(playerAdded);
            messages.add(playerStatus);
        }
        return messages;
    }

    public User getUserById(int clientId) {
        for (User user: users) {
            if (user.getClientID() == clientId) {
                return user;
            }
        }
        return null;
    }


    private User getNextReadyPlayer() {
        for (User user: users) {
            if (user.isReady()) {
                return user;
            }
        }
        return null;
    }

    private void updateMapSelector() {
        if (getNextReadyPlayer() != null && !getNextReadyPlayer().isAI()) {
            mapSelectorClientId = getNextReadyPlayer().getClientID();
        } else {
            mapSelectorClientId = NO_MAP_SELECTOR;
        }
        isMapSelectorNotified = false;
    }

    public boolean isMapSelectorNotified() {
        return isMapSelectorNotified;
    }

    public void setMapSelectorNotified(boolean mapSelectorNotified) {
        isMapSelectorNotified = mapSelectorNotified;
    }

    /**
     * @return whether the Build-Up Phase finished or not
     */
    public boolean isBuildUpPhaseFinished() {
        boolean buildUpPhaseFinished = true;
        for (User user: getOnlineUsers()) {
            buildUpPhaseFinished = buildUpPhaseFinished && user.isStartingPointSet();
        }
        return buildUpPhaseFinished;
    }

    public ProgrammingDeck getProgrammingDeckById(int clientId) {
        return getUserById(clientId).getPlayerInventory().getProgrammingDeck();
    }

    public int[] getIncompleteProgramUserIds() {
        ArrayList<Integer> clientIds = new ArrayList<>();
        for (User user: getOnlineUsers()) {
            if (!user.getProgram().isReady()) {
                clientIds.add(user.getClientID());
            }
        }
        return  Arrays.stream(clientIds.toArray(new Integer[0])).mapToInt(Integer::intValue).toArray();
    }

    public ArrayList<User> getOnlineUsers() {
        ArrayList<User> onlineUsers = new ArrayList<>();
        for (User user: users) {
            if (user.isOnline()) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }
    public HashMap<Integer, String> getCurrentCards(int register) {
        HashMap<Integer, String> currentCards = new HashMap<>();
        for(User user: getOnlineUsers()){
            currentCards.put(user.getClientID(), user.getProgram().getCardInRegister(register).getName());
        }
        return currentCards;
    }

    public int getBuildUpPhaseCurrentUserId() {
        return buildUpPhaseCurrentUserId;
    }

    public void setNextBuildUpPhaseCurrentUser() {
        for (User user: getOnlineUsers()) {
            if (!user.isStartingPointSet()) {
                buildUpPhaseCurrentUserId = user.getClientID();
                return;
            }
        }
        resetBuildUpPhaseCurrentUserId();
    }

    public void resetBuildUpPhaseCurrentUserId() {
        buildUpPhaseCurrentUserId = -1;
    }

    public boolean areAllProgramsReady() {
        for (User user: getOnlineUsers()) {
            if (!user.getProgram().isReady()) {
                return false;
            }
        }
        return true;
    }
}
