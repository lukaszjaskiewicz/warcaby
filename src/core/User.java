package core;

import java.util.*;

public class User {
    private int id;
    private String login;
    private String password;
    private String email;
    private String nickname;
    private char online;
    private char flag;
    private char role;
    private int sessionId;
    private Vector<Friend> friends;
    private int numberOfFriend;
    private int inGameId;
    private int inGame;

    public User() {
        friends = new Vector<>();
        this.numberOfFriend = 0;
        this.inGameId = -1;
    }

    public void addFriend(int id, String nick) {
        Friend af = new Friend();
        af.setId(id);
        af.setNick(nick);
        friends.add(af);
        DBConnection db = new DBConnection();
        db.addFriend(this.id, id);
        db.addFriend(id, this.id);
    }

    public void deleteFriend(String nick) {
        for(Friend f : friends) {
            if(f.getNick().equals(nick)) {
                friends.remove(f);
                DBConnection db=new DBConnection();
                db.deleteFriend(this.id,id);
                db.deleteFriend(id,this.id);
                break;
            }
        }
    }

    public void deleteOneFriend(String nick) {
        DBConnection db = new DBConnection();
        int id = db.getUserId(nick);
        db.deleteFriend(this.id,id);
        db.deleteFriend(id,this.id);
    }

    public void loadFriends()
    {
        DBConnection db=new DBConnection();
        friends=db.getFriends(this.id);
        this.numberOfFriend=friends.size();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public void setOnline(char online) { this.online = online; }

    public void setFlag(char flag) { this.flag = flag; }

    public void setRole(char role) { this.role = role; }

    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public void setInGameId(int inGameId) {
        this.inGameId = inGameId;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() { return nickname; }

    public char getOnline() { return online; }

    public char getFlag() { return flag; }

    public char getRole() { return role; }

    public int getSessionId() { return sessionId; }

    public Vector<Friend> getFriends() {
        return friends;
    }

    public int getNumberOfFriend() {
        return numberOfFriend;
    }

    public int getInGameId() {
        return inGameId;
    }

    public void makeMove(int[] coordinatesBefore, int[] coordinatesAfter) {

    }

    public void sendChatMessage(String message) {

    }

    public void resign() {

    }

    public void setInGame(int inGame) {
        this.inGame = inGame;
    }

    public int getInGame() {
        return inGame;
    }
}
