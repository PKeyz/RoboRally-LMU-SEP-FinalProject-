package bb.roborally.client.game;

import bb.roborally.client.board.BoardView;
import bb.roborally.client.chat.ChatView;
import bb.roborally.client.phase_info.PhaseInfoView;
import bb.roborally.client.player_inventory.PlayerInventoryView;
import bb.roborally.client.player_list.PlayerListView;
import bb.roborally.client.programming_interface.ProgrammingInterfaceView;
import bb.roborally.client.timer.TimerView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;

/**
 *
 * @author Bence Ament
 * @author Zeynab Baiani
 * @author Muqiu Wang
 * @author tolgaengin
 */
public class GameView {
    private final GridPane view = new GridPane();
    private final TimerView timer = new TimerView();
    private final PhaseInfoView phase = new PhaseInfoView();
    private final ChatView chat = new ChatView();
    private final PlayerListView players = new PlayerListView(PlayerListView.Kind.DETAILED);
    private final BoardView boardView = new BoardView();
    private final PlayerInventoryView playerInventoryView = new PlayerInventoryView();
    private final HBox controlBox = new HBox();
    private final ProgrammingInterfaceView programmingInterface = new ProgrammingInterfaceView();

    public GameView() {
        TabPane tabPane = new TabPane();
        Tab chatTab = new Tab("Chat", chat.getView());
        Tab playersTab = new Tab("Players", players.getView());
        tabPane.getTabs().addAll(playersTab, chatTab);

        GridPane leftGrid = new GridPane();
        RowConstraints leftSide1 = new RowConstraints();
        leftSide1.setPercentHeight(55);
        RowConstraints leftSide2 = new RowConstraints();
        leftSide2.setFillHeight(true);
        RowConstraints leftSide3 = new RowConstraints();
        leftSide3.setPercentHeight(20);
        leftGrid.getRowConstraints().addAll(leftSide1, leftSide2, leftSide3);
        leftGrid.setVgap(5);
        leftGrid.addRow(0, boardView.getView());
        leftGrid.addRow(1, playerInventoryView.getView());
        leftGrid.addRow(2, controlBox);

        GridPane rightGrid = new GridPane();
        RowConstraints rightSide1 = new RowConstraints();
        rightSide1.setPercentHeight(10);
        RowConstraints rightSide2 = new RowConstraints();
        rightSide2.setPercentHeight(20);
        RowConstraints rightSide3 = new RowConstraints();
        rightSide3.setPercentHeight(50);
        rightGrid.getRowConstraints().addAll(rightSide1, rightSide2, rightSide3);
        rightGrid.setVgap(10);
        rightGrid.addRow(0, timer.getView());
        rightGrid.addRow(1, phase.getView());
        rightGrid.addRow(2, tabPane);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(70);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(30);
        view.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        view.getColumnConstraints().addAll(column1, column2);
        view.addColumn(0, leftGrid);
        view.addColumn(1, rightGrid);
        applyStyle();
    }

    private void applyStyle() {
        view.setPadding(new Insets(10, 10, 10, 10));
        view.setStyle("-fx-background-color:linear-gradient(to bottom, #386D8B, #494986, #638395)");
        view.setHgap(20);
        controlBox.setStyle("-fx-background-color: rgba(214, 214, 231, 0.87)");
        controlBox.setAlignment(Pos.CENTER);
    }

    public Parent getView() {
        return view;
    }
    public TimerView getTimer() {
        return timer;
    }
    public PhaseInfoView getPhase() {
        return phase;
    }
    public BoardView getGameBoardView() {
        return boardView;
    }

    public PlayerInventoryView getPlayerInventoryView() {
        return playerInventoryView;
    }

    public ProgrammingInterfaceView getProgrammingInterface() {
        return programmingInterface;
    }
    public ChatView getChat() {
        return chat;
    }
    public PlayerListView getPlayers() {
        return players;
    }

    public void setControlToProgrammingInterface() {
        controlBox.getChildren().clear();
        controlBox.getChildren().add(programmingInterface.getView());
    }
    public void setControlToUpgradeShop() {
        //
    }
}
