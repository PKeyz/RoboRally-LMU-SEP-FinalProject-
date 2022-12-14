package bb.roborally.server.game;

import bb.roborally.server.game.cards.PlayingCard;

/**
 * procedure of the program that a robot has to follow based on the programming cards.
 * @author Veronika Heckel
 * @author Muqiu Wang
 * @author Tolga Engin
 * @author Zeynab Baiani
 * @author Bence Ament
 * @author  Philipp Keyzman
 */
public class Program {
    private final int REGISTER_COUNT = 5;
    PlayingCard[] program = new PlayingCard[REGISTER_COUNT];

    public void add(PlayingCard playingCard, int register) {
        program[register - 1] = playingCard;
    }

    public void set(String[] cardsYouGotNow) {
        int register = 1;
        for (String card: cardsYouGotNow) {
            add(PlayingCard.fromString(card), register);
            register += 1;
        }
    }

    public void reset() {
        for (int i = 0; i < REGISTER_COUNT; i++) {
            program[i] = null;
        }
    }

    public void resetOneRegister (int register){
        for (int i = 0; i < REGISTER_COUNT;i++){
            if (i +1 == register){
                program[i] = null;
            }
        }
    }

    public boolean isReady() {
        for (PlayingCard playingCard: program) {
            if (playingCard == null) {
                return false;
            }
        }
        return true;
    }

    public PlayingCard getCardInRegister(int register) {
        if (register <= 5 && register >=1) {
            return program[register - 1];
        }
       return null;
    }

    public void resetOneRegister(PlayingCard playingCard){
        for(int i = 0; i < REGISTER_COUNT; i++){
            if(program[i].isMarked()){
                program[i] = null;
            }
        }
    }
    public PlayingCard[] getProgram(){
        return program;
    }

}
