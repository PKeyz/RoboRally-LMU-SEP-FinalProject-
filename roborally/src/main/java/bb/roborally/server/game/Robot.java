package bb.roborally.server.game;

import bb.roborally.server.game.board.Cell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * contains attributes and possible actions of a robot
 * @author Veronika Heckel
 * @author Muqiu Wang
 * @author Tolga Engin
 * @author Zeynab Baiani
 * @author Bence Ament
 * @autor  Philipp Keyzman
 */
public class Robot {
    private boolean available = true;
    private int figureId;
    private String name;
    private Position position;
    private Orientation robotOrientation = Orientation.RIGHT;


    public Robot(int figureId, String name) {
        this.figureId = figureId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Orientation getRobotOrientation() {
        return robotOrientation;
    }

    public void setRobotOrientation(Orientation robotOrientation) {
        this.robotOrientation = robotOrientation;
    }

    public int getFigureId() {
        return figureId;
    }

    public void setFigureId(int figureId) {
        this.figureId = figureId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }


    public ImageView getRobotElement() {
        Image image = new Image(getClass().getResource("/robots/demo_robot.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(Cell.CELL_HEIGHT);
        imageView.setFitWidth(Cell.CELL_WIDTH);
        return imageView;
    }

    @Override
    public String toString() {
        return getFigureId() + ": " + getName();
    }
}


