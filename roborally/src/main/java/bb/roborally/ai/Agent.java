package bb.roborally.ai;

import bb.roborally.protocol.Envelope;
import bb.roborally.protocol.Message;
import bb.roborally.protocol.Orientation;
import bb.roborally.protocol.Position;
import bb.roborally.protocol.chat.SendChat;
import bb.roborally.protocol.connection.HelloServer;
import bb.roborally.protocol.connection.Welcome;
import bb.roborally.protocol.game_events.CheckPointReached;
import bb.roborally.protocol.game_events.Movement;
import bb.roborally.protocol.game_events.PlayerTurning;
import bb.roborally.protocol.gameplay.*;
import bb.roborally.protocol.lobby.PlayerValues;
import bb.roborally.protocol.lobby.SetStatus;
import bb.roborally.protocol.map.GameStarted;
import bb.roborally.protocol.map.MapSelected;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
    private final String ip;
    private final int port;
    private Socket socket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private int id;
    private BoardModel boardModel;
    private Position position;
    private Orientation orientation = Orientation.RIGHT;
    private CardModel[] yourCards = null;
    private int checkpoints = 0;
    private String phase;
    private HashMap<Integer, String> activeCards = null;

    public Agent(String ip, int port) {
        this.ip = ip;
        this.port = port;
        setupLogger();
    }

    public void start() {
        connect();
        register();
        listen();
    }

    private void connect() {
        boolean connected = false;
        while (!connected) {
            try {
                socket = new Socket(ip, port);
                outputStream = new PrintWriter(socket.getOutputStream(), true);
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String helloClientJson = inputStream.readLine();
                LOGGER.info("Incoming: " + helloClientJson);
                Envelope helloClientEnvelope = Envelope.fromJson(helloClientJson);
                if (helloClientEnvelope.getMessageType() == Envelope.MessageType.HELLO_CLIENT) {
                    HelloServer helloServer = new HelloServer(true);
                    broadcast(helloServer);
                    String welcomeJson = inputStream.readLine();
                    Envelope welcomeEnvelope = Envelope.fromJson(welcomeJson);
                    if (welcomeEnvelope.getMessageType() == Envelope.MessageType.WELCOME) {
                        Welcome welcome = (Welcome) welcomeEnvelope.getMessageBody();
                        setId(welcome.getClientID());
                        connected = true;
                        LOGGER.info("Bot connected to the server");
                    }
                    Thread.sleep(1000);
                }
                Thread.sleep(1000);
            } catch (IOException e) {
                try {
                    Thread.sleep(1000);
                    LOGGER.severe(e.getMessage());
                } catch (InterruptedException ex) {
                    LOGGER.severe(e.getMessage());
                }
            } catch (InterruptedException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

    private void register() {
        int FIGURE = 3;
        PlayerValues playerValues = new PlayerValues(getName(), FIGURE);
        broadcast(playerValues);
        SendChat sendChat = new SendChat("Bot in the house!", -1);
        broadcast(sendChat);
        SetStatus setStatus = new SetStatus(true);
        broadcast(setStatus);
    }

    private void listen() {
        while(!socket.isClosed()) {
            try {
                String json = inputStream.readLine();
                LOGGER.info("Incoming: " + json);
                Envelope envelope = Envelope.fromJson(json);
                if (envelope.getMessageType() == Envelope.MessageType.ALIVE) {
                    broadcast(envelope.getMessageBody());
                } else if (envelope.getMessageType() == Envelope.MessageType.GAME_STARTED) {
                    this.boardModel = new BoardModel(((GameStarted) envelope.getMessageBody()).board());
                } else if (envelope.getMessageType() == Envelope.MessageType.ACTIVE_PHASE) {
                    ActivePhase activePhase = (ActivePhase) envelope.getMessageBody();
                    if (activePhase.getPhase() == 0) {
                        this.phase = "BuildUp";
                    } else if (activePhase.getPhase() == 1) {
                        this.phase = "Programming";
                    } else if (activePhase.getPhase() == 3) {
                        this.phase = "Active";
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.CURRENT_PLAYER) {
                    CurrentPlayer currentPlayer = (CurrentPlayer) envelope.getMessageBody();
                    if (phase.equals("BuildUp")) {
                        pickStartingPoint();
                    } else if (phase.equals("Active")) {
                        if (currentPlayer.getClientID() == id) {
                            broadcast(new PlayCard(activeCards.get(id)));
                        }
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.CURRENT_CARDS) {
                    CurrentCards currentCards = (CurrentCards) envelope.getMessageBody();
                    this.activeCards = currentCards.getActiveCards();
                } else if (envelope.getMessageType() == Envelope.MessageType.YOUR_CARDS) {
                    yourCards = CardModel.fromStringArray(((YourCards) envelope.getMessageBody()).getCardsInHand());
                } else if (envelope.getMessageType() == Envelope.MessageType.MAP_SELECTED) {
                    MapSelected mapSelected = (MapSelected) envelope.getMessageBody();
                    if (mapSelected.getMap().equals("DeathTrap")) {
                        orientation = Orientation.LEFT;
                    } else {
                        orientation = Orientation.RIGHT;
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.CHECK_POINT_REACHED) {
                    CheckPointReached checkPointReached = (CheckPointReached) envelope.getMessageBody();
                    if (checkPointReached.getClientID() == id) {
                        setCheckpoints(getCheckpoints() + 1);
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.TIMER_STARTED) {
                    Program program = createProgram(yourCards);
                    int register = 1;
                    for (CardModel card: program.getProgram()) {
                        SelectedCard selectedCard = new SelectedCard(card.type().toString(), register);
                        broadcast(selectedCard);
                        register += 1;
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.MOVEMENT) {
                    Movement movement = (Movement) envelope.getMessageBody();
                    if (movement.getClientID() == id) {
                        setPosition(new Position(movement.getX(), movement.getY()));
                    }
                }  else if (envelope.getMessageType() == Envelope.MessageType.PLAYER_TURNING) {
                    PlayerTurning playerTurning = (PlayerTurning) envelope.getMessageBody();
                    if (playerTurning.getClientID() == id) {
                        if (playerTurning.getRotation() == "clockwise") {
                            if (getOrientation() == Orientation.TOP) {
                                setOrientation(Orientation.RIGHT);
                            } else if (getOrientation() == Orientation.RIGHT) {
                                setOrientation(Orientation.BOTTOM);
                            } else if (getOrientation() == Orientation.BOTTOM) {
                                setOrientation(Orientation.LEFT);
                            } else if (getOrientation() == Orientation.LEFT) {
                                setOrientation(Orientation.TOP);
                            }
                        } else {
                            if (getOrientation() == Orientation.TOP) {
                                setOrientation(Orientation.LEFT);
                            } else if (getOrientation() == Orientation.RIGHT) {
                                setOrientation(Orientation.TOP);
                            } else if (getOrientation() == Orientation.BOTTOM) {
                                setOrientation(Orientation.RIGHT);
                            } else if (getOrientation() == Orientation.LEFT) {
                                setOrientation(Orientation.BOTTOM);
                            }
                        }
                    }
                } else if (envelope.getMessageType() == Envelope.MessageType.GAME_FINISHED) {
                    LOGGER.info("Game Finished: Bot stopping");
                    System.exit(0);
                }
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

    private void pickStartingPoint() {
        int x = 0;
        int y = 0;
        boolean found = false;
        while (!found && x < boardModel.xSize()) {
            while (!found && y < boardModel.ySize()) {
                if (boardModel.get(x, y).hasTile(TileModel.TileType.START_POINT)) {
                    found = true;
                } else {
                    y += 1;
                }
            }
            if (!found) {
                x += 1;
            }
        }
        SetStartingPoint setStartingPoint = new SetStartingPoint(x, y);
        position = new Position(x, y);
        broadcast(setStartingPoint);
    }

    protected abstract Program createProgram(CardModel[] availableCards);

    protected abstract String getName();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    public CardModel[] getYourCards() {
        return yourCards;
    }

    public void setYourCards(CardModel[] yourCards) {
        this.yourCards = yourCards;
    }

    private static void setupLogger(){
        LOGGER.setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler("log/bot.log");
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            LOGGER.addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void broadcast(Message message) {
        outputStream.println(message.toJson());
        LOGGER.info("Outgoing: " + message.toJson());
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(int checkpoints) {
        this.checkpoints = checkpoints;
    }
}