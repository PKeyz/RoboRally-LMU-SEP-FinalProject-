package bb.roborally.server;

import bb.roborally.data.messages.*;
import bb.roborally.data.messages.Error;
import bb.roborally.data.messages.chat.ReceivedChat;
import bb.roborally.data.messages.chat.SendChat;
import bb.roborally.data.messages.connection.Alive;
import bb.roborally.data.messages.gameplay.*;
import bb.roborally.data.messages.lobby.PlayerAdded;
import bb.roborally.data.messages.lobby.PlayerStatus;
import bb.roborally.data.messages.lobby.PlayerValues;
import bb.roborally.data.messages.lobby.SetStatus;
import bb.roborally.data.messages.map.MapSelected;
import bb.roborally.data.messages.map.SelectMap;
import bb.roborally.game.Game;
import bb.roborally.game.PlayerQueue;
import bb.roborally.game.User;
import bb.roborally.game.board.Board;
import bb.roborally.game.cards.PlayingCard;
import bb.roborally.game.map.DizzyHighway;
import bb.roborally.game.tiles.StartPoint;
//import bb.roborally.game.map.DizzyHighway;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    private final ClientList clientList = new ClientList();
    private final Game game = new Game(1); // TODO: cmd arg, 1 for testing purposes
    private final ChatHistory chatHistory = new ChatHistory();
    public static void main(String[] args) {
        Server server = new Server();
        server.registerUsers();
    }

    /**
     * Waits for and handles the Login Requests of Users.
     */
    public void registerUsers() {
        try {
            int PORT = 6868;
            ServerSocket server = new ServerSocket(PORT);
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("Server started running on " + inetAddress.getHostAddress() + ":" + PORT);
            while (true) {
                Socket clientSocket = server.accept();
                if(clientSocket != null) {
                    ServerThread serverThread = new ServerThread(this, clientSocket);
                    serverThread.start();
                }
            }
        }
        catch(Exception e) {
            System.out.println("ServerError: " +  e.getMessage());
        }
    }

    public void logout(User user) {
        clientList.clearClientList();
        game.getPlayerQueue().remove(user);
    }

    private void broadcast(Message message) throws IOException {
        for (Socket socket: clientList.getAllClients()) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(message.toJson());
        }
    }

    /**
     * This method can be used to broadcast messages to subsets of all users.
     * @throws IOException
     */
    private void broadcastOnly(Message message, int targetClientId) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(clientList.getClient(targetClientId).getOutputStream());
        dataOutputStream.writeUTF(message.toJson());
    }

    private void broadcastExcept(Message message, int exceptClientId) throws IOException {
        for (Socket socket: clientList.getAllClientsExcept(exceptClientId)) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(message.toJson());
        }
    }

    public void updateUser(int clientId) throws IOException {
        for (ReceivedChat receivedChat: chatHistory.getPublicMessages()) {
            broadcastOnly(receivedChat, clientId);
        }
        for (Message message: game.getPlayerQueue().generatePlayersUpdate()) {
            broadcastOnly(message, clientId);
        }
    }

    public void process(Alive alive, User user) {
        user.setUserStatus(User.UserStatus.VERIFIED);
    }

    public void process(PlayerValues playerValues, User user) throws IOException {
        if (game.getRobotList().isRobotAvailable(playerValues.getFigure())) {
            user.setName(playerValues.getName());
            user.setRobot(game.getRobotList().getRobotByFigureId(playerValues.getFigure()));
            game.getRobotList().makeUnavailable(playerValues.getFigure());
            PlayerAdded playerAdded = new PlayerAdded(user.getClientID(), user.getName(), playerValues.getFigure());
            game.getPlayerQueue().add(user);
            broadcast(playerAdded);
        } else {
            Error error = new Error("Robot is already taken! Choose another one.");
            broadcastOnly(error, user.getClientID());
        }
    }

    public void process(SetStatus setStatus, User user) throws IOException {
        user.setReady(setStatus.isReady());
        PlayerStatus playerStatus = new PlayerStatus(user.getClientID(), user.isReady());
        game.getPlayerQueue().update(playerStatus);
        broadcast(playerStatus);
        // Send the SelectMap message if necessary
        if (game.getPlayerQueue().isMapSelectorAvailable() && !game.getPlayerQueue().isMapSelectorNotified()) {
            SelectMap selectMap = new SelectMap(game.getAvailableMaps());
            broadcastOnly(selectMap, game.getPlayerQueue().getMapSelectorClientId());
            game.getPlayerQueue().setMapSelectorNotified(true);
        }
    }

    public void process(SendChat sendChat, User user) throws IOException {
        if (sendChat.getTo() == -1) {
            ReceivedChat receivedChat = new ReceivedChat(sendChat.getMessage(), user.getClientID(), false);
            chatHistory.addMessage(receivedChat);
            broadcast(receivedChat);
        } else {
            ReceivedChat receivedChat = new ReceivedChat(sendChat.getMessage(), user.getClientID(), true);
            broadcastOnly(receivedChat, user.getClientID());
            broadcastOnly(receivedChat, sendChat.getTo());
        }
    }

    public void process(MapSelected mapSelected, User user) throws IOException {
        if (game.getPlayerQueue().isGameReadyToStart()) {
            if (user.getClientID() == game.getPlayerQueue().getMapSelectorClientId()) {
                if (Arrays.stream(game.getAvailableMaps()).anyMatch(map -> map.equals(mapSelected.getMap()))) {
                    game.setMapSelected(true);
                    game.setSelectedMap(mapSelected.getMap());
                    if (game.getSelectedMap().equals("DizzyHighway")) {
                        Board dizzyHighway = new Board(DizzyHighway.buildDizzyHighway());
                        game.setBoard(dizzyHighway);
                        broadcast(dizzyHighway);
                    }
                    broadcast(new ActivePhase(0));
                }
            }
        } else {
            Error error = new Error("Error: There are not enough ready players in the lobby!");
            broadcastOnly(error, user.getClientID());
        }
    }

    public void process(SetStartingPoint setStartingPoint, User user) throws IOException {
        if (!user.isStartingPointSet()) {
            int x = setStartingPoint.getX();
            int y = setStartingPoint.getY();
            if (game.getBoard().get(x, y).hasTile("StartPoint")) {
                StartPoint startPoint = (StartPoint) game.getBoard().get(x, y).getTile("StartPoint");
                if (!startPoint.isTaken()) {
                    // StartPoint can be set
                    user.setStartingPointSet(true);
                    startPoint.setTaken(true);
                    StartingPointTaken startingPointTaken = new StartingPointTaken(x, y, user.getClientID());
                    broadcast(startingPointTaken);
                    if (game.getPlayerQueue().isBuildUpPhaseFinished()) {
                        startProgrammingPhase();
                    }
                }
            }
        } else {
            // TODO: User already set StartPoint, what to do
        }
    }

    private void startProgrammingPhase() throws IOException {
        ActivePhase activePhase = new ActivePhase(2);
        broadcast(activePhase);
        for (User user: game.getPlayerQueue().getUsers()) {
            user.getProgram().reset();
            ArrayList<PlayingCard> hand = user.getProgrammingDeck().drawHand();
            YourCards yourCards = new YourCards(hand);
            broadcastOnly(yourCards, user.getClientID());
            if (user.getProgrammingDeck().isReshuffleNeeded()) {
                ShuffleCoding shuffleCoding = new ShuffleCoding(user.getClientID());
                broadcast(shuffleCoding);
            }
            NotYourCards notYourCards = new NotYourCards(user.getClientID(), hand.size());
            broadcastExcept(notYourCards, user.getClientID());
        }
    }

    public void process(SelectedCard selectedCard, User user) throws IOException {
        PlayingCard card = PlayingCard.fromString(selectedCard.getCard());
        user.getProgram().add(card, selectedCard.getRegister());
        CardSelected cardSelected;
        if (card == null) {
            cardSelected = new CardSelected(user.getClientID(), selectedCard.getRegister(), false);
        } else {
            cardSelected = new CardSelected(user.getClientID(), selectedCard.getRegister(), true);
        }
        broadcast(cardSelected);
        if (user.getProgram().isReady()) {
            SelectionFinished selectionFinished = new SelectionFinished(user.getClientID());
            broadcast(selectionFinished);
            TimerStarted timerStarted = new TimerStarted();
            broadcast(timerStarted);
            (new Thread() { public void run() {
                try {
                    Thread.sleep(30000);
                    int[] incompleteProgramUsers = game.getPlayerQueue().getIncompleteProgramUserIds();
                    TimerEnded timerEnded = new TimerEnded(incompleteProgramUsers);
                    broadcast(timerEnded);
                    for (int clientId: incompleteProgramUsers) {
                        //CardsYouGotNow cardsYouGotNow = new CardsYouGotNow();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } }).start();
        } else {

        }
    }

    public ClientList getClientList() {
        return clientList;
    }
}
