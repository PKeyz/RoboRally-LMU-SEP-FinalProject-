package bb.roborally.data.messages.game_events;

import bb.roborally.data.messages.Envelope;
import bb.roborally.data.messages.Message;

public class SelectedDamage implements Message {
    private String cards;

    public SelectedDamage(String cards){
        this.cards=cards;
    }
    public SelectedDamage(){
    }

    public String getCards() {
        return cards;
    }

    public void setCards(String cards) {
        this.cards = cards;
    }

    @Override
    public String toJson() {
        return toEnvelope().toJson();
    }

    @Override
    public Envelope toEnvelope() {
        return new Envelope(Envelope.MessageType.SELECTED_DAMAGE, this);
    }
}