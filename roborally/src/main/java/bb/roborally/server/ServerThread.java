package bb.roborally.server;

import bb.roborally.data.messages.Envelope;
import bb.roborally.data.messages.connection.Alive;
import bb.roborally.data.messages.connection.HelloClient;
import bb.roborally.data.messages.connection.HelloServer;
import bb.roborally.data.messages.connection.Welcome;
import bb.roborally.data.messages.lobby.PlayerValues;
import bb.roborally.data.util.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

public class ServerThread extends Thread{
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Server server;
    private User user;

    public ServerThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Receives, processes and forwards messages from Clients.
     */
    public void run(){
        connect();
        String json = "";
        try{
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            while(!socket.isClosed()) {
                // Receive messages from User and forward them to the Server for execution
                json = dataInputStream.readUTF();
                System.out.println(json);
                Envelope envelope = Envelope.fromJson(json);
                if (envelope.getMessageType() == Envelope.MessageType.ALIVE) {
                    Alive alive = (Alive) envelope.getMessageBody();
                    server.process(alive, this.user);
                } else if (envelope.getMessageType() == Envelope.MessageType.PLAYER_VALUES) {
                    PlayerValues playerValues = (PlayerValues) envelope.getMessageBody();
                    server.process(playerValues, user);
                } else {
                    server.process(envelope);
                }
            }
        }
        catch(Exception e) {
            System.out.println("ServerThreadError: " + e.getMessage());
        }
    }

    public void connect() {
        try {
            HelloClient helloClient = new HelloClient();
            this.dataOutputStream.writeUTF(helloClient.toJson());
            String helloServerJson = this.dataInputStream.readUTF();
            System.out.println(helloServerJson);
            Envelope helloServerEnvelope = Envelope.fromJson(helloServerJson);
            if (helloServerEnvelope.getMessageType() == Envelope.MessageType.HELLO_SERVER) {
                HelloServer helloServer = (HelloServer) helloServerEnvelope.getMessageBody();
                int clientID = server.clientList.size();
                this.user = new User(clientID, helloServer.isAI());
                server.clientList.addClient(user, socket);
                Welcome welcome = new Welcome(clientID);
                dataOutputStream.writeUTF(welcome.toJson());
                // Connection is built up -> AliveChecker must be started
                AliveChecker aliveChecker = new AliveChecker(server, dataOutputStream, user);
                Timer timer = new Timer();
                timer.schedule(aliveChecker, 0, 5000);
            } else {
                // Error: incorrect message type
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
