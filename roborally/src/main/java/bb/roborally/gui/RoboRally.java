package bb.roborally.gui;

import bb.roborally.data.messages.*;
import bb.roborally.data.util.User;
import bb.roborally.gui.game.GameModel;
import bb.roborally.gui.game.GameView;
import bb.roborally.gui.game.GameViewModel;
import bb.roborally.gui.start_menu.StartMenuModel;
import bb.roborally.gui.start_menu.StartMenuView;
import bb.roborally.gui.start_menu.StartMenuViewModel;
import bb.roborally.networking.ClientReaderThread;
import bb.roborally.networking.ClientWriterThread;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class RoboRally extends Application {
    Stage primaryStage;
    StartMenuModel startMenuModel;
    GameModel gameModel;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.startMenuModel = new StartMenuModel();
        StartMenuView startMenuView = new StartMenuView();
        StartMenuViewModel startMenuViewModel = new StartMenuViewModel(this, startMenuModel, startMenuView);
        Scene scene = new Scene(startMenuView.getParent(), 900, 600);
        this.primaryStage.setTitle("RoboRally");
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    @Override
    public void stop() {
        if (gameModel != null && gameModel.getUser() != null) {
            LogoutRequest logoutRequest = new LogoutRequest(gameModel.getUser());
            try {
                dataOutputStream.writeUTF(logoutRequest.toJson());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void login() {
        try {
            Socket client = new Socket(startMenuModel.getIp(), startMenuModel.getPort());
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            dataInputStream = new DataInputStream(client.getInputStream());
            User user = new User(startMenuModel.getUsername());
            LoginRequest loginRequest = new LoginRequest(user);
            dataOutputStream.writeUTF(loginRequest.toJson());
            String response = dataInputStream.readUTF();
            Envelope envelope = Envelope.fromJson(response);
            if (envelope.getMessageType().equals("LoginConfirmation")) {
                LoginConfirmation loginConfirmation = (LoginConfirmation) envelope.getMessageBody();
                startMenuModel.setErrorMessage("");
                openGameView();
                gameModel.process(envelope);
                gameModel.setUser(user);
                ClientReaderThread readerThread = new ClientReaderThread(dataInputStream, gameModel);
                ClientWriterThread writerThread = new ClientWriterThread(dataOutputStream, gameModel);
                readerThread.start();
                writerThread.start();
            } else if (envelope.getMessageType().equals("LoginError")) {
                LoginError loginError = (LoginError) envelope.getMessageBody();
                startMenuModel.setErrorMessage(loginError.getMessage());
            } else {
                startMenuModel.setErrorMessage("Error: Could Not Connect To Server!");
            }
        } catch (UnknownHostException ex) {
            startMenuModel.setErrorMessage("Server was not found! (Check ip and port)");
        } catch (IOException ex) {
            startMenuModel.setErrorMessage("An I/O exception occurred! (Check ip and port)");
        }
    }


    private void openStartMenuView() {
        this.startMenuModel = new StartMenuModel();
        StartMenuView startMenuView = new StartMenuView();
        StartMenuViewModel startMenuViewModel = new StartMenuViewModel(this, startMenuModel, startMenuView);
        this.primaryStage.getScene().setRoot(startMenuView.getParent());
    }

    private void openGameView() {
        this.gameModel = new GameModel();
        GameView gameView = new GameView();
        GameViewModel gameViewModel = new GameViewModel(this, gameModel, gameView);
        this.primaryStage.getScene().setRoot(gameView.getParent());
    }

    public static void main(String[] args) {
        launch();
    }
}