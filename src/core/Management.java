package core;

import java.util.*;

public class Management {
    public static Vector<Game> games;
    public static int numberGames;
    public static Vector<User> users;
    public static Vector<Integer> loggedUsersId;
    public static Vector<Integer> usersInGame;
    public static Management instance = null;
    public static Vector<User> searchingForGame;

    private Management() {
        this.numberGames = 0;
        this.games = new Vector<Game>();
        this.users = new Vector<User>();
        this.usersInGame = new Vector<Integer>();
        this.loggedUsersId = new Vector<Integer>();
        this.searchingForGame = new Vector<User>();
    }

    public static Management getInstance(){
        if(instance==null){
            instance = new Management();
        }
        return instance;
    }

    public int newGame(int id1, int id2) {
        DBConnection db = new DBConnection();
        int id = db.getNextGameId();
        Game new_game = new Game(id, id1, id2);
        games.add(new_game);
        db.addGame(id2, id2);
        numberGames++;
        return id;
    }

    public void deleteGame(int id) {
        for(Game g : games) {
            if(g.getId() == id) {
                games.remove(g);
                break;
            }
        }
        DBConnection db = new DBConnection();
        db.deleteGame(id);
    }

    public Game getGameById(int id){
        DBConnection db = new DBConnection();
        if(db.getGameById(id) != null) {
            Game game = db.getGameById(id);

            System.out.println("Pomyslne pobranie gry!");
            return game;
        }
        else {
            System.out.println("Blad pobierania gry!");
            return null;
        }
    }
}
