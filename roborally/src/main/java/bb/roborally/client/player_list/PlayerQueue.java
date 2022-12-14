package bb.roborally.client.player_list;

import bb.roborally.client.robot_selector.Robot;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/**
 *
 * @author Bence Ament
 */
public class PlayerQueue {

    private final Player localPlayer = new Player(true);
    private final ArrayList<Player> players = new ArrayList<>(){{add(localPlayer);}};
    private final ObservableList<Player> displayablePlayers = FXCollections.observableArrayList(localPlayer);
    public ArrayList<Player> getPlayers() {
        return players;
    }
    public ObservableList<Player> getObservableListPlayers() {
        return displayablePlayers;
    }

    /**
     * @param id the id of the local player
     */
    public void setLocalPlayerId(int id) {
        localPlayer.setId(id);
    }

    public void addPlayer(int id, String name, Robot robot) {
        if (getPlayerById(id) == null) {
            Player other = new Player(id);
            other.add(name, robot);
            players.add(other);
            displayablePlayers.add(other);
        } else {
            Player present = getPlayerById(id);
            present.add(name, robot);
            displayablePlayers.removeIf(player -> player.getId() == present.getId());
            displayablePlayers.add(present);
        }
    }

    /**
     * @param id id of the user
     * @param ready new value of ready
     */
    public void setPlayerReady(int id, boolean ready) {
        getPlayerById(id).setReady(ready);
        displayablePlayers.set(displayablePlayers.indexOf(getPlayerById(id)), getPlayerById(id));
        if (!ready && id == getLocalPlayerId()) {
            localPlayer.mapSelectorProperty().set(false);
        }
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public int getLocalPlayerId() {
        return localPlayer.getId();
    }

    public boolean getLocalPlayerIdSet() {
        return localPlayer.isIdSet();
    }

    public BooleanBinding localPlayerIdSetProperty() {
        return localPlayer.idSetProperty();
    }

    public Player getPlayerById(int id) {
        for (Player player: players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public Player getDisplayablePlayerById(int id) {
        for (Player player: displayablePlayers) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    public int size() {
        return players.size();
    }

    /**
     * @return the robots of all active Players
     */
    public ArrayList<Robot> getRobots() {
        ArrayList<Robot> robots = new ArrayList<>();
        for (Player player: players) {
            if (player.getRobot() != null) {
                robots.add(player.getRobot());
            }
        }
        return robots;
    }
}
