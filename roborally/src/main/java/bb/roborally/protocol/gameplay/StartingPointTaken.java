package bb.roborally.protocol.gameplay;

import bb.roborally.protocol.Envelope;
import bb.roborally.protocol.Message;
import bb.roborally.protocol.Position;
import bb.roborally.server.game.Game;
import bb.roborally.server.game.User;

/**
 * @author Veronika Heckel
 */
public class StartingPointTaken implements Message {

    private Game game;
    private int x;
    private int y;
    private int clientID;

    public StartingPointTaken(){

    }

    public StartingPointTaken(int x, int y, int clientID){
        this.x = x;
        this.y = y;
        this.clientID = clientID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    @Override
    public String toJson() {
        return toEnvelope().toJson();
    }

    @Override
    public Envelope toEnvelope() {
        return new Envelope(Envelope.MessageType.STARTING_POINT_TAKEN, this);
    }
}
