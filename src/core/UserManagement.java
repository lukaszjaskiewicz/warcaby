package core;

import java.util.Random;
import java.util.Vector;

public class UserManagement {
    private Vector<User> users;
    private Vector<Integer> loggedUsersId;
    private Vector<IdPlayingUsers> gameId;
    private int numberOfUsers;
    private int numberOfLoggedUsers;
    private int numberOfGames;

    public UserManagement() {
        this.numberOfUsers = 0;
        this.numberOfGames = 0;
        this.numberOfLoggedUsers = 0;
        this.users = new Vector<>();
        this.loggedUsersId = new Vector<>();
    }

    public void registration(int id, String login, String password, String email, String nickname) {
        DBConnection db = new DBConnection();
        DataHasher dh = new DataHasher();
        String hlogin = dh.encryption(login);
        String hpass = dh.encryption(password);
        db.addUserToDB(id, hlogin, hpass, email, nickname);
    }

    public User login(String login, String password) {
        Management m = Management.getInstance();
        DBConnection db = new DBConnection();
        DataHasher dh = new DataHasher();
        if(db.getUser(dh.encryption(login),dh.encryption(password)) != null) {
            Random rand = new Random();
            User log_user = db.getUser(dh.encryption(login),dh.encryption(password));

            int sessionId = rand.nextInt(Integer.MAX_VALUE);
            if (m.loggedUsersId.size() > 0) {
                while (m.loggedUsersId.contains(sessionId))
                    sessionId = rand.nextInt(Integer.MAX_VALUE);
            }

            m.loggedUsersId.add(sessionId);
            log_user.setSessionId(sessionId);
            m.users.add(log_user);
			System.out.println("Aktywne sesje: ");
            m.loggedUsersId.forEach( (token) -> System.out.println(token));

            db.setUserSessionToken(log_user.getId(), log_user.getSessionId());

            System.out.println("Poprawne logowanie!");
            return log_user;
        }
        else {
            System.out.println("Bledne logowanie!");
            return null;
        }
    }

    public User loginToken(int token) {
        Management m = Management.getInstance();
        DBConnection db = new DBConnection();
        if (m.loggedUsersId.contains(token)) {
            User log_user = db.getUserByToken(token);
            if (log_user != null) {
                System.out.println("Poprawne logowanie!");
                return log_user;
            }
            System.out.println("Bledne logowanie (błąd w bazie danych) !");
            return null;
        }
        else {
            System.out.println("Bledne logowanie!");
            return null;
        }
    }

    public boolean logout(String nickname, int token) {
        Management m = Management.getInstance();
        DBConnection db = new DBConnection();
        boolean userLoggedOut = db.logoutUser(nickname, token);
        if (userLoggedOut) {
            m.loggedUsersId.remove(m.loggedUsersId.indexOf(token));
        }
        return userLoggedOut;
    }

    public void setGameId(int user1, int user2) {
        IdPlayingUsers ipu = new IdPlayingUsers();
        ipu.setU1(user1);
        ipu.setU2(user2);
        gameId.add(ipu);
    }

    public Vector<User> getFriends(String nickname, int token) {
        DBConnection db = new DBConnection();
        Vector<User> friends = db.getFriendsWithNickname(nickname);
        return friends;
    }

    public Vector<User> getUsers() {
        return users;
    }

    public Vector<Integer> getLoggedUsersId() {
        return loggedUsersId;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public int getNumberOfLoggedUsers() {
        return numberOfLoggedUsers;
    }

    public int getNumberOfGame() {
        return numberOfGames;
    }

    public Vector<IdPlayingUsers> getGameId() {
        return gameId;
    }
}
