package bb.roborally.server;

import bb.roborally.data.messages.*;
import bb.roborally.data.messages.connection.Alive;
import bb.roborally.data.messages.lobby.PlayerAdded;
import bb.roborally.data.messages.lobby.PlayerValues;
import bb.roborally.data.util.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    private final int PORT = 6868;
    public ClientList clientList = new ClientList();
    public static void main(String[] args){
        Server server = new Server();
        server.registerUsers();
    }

    /**
     * Waits for and handles the Login Requests of Users.
     */
    public void registerUsers() {
        try {
            ServerSocket server = new ServerSocket(PORT);
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("Server started running on " + inetAddress.getHostAddress() + ":" + PORT);
            while(true) {
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

    /**
     * Processes valid LoginRequests, upon successful login starts a new ServerThread for the User and sends
     * out notifications. Upon unsuccessful login attempt it sends out an error message.
     * @param user
     * @param socket
     * @throws IOException
     */
    private void handleLoginRequest(User user, Socket socket) throws IOException {
        if (!clientList.containsClient(user)) {
            clientList.addClient(user, socket);
            LoginConfirmation loginConfirmation = new LoginConfirmation(user);
            broadcast(loginConfirmation.toEnvelope(), null, null);
            ServerThread messageRouterThread = new ServerThread(this, socket);
            messageRouterThread.start();
        } else {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            LoginError loginError = new LoginError(user, "The username '" + user.getName() + "' is already taken!");
            dataOutputStream.writeUTF(loginError.toJson());
            socket.close();
        }
    }

    /**
     * This method can be used to broadcast messages to subsets of all users.
     * @param envelope The message to be broadcast
     * @param whitelist The list of users who must receive the message
     * @param blacklist the list of users who mustn't recieve the message
     * @throws IOException
     */
    private void broadcast(Envelope envelope, User[] whitelist, User[] blacklist) throws IOException {
        if (whitelist != null) {
            for (User recipient: whitelist) {
                if (clientList.getClientSocket(recipient) != null ){
                    DataOutputStream dataOutputStream = new DataOutputStream(clientList.getClientSocket(recipient).getOutputStream());
                    dataOutputStream.writeUTF(envelope.toJson());
                }
            }
        } else if (blacklist != null) {
            for (User recipient: clientList.getUsers()) {
                if (!Arrays.asList(blacklist).contains(recipient)) {
                    DataOutputStream dataOutputStream = new DataOutputStream(clientList.getClientSocket(recipient).getOutputStream());
                    dataOutputStream.writeUTF(envelope.toJson());
                }
            }
        }
        else {
            for (User recipient: clientList.getUsers()) {
                DataOutputStream dataOutputStream = new DataOutputStream(clientList.getClientSocket(recipient).getOutputStream());
                dataOutputStream.writeUTF(envelope.toJson());
            }
        }
    }

    public synchronized void process(Envelope envelope) throws IOException {
        if (envelope.getMessageType() == Envelope.MessageType.CHAT_MESSAGE) {
            ChatMessage chatMessage = (ChatMessage) envelope.getMessageBody();
            process(chatMessage);
        } else if (envelope.getMessageType() == Envelope.MessageType.LOGOUT_REQUEST) {
            LogoutRequest logoutRequest = (LogoutRequest) envelope.getMessageBody();
            process(logoutRequest);
        } else if (envelope.getMessageType() == Envelope.MessageType.PLAYER_VALUES) {

        }
    }

    private void process(ChatMessage chatMessage) throws IOException {
        broadcast(chatMessage.toEnvelope(), null, null);
    }

    private void process(LogoutRequest logoutRequest) throws IOException {
        clientList.removeClient(logoutRequest.getUser());
        LogoutConfirmation logoutConfirmation = new LogoutConfirmation(logoutRequest.getUser());
        broadcast(logoutConfirmation.toEnvelope(), null, null);
    }

    public void process(Alive alive, User user) {
        user.setUserStatus(User.UserStatus.VERIFIED);
    }

    public void process(PlayerValues playerValues, User user) throws IOException {
        // TODO: Check that the Robot is unique, username must not be unique -> in ClientList
        user.setName(playerValues.getName());
        user.setFigure(playerValues.getFigure());
        PlayerAdded playerAdded = new PlayerAdded(user.getClientID(), user.getName(), user.getFigure());
        broadcast(playerAdded.toEnvelope(), null, null);
    }

}
