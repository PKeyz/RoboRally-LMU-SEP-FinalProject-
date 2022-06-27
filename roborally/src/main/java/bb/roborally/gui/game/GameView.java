package bb.roborally.gui.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class GameView {
    private GridPane view;
    private ListView<String> chatListView;
    private TextField messageField;
    private Button sendButton;
    private Label time;
    private Label playerMat;
    private Label upgradeCards;
    private Label phases;
    private Label gameBoard;
    public GameView() {
        buildUI();
    }

    public void buildUI() {
        view = new GridPane();
        VBox chatContainer = new VBox();
        VBox cards = new VBox();
        chatListView = new ListView<>();
        HBox chatFormHolder = new HBox();
        HBox timer = new HBox();
        HBox phase = new HBox();
        HBox program = new HBox();
        HBox upgrade = new HBox();
        HBox gameBoards = new HBox();
        VBox rightSide = new VBox(timer,phase,chatContainer);
        VBox leftSide = new VBox(gameBoards,cards);
        messageField = new TextField();
        sendButton = new Button("Send");
        time = new Label("TIMER");
        phases = new Label("PHASE");
        gameBoard = new Label("Game Board");
        playerMat = new Label("Player Mat");
        upgradeCards = new Label("Upgrade Cards");
        cards.getChildren().addAll(program,upgrade);
        chatFormHolder.getChildren().addAll(messageField, sendButton);
        chatFormHolder.setSpacing(20);
        timer.setPrefHeight(50);
        timer.getChildren().addAll(time);
        phase.setPrefHeight(50);
        phase.getChildren().addAll(phases);
        gameBoards.getChildren().addAll(gameBoard);
        gameBoards.setPrefHeight(300);
        gameBoards.setPrefWidth(500);
        program.getChildren().addAll(playerMat);
        upgrade.getChildren().addAll(upgradeCards);
        timer.setAlignment(Pos.CENTER);
        phase.setAlignment(Pos.CENTER);
        gameBoards.setAlignment(Pos.CENTER);
        program.setAlignment(Pos.CENTER);
        upgrade.setAlignment(Pos.CENTER);
        chatContainer.getChildren().addAll(chatListView, chatFormHolder);
        //rightSide.getChildren().addAll(chatContainer,timer,phase);
        //leftSide.getChildren().addAll(gameBoards);
        timer.setStyle("-fx-background-color: red");
        phase.setStyle("-fx-background-color: green");
        gameBoards.setStyle("-fx-background-color: blue");
        program.setStyle("-fx-background-color: pink");
        upgrade.setStyle("-fx-background-color: yellow");
        rightSide.setAlignment(Pos.BOTTOM_RIGHT);
        leftSide.setAlignment(Pos.TOP_LEFT);
        view.addColumn(1,rightSide);
        //view.addRow(2, chatContainer);
        //view.addRow(0,timer);
        //view.addRow(1,phase);
        //rightSide.setHgap(10);
        //view.setVgap(10);
        rightSide.setPadding(new Insets(20,20,20,20));
        leftSide.setPadding(new Insets(20, 20, 20, 20));
        //view.setAlignment(Pos.BOTTOM_RIGHT);
        view.addColumn(0,leftSide);
    }

    public ListView<String> getChatListView() {
        return this.chatListView;
    }

    public TextField getMessageField() {
        return this.messageField;
    }

    public Button getSendButton() {
        return this.sendButton;
    }

    public Parent getParent() {
        return view;
    }

    //private void messagePlayers(){
    //    //check if the name is already taken or emtpty
    //    if (view.getUsernameField().getText() == null || view.getUsernameField().getText().trim().isEmpty()) {
    //        view.getInfoLabel().setText("Error: Missing username!");
    //    }
    //    //sends messages using UTF8 coding
    //    else{
    //        String username = view.getUsernameField().getText();
    //        int robotIndex = (int) view.getRobotComboBox().getValue();
    //        PlayerValues playerValues = new PlayerValues(username, robotIndex);
    //        try {
    //            NetworkConnection.getInstance().getDataOutputStream().writeUTF(playerValues.toJson());
    //        } catch (IOException e) {
    //            throw new RuntimeException(e);
    //        }
//
    //    }
//
    //}

}
