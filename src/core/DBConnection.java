package core;

import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;

public class DBConnection {
    private Connection connection;
    private Statement statement;
    private ResultSet rs;

    public DBConnection() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@212.182.24.105:1521:xe","wrobelm","Pro-Checkers2018");
            //System.out.println("You are connected to database!");
            statement = connection.createStatement();
        }
        catch(Exception ex) {
            System.out.println("#DBConnection(): Error: "+ex);
        }
    }



    public User getUser(String login, String password) {
        User user = null;
        try {
            String query = "SELECT * FROM users WHERE user_login='"+login+"'";
            rs = statement.executeQuery(query);
            if(rs.next()) {
                int user_id = Integer.parseInt(rs.getString("user_id"));
                String user_login = rs.getString("user_login");
                String user_password = rs.getString("user_password");
                String user_email = rs.getString("user_mail");
                String user_nickname = rs.getString("user_nickname");
                char user_flag = rs.getString("user_flag").toCharArray()[0];
                char user_roles = rs.getString("user_roles").toCharArray()[0];
                if(user_password.equals(password)) {
                    User us = new User();
                    us.setId(user_id);
                    us.setLogin(user_login);
                    us.setPassword(user_password);
                    us.setEmail(user_email);
                    us.setNickname(user_nickname);
                    us.setOnline('1');
                    us.setFlag(user_flag);
                    us.setRole(user_roles);
                    user = us;
                    rs = statement.executeQuery("UPDATE users SET user_online = '1' WHERE user_login='"+login+"'");
                }
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return user;
    }

    public User getUserByToken(int token) {
        User user = null;
        try {
            String query = "SELECT * FROM users WHERE user_token="+token;
            rs = statement.executeQuery(query);
            if(rs.next()) {
                char user_online = rs.getString("user_online").toCharArray()[0];
                if (user_online == '1') {
                    int user_id = Integer.parseInt(rs.getString("user_id"));
                    String user_login = rs.getString("user_login");
                    String user_password = rs.getString("user_password");
                    String user_email = rs.getString("user_mail");
                    String user_nickname = rs.getString("user_nickname");
                    char user_flag = rs.getString("user_flag").toCharArray()[0];
                    char user_roles = rs.getString("user_roles").toCharArray()[0];

                    User us = new User();
                    us.setId(user_id);
                    us.setLogin(user_login);
                    us.setPassword(user_password);
                    us.setEmail(user_email);
                    us.setNickname(user_nickname);
                    us.setOnline('1');
                    us.setFlag(user_flag);
                    us.setRole(user_roles);
                    us.setSessionId(token);
                    user = us;
                }
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return user;
    }

    public void setUserSessionToken(int userId, int token) {
        try {
            String query = "UPDATE users SET user_token = " + token + " WHERE user_id = " + userId;
            rs = statement.executeQuery(query);
        }
        catch (SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
    }

    public void addUserToDB(int id, String login, String password, String email, String nickname) {
        try {
            String query = "SELECT user_login FROM users WHERE user_login='"+login+"' OR user_nickname='"+nickname+"'";
            rs = statement.executeQuery(query);
            if(!rs.next()) {
                String query_r = "INSERT INTO users (user_id, user_login, user_password, user_mail, user_nickname, user_online, user_flag, user_roles) VALUES (null, '"+login+"', '"+password+"', '"+email+"', '"+nickname+"' , '0', 'A', 'U')";
                rs = statement.executeQuery(query_r);
                System.out.println("Pomyslnie utworzono uzytkownika!");
            }
            else {
                System.out.println("Nie mozna utworzyc uzytkownika!");
            }
        }
        catch(Exception ex) {
            System.out.println("Error: "+ex);
        }
    }

    public boolean logoutUser(String nickname, int token) {
        try {
            String query = "UPDATE users SET user_online = '0', user_token = NULL WHERE user_token = " + token + " AND user_nickname = '" + nickname + "'";
            rs = statement.executeQuery(query);
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return false;
    }

    public int getNextGameId() {
        int id = 0;
        try {
            String query = "SELECT MAX(game_id) FROM games";
            rs = statement.executeQuery(query);
            if(rs.next()) {
                id = Integer.parseInt(rs.getString("MAX(game_id)"));
            }
        }
        catch(Exception ex) {
            System.out.println("Error: " + ex);
        }
        id = id + 1;
        return id;
    }

    public void addGame(int playerId1, int playerId2) {
        try {
            int game_id=this.getNextGameId();

            String query = "INSERT INTO games (game_id, player1_id, player2_id, game_tutorial, game_cdate) VALUES (null, "+playerId1+", "+playerId2+", 'n', TO_DATE('"+Date.valueOf(LocalDate.now())+"','yyyy-mm-dd'))";
            rs = statement.executeQuery(query);
            for(int i=0;i<12;i++)
            {
                query="INSERT INTO game_pawns VALUES (null, "+game_id+", "+playerId1+", 'y', 'n')";
                rs = statement.executeQuery(query);
            }
            for(int i=0;i<12;i++)
            {
                query="INSERT INTO game_pawns VALUES (null, "+game_id+", "+playerId2+", 'y', 'n')";
                rs = statement.executeQuery(query);
            }

            int id1=1,id2=13;
            query="SELECT MIN(game_pawn_id) FROM game_pawns WHERE game_id = "+game_id+" AND player_id = "+playerId1;
            rs=statement.executeQuery(query);
            if(rs.next()) id1=rs.getInt("MIN(game_pawn_id)");

            query="SELECT MIN(game_pawn_id) FROM game_pawns WHERE game_id = "+game_id+" AND player_id = "+playerId2;
            rs=statement.executeQuery(query);
            if(rs.next()) id2=rs.getInt("MIN(game_pawn_id)");

            query="INSERT INTO game_boards VALUES (null, "+game_id+", null, "+(id1)+", null, "+(id1+1)+", null, "+(id1+2)+", null, "+(id1+3)+", "+(id1+4)+", null, "+(id1+5)+", null, "+(id1+6)+", null, "+(id1+7)+", null, null, "+(id1+8)+", null, "+(id1+9)+", null, "+(id1+10)+", null, "+(id1+11)+", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "+(id2)+", null, "+(id2+1)+", null, "+(id2+2)+", null, "+(id2+3)+", null, null, "+(id2+4)+", null, "+(id2+5)+", null, "+(id2+6)+", null, "+(id2+7)+", "+(id2+8)+", null, "+(id2+9)+", null, "+(id2+10)+", null, "+(id2+11)+", null)";
            System.out.println(query);
            rs=statement.executeQuery(query);
        }
        catch(Exception ex) {
            System.out.println("Error: " + ex);
        }
    }

    public void deleteGame(int id) {
        try {
            String query = "DELETE FROM game_boards WHERE game_id="+id;
            rs = statement.executeQuery(query);
            query = "DELETE FROM game_pawns WHERE game_id="+id;
            rs = statement.executeQuery(query);
            query = "DELETE FROM games WHERE game_id="+id;
            rs = statement.executeQuery(query);
        }
        catch(Exception ex) {
            System.out.println("Error: " + ex);
        }
    }

    public Vector<User> getFriendsWithNickname(String nickname) {
        Vector<User> friends = new Vector<>();
        try {
            String query = "SELECT user_id FROM users WHERE user_nickname='"+nickname+"'";
            rs = statement.executeQuery(query);
            while(rs.next()) {
                int id = Integer.parseInt(rs.getString("user_id"));

                query = "SELECT friend_id, users_friends.user_id, user_nickname FROM users_friends LEFT JOIN users ON users.user_id = users_friends.friend_id WHERE users_friends.user_id=" + id;
                rs = statement.executeQuery(query);
                while (rs.next()) {
                    String nick = rs.getString("user_nickname");
                    id = Integer.parseInt(rs.getString("friend_id"));

                    User user = new User();
                    user.setNickname(nick);
                    user.setId(id);

                    friends.add(user);
                }
            }
        }
        catch(SQLException sqlEx) {
            System.out.println("Error: "+sqlEx);
            sqlEx.printStackTrace();
        }

        return friends;
    }

    public void addFriend(int pid1, int pid2) {
        try {
            String query = "INSERT INTO users_friends (user_id, friend_id, usr_frd_cdate) VALUES ("+pid1+", "+pid2+", TO_DATE('"+Date.valueOf(LocalDate.now())+"','yyyy-mm-dd'))";
            rs = statement.executeQuery(query);
        }
        catch(Exception ex) {
            System.out.println("Error: "+ex);
        }
    }

    public void deleteFriend(int pid1, int pid2) {
        try {
            String query = "DELETE FROM users_friends WHERE user_id="+pid1+" AND friend_id="+pid2;
            rs = statement.executeQuery(query);
        }
        catch(Exception ex) {
            System.out.println("Error: "+ex);
        }
    }

    public Vector<Friend> getFriends(int pid)
    {
        Vector<Friend> fr=new Vector<Friend>();
        String nick="";
        int friend_id;
        Friend friend;
        try{
            String query="SELECT f.friend_id, u.user_nickname FROM users_friends f, users u WHERE f.user_id="+pid+" AND u.user_id=f.friend_id";
            rs=statement.executeQuery(query);
            while(rs.next())
            {
                friend_id=Integer.parseInt(rs.getString("friend_id"));
                nick=rs.getString("user_nickname");
                friend=new Friend(friend_id,nick);
                fr.add(friend);
            }
        }
        catch(Exception ex)
        {
            System.out.println("Error: "+ex);
        }
        return fr;
    }

    // game
    public Game getGameById(int id) {
        Game game = null;
        Board board = null;
        try {
            String query = "SELECT * FROM games WHERE game_id="+id;
            rs = statement.executeQuery(query);
            if(rs.next()) {
                int game_id = Integer.parseInt(rs.getString("game_id"));
                int playerId1 = Integer.parseInt(rs.getString("player1_id"));
                int playerId2 = Integer.parseInt(rs.getString("player2_id"));
                Boolean ongoing = Boolean.getBoolean(rs.getString("ongoing"));

                Game ga = new Game(game_id, playerId1, playerId2);
                //ga.setBoard(board);
                game = ga;
            }

            query = "SELECT * FROM game_boards WHERE game_id="+id;
            rs = statement.executeQuery(query);
            if(rs.next()) {
                int game_id = Integer.parseInt(rs.getString("game_id"));
                int [][] piecePlacement = new int[8][8];
                piecePlacement[0][0] = Integer.parseInt(rs.getString("a1"));
                piecePlacement[0][1] = Integer.parseInt(rs.getString("a2"));
                piecePlacement[0][2] = Integer.parseInt(rs.getString("a3"));
                piecePlacement[0][3] = Integer.parseInt(rs.getString("a4"));
                piecePlacement[0][4] = Integer.parseInt(rs.getString("a5"));
                piecePlacement[0][5] = Integer.parseInt(rs.getString("a6"));
                piecePlacement[0][6] = Integer.parseInt(rs.getString("a7"));
                piecePlacement[0][7] = Integer.parseInt(rs.getString("a8"));

                piecePlacement[1][0] = Integer.parseInt(rs.getString("b1"));
                piecePlacement[1][1] = Integer.parseInt(rs.getString("b2"));
                piecePlacement[1][2] = Integer.parseInt(rs.getString("b3"));
                piecePlacement[1][3] = Integer.parseInt(rs.getString("b4"));
                piecePlacement[1][4] = Integer.parseInt(rs.getString("b5"));
                piecePlacement[1][5] = Integer.parseInt(rs.getString("b6"));
                piecePlacement[1][6] = Integer.parseInt(rs.getString("b7"));
                piecePlacement[1][7] = Integer.parseInt(rs.getString("b8"));

                piecePlacement[2][0] = Integer.parseInt(rs.getString("c1"));
                piecePlacement[2][1] = Integer.parseInt(rs.getString("c2"));
                piecePlacement[2][2] = Integer.parseInt(rs.getString("c3"));
                piecePlacement[2][3] = Integer.parseInt(rs.getString("c4"));
                piecePlacement[2][4] = Integer.parseInt(rs.getString("c5"));
                piecePlacement[2][5] = Integer.parseInt(rs.getString("c6"));
                piecePlacement[2][6] = Integer.parseInt(rs.getString("c7"));
                piecePlacement[2][7] = Integer.parseInt(rs.getString("c8"));

                piecePlacement[3][0] = Integer.parseInt(rs.getString("d1"));
                piecePlacement[3][1] = Integer.parseInt(rs.getString("d2"));
                piecePlacement[3][2] = Integer.parseInt(rs.getString("d3"));
                piecePlacement[3][3] = Integer.parseInt(rs.getString("d4"));
                piecePlacement[3][4] = Integer.parseInt(rs.getString("d5"));
                piecePlacement[3][5] = Integer.parseInt(rs.getString("d6"));
                piecePlacement[3][6] = Integer.parseInt(rs.getString("d7"));
                piecePlacement[3][7] = Integer.parseInt(rs.getString("d8"));

                piecePlacement[4][0] = Integer.parseInt(rs.getString("e1"));
                piecePlacement[4][1] = Integer.parseInt(rs.getString("e2"));
                piecePlacement[4][2] = Integer.parseInt(rs.getString("e3"));
                piecePlacement[4][3] = Integer.parseInt(rs.getString("e4"));
                piecePlacement[4][4] = Integer.parseInt(rs.getString("e5"));
                piecePlacement[4][5] = Integer.parseInt(rs.getString("e6"));
                piecePlacement[4][6] = Integer.parseInt(rs.getString("e7"));
                piecePlacement[4][7] = Integer.parseInt(rs.getString("e8"));

                piecePlacement[5][0] = Integer.parseInt(rs.getString("f1"));
                piecePlacement[5][1] = Integer.parseInt(rs.getString("f2"));
                piecePlacement[5][2] = Integer.parseInt(rs.getString("f3"));
                piecePlacement[5][3] = Integer.parseInt(rs.getString("f4"));
                piecePlacement[5][4] = Integer.parseInt(rs.getString("f5"));
                piecePlacement[5][5] = Integer.parseInt(rs.getString("f6"));
                piecePlacement[5][6] = Integer.parseInt(rs.getString("f7"));
                piecePlacement[5][7] = Integer.parseInt(rs.getString("f8"));

                piecePlacement[6][0] = Integer.parseInt(rs.getString("g1"));
                piecePlacement[6][1] = Integer.parseInt(rs.getString("g2"));
                piecePlacement[6][2] = Integer.parseInt(rs.getString("g3"));
                piecePlacement[6][3] = Integer.parseInt(rs.getString("g4"));
                piecePlacement[6][4] = Integer.parseInt(rs.getString("g5"));
                piecePlacement[6][5] = Integer.parseInt(rs.getString("g6"));
                piecePlacement[6][6] = Integer.parseInt(rs.getString("g7"));
                piecePlacement[6][7] = Integer.parseInt(rs.getString("g8"));

                piecePlacement[7][0] = Integer.parseInt(rs.getString("h1"));
                piecePlacement[7][1] = Integer.parseInt(rs.getString("h2"));
                piecePlacement[7][2] = Integer.parseInt(rs.getString("h3"));
                piecePlacement[7][3] = Integer.parseInt(rs.getString("h4"));
                piecePlacement[7][4] = Integer.parseInt(rs.getString("h5"));
                piecePlacement[7][5] = Integer.parseInt(rs.getString("h6"));
                piecePlacement[7][6] = Integer.parseInt(rs.getString("h7"));
                piecePlacement[7][7] = Integer.parseInt(rs.getString("h8"));

                int playerid;
                String king;
                Board bo = new Board();
                //query = "SELECT * FROM game_pawns WHERE game_id="+id;
                //rs = statement.executeQuery(query);
                for(int i=0;i<8;i++){
                    for(int j=0;j<8;j++){
                        if(piecePlacement[i][j]!=0) {
                            query = "SELECT player_id, game_pawn_king FROM game_pawns WHERE game_pawn_id=" + piecePlacement[i][j];
                            rs = statement.executeQuery(query);
                            playerid = Integer.parseInt(rs.getString("player_id"));
                            king = (rs.getString("game_pawn_king"));
                            if(playerid==game.getPlayersId()[0]){
                                if(king.equals("n")){
                                    piecePlacement[i][j]=1;
                                }else{
                                    piecePlacement[i][j]=3;
                                }
                            }else{
                                if(king.equals("n")){
                                    piecePlacement[i][j]=2;
                                }else{
                                    piecePlacement[i][j]=4;
                                }
                            }
                        }
                    }
                }

                bo.setPiecePlacement(piecePlacement);
                board = bo;
                game.setBoard(board);
            }
        }
        catch(SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }

        return game;
    }

    public Boolean checkDataRegistration(String login, String nickname, String email) {
        Boolean result = false;
        try {
            String query = "SELECT user_login, user_nickname, user_mail FROM users WHERE user_login='"+ login + "' OR user_nickname='" + nickname + "' OR user_mail='" + email + "'";
            rs = statement.executeQuery(query);
            if(rs.next()) {
                result = true;
            }
            else {
                result = false;
            }
        }
        catch(SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return result;
    }

    public int getNextUserId() {
        int id = 0;
        try {
            String query = "SELECT MAX(user_id) FROM users";
            rs = statement.executeQuery(query);
            if(rs.next()) {
                id = Integer.parseInt(rs.getString("MAX(user_id)"));
            }
        }
        catch(SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        id = id + 1;
        return id;
    }

    public int getUserId(String nickname) {
        int id = 0;
        try {
            String query = "SELECT user_id FROM users WHERE user_nickname='" + nickname + "'";
            rs = statement.executeQuery(query);
            if(rs.next()) {
                id = Integer.parseInt(rs.getString("user_id"));
            }
        }
        catch(SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return id;
    }

    public String getUserNickname(int id) {
        String nickname = "";
        try {
            String query = "SELECT user_nickname FROM users WHERE user_id=" + id;
            rs = statement.executeQuery(query);
            if(rs.next()) {
                nickname = rs.getString("user_nickname");
            }
        }
        catch(SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return nickname;
    }

    public Vector<User> getAllUsers() {
        Vector<User> users = new Vector<>();
        try {
            String query = "SELECT * FROM users";
            rs = statement.executeQuery(query);
            while(rs.next()) {
                int user_id = Integer.parseInt(rs.getString("user_id"));
                char user_online = rs.getString("user_online").toCharArray()[0];
                String user_login = rs.getString("user_login");
                String user_password = rs.getString("user_password");
                String user_email = rs.getString("user_mail");
                String user_nickname = rs.getString("user_nickname");
                char user_flag = rs.getString("user_flag").toCharArray()[0];
                char user_roles = rs.getString("user_roles").toCharArray()[0];

                User us = new User();
                us.setId(user_id);
                us.setLogin(user_login);
                us.setPassword(user_password);
                us.setEmail(user_email);
                us.setNickname(user_nickname);
                us.setFlag(user_flag);
                us.setRole(user_roles);
                users.add(us);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: "+ex.getSQLState());
            ex.printStackTrace();
        }
        return users;
    }
}
