package bb.roborally.game.tiles;

import bb.roborally.data.messages.game_events.Movement;
import bb.roborally.game.Orientation;
import bb.roborally.game.Robot;

import java.util.ArrayList;


/**
 * @author Muqiu Wang
 */

public class PushPanel extends Tile{
    final int activationOrder = 3;
    private ArrayList<Integer> registers;
    private ArrayList<Orientation> orientations;
    private String type;
    private String isOnBoard;

    public PushPanel() {
    }
    public PushPanel(String type, String isOnBoard, ArrayList<Orientation> orientations, ArrayList<Integer> registers){
        this.type = type;
        this.setIsOnBoard(isOnBoard);
        this.setOrientations(orientations);
        this.registers = registers;
    }

    @Override
    public String getType() {
        return "PushPanel";
    }

    public ArrayList<Integer> getRegisters() {
        return registers;
    }

    public void setRegisters(ArrayList<Integer> registers) {
        this.registers = registers;
    }

    @Override
    public ArrayList<Orientation> getOrientations() {
        return orientations;
    }

    @Override
    public void setOrientations(ArrayList<Orientation> orientations) {
        this.orientations = orientations;
    }

    //public Movement pushPanelEffect(Robot robot, int activeRegister){
    //    if(registers.contains(activeRegister)){
    //        int column = robot.getPosition().getColumn();
    //        int row = robot.getPosition().getRow();
    //        if(this.orientations.contains(Orientation.RIGHT)){
    //            robot.getPosition().setColumn(column+1);
    //            return new Movement(robot.getClientID(), column+1, robot.getPosition().getRow());
    //        }else if(this.orientations.contains(Orientation.BOTTOM)){
    //            robot.getPosition().setRow(row+1);
    //            return new Movement(robot.getClientID(), robot.getPosition().getColumn(), robot.getPosition().getRow()+1);
    //        }else if(this.orientations.contains(Orientation.LEFT)){
    //            robot.getPosition().setColumn(column-1);
    //            return new Movement(robot.getClientID(), robot.getPosition().getColumn()-1, robot.getPosition().getRow());
    //        }else if(this.orientations.contains(Orientation.TOP)){
    //            robot.getPosition().setRow(row-1);
    //            return new Movement(robot.getClientID(), robot.getPosition().getColumn(), robot.getPosition().getRow()-1);
    //        }
    //    }else{
    //        return null;
    //    }
    //    return null;
    //}
}
