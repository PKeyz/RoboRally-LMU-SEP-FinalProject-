package bb.roborally.game.tiles;

import bb.roborally.data.messages.game_events.Reboot;
import bb.roborally.game.Robot;

import java.util.ArrayList;

public class RebootPoint extends Tile{
    ArrayList<Robot> rebootQueue = new ArrayList<>();
    @Override
    public String getName() {
        return "RebootPoint";
    }

    public Reboot needToReboot(Robot robot){
        rebootQueue.add(robot);
        return new Reboot(robot.getClientID());
    }
}
