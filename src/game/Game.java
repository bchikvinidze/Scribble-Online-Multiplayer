package game;

import login.User;


import javax.websocket.Session;
import java.io.IOException;

//SEND TEXT TO PLAYER USING THIS!!!!!!!
//session.getBasicRemote().sendText(text);


public class Game {
    public static final int N_ROUNDS = 18;
    public static final int MAX_PLAYERS = 6;

    private int playerCount;
    private int curRound;
    private Round[] rounds;
    private Player[] players;

    public Game() {

        players = new Player[MAX_PLAYERS]; // Better make this as ArrayList, which changes size as new players register.
        rounds = new Round[N_ROUNDS];
        playerCount = 0;
        curRound = 0;
    }

    public synchronized int registerSession(Session session, User user) {
        Player newPlayer = new Player(session, user); // Better to pass player as argument (will be easier for testing) for dependency injection
        players[playerCount] = newPlayer;
        playerCount++;
        if(playerCount == 2)
        {
            new Thread(() -> {
                try
                {
                    begin();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }).start();
        }
        return playerCount - 1;
    }

    private void begin() throws IOException, InterruptedException { // better name: play() (because this method covers the whole gameplay)
        for(; curRound < N_ROUNDS; curRound++)
        {
            rounds[curRound] = new Round(players[curRound % playerCount]);
            Round CurrentRound = rounds[curRound];

            CurrentRound.OnRoundBegin(players);
            // Game in Progress
            CurrentRound.OnRoundEnd(players);
        }
        Player p = GetWinner();
    }


    private Player GetWinner() {
        Player winner = null;
        int maxScore = 0;
        for(Player p : players)
        {
            if(p != null && p.GetScore() > maxScore)
            {
                maxScore = p.GetScore();
                winner = p;
            }
        }
        return winner;

        // Debug winner won the game
    }

    public void stroke(String start, int id) throws IOException {
        for(int i = 0; i < playerCount; i++)
            if(players[i] != null && i != id)
                players[i].notifyPlayer(start);
    }

    public void CheckGuessFromGame(int PlayerIndex, String guess) throws IOException {
        if(playerCount < 2 || curRound == 18)
        {
            for(Player p : players)
                if(p != null)
                    p.notifyPlayer("C," + players[PlayerIndex].GetName() + ": " + guess);

        }
        Round round = rounds[curRound];
        int res = round.CheckGuess(guess);
        if(res == 1)
        {
            round.OnCorrectGuess(players, PlayerIndex);
        }
        else if(res == 2) // do we need this?
        {
            round.OnCloseGuess(players[PlayerIndex]);
        }
        else
        {
            round.OnIncorrectGuess(players, PlayerIndex, guess);
        }
    }
    public void unregister(int playerIndex){
        players[playerIndex] = null;
    }
}