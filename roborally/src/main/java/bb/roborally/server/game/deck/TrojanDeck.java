package bb.roborally.server.game.deck;

import bb.roborally.server.game.cards.PlayingCard;
import bb.roborally.server.game.cards.Trojan;
import bb.roborally.server.game.cards.Virus;

import java.util.ArrayList;

public class TrojanDeck {

    ArrayList<PlayingCard> trojanDeck = new ArrayList<>();

    public TrojanDeck(){
        fillTrojanDeck();
    }
    public void fillTrojanDeck(){
        for(int i = 0; i < 11; i++){
            trojanDeck.add(new Trojan());
        }
    }

    public void drawTrojanCard(){
        trojanDeck.remove(0);
    }

    public void addTrojanCard(){
        trojanDeck.add(new Trojan());
    }

    public ArrayList<PlayingCard> getTrojanDeck(){
        return trojanDeck;
    }
}