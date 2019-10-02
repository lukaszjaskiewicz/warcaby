package core;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Vector;

public class Servlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        // Odczyt zawartości żądania
        String data = readRequestBody(req);

        // Konwersja na obiekt JSON
        if (data != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(data));
            JsonObject json = jsonReader.readObject();

            jsonReader.close();

            // logowanie loginem i hasłem
            if ((data.contains("sign") && json.getBoolean("sign")) && !data.contains("token")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = loginViaPassword(jsonUser);
                sendResponse(res, jsonResp);
            }
            // logowanie na podstawie aktywnej sesji
            else if ((data.contains("sign") && json.getBoolean("sign")) && data.contains("token")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = loginViaSessionToken(jsonUser);
                sendResponse(res, jsonResp);
            }
            // wylogowanie
            else if (data.contains("sign") && !json.getBoolean("sign")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = logout(jsonUser);
                sendResponse(res, jsonResp);
            }
            //znajomi
            else if(data.contains("friends") && json.getBoolean("friends")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = getFriendsList(jsonUser);
                sendResponse(res, jsonResp);
            }
            //dodawanie nowego znajomego
            else if(data.contains("addFriend") && json.getBoolean("addFriend")) {
                JsonObject jsonUser = json.getJsonObject("user");
                JsonObject jsonFriend = json.getJsonObject("friendToAdd");
                String jsonResp = addFriend(jsonUser, jsonFriend);
                sendResponse(res, jsonResp);
            }
            //usuwanie znajomego
            else if(data.contains("deleteFriend") && json.getBoolean("deleteFriend")) {
                JsonObject jsonUser = json.getJsonObject("user");
                JsonObject jsonFriend = json.getJsonObject("friendToDelete");
                String jsonResp = deleteFriend(jsonUser, jsonFriend);
                sendResponse(res, jsonResp);
            }
            // plansza
            else if (data.contains("board") && json.getBoolean("board")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = updateBoard(jsonUser);
                sendResponse(res, jsonResp);
            }
            //rejestracja
            else if(data.contains("newUser") && json.getBoolean("register")) {
                JsonObject jsonUser = json.getJsonObject("newUser");
                String jsonResp = registerUser(jsonUser);
                sendResponse(res, jsonResp);
            }
            // nowa gra
            else if (data.contains("newGame") && json.getBoolean("newGame")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = newGame(jsonUser);
                sendResponse(res, jsonResp);

            }
            //"Czy przydzielono mi jakas gre?"
            else if(data.contains("checkGame") && json.getBoolean("checkGame")) {
                JsonObject jsonUser = json.getJsonObject("user");
                String jsonResp = checkGame(jsonUser);
                sendResponse(res, jsonResp);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{

    }

    // logowanie loginem i hasłem
    public String loginViaPassword(JsonObject jsonUser) {
        System.out.println("Żądanie zalogowania dla loginu: " + jsonUser.getString("login"));

        UserManagement um = new UserManagement();

        User loggedUser = um.login(jsonUser.getString("login"), jsonUser.getString("password"));

        String jsonResp;
        if (loggedUser != null){

            // Zbudowanie JSON'a zwrotnego
            jsonResp = Json.createObjectBuilder()
                    .add("signedUser", Json.createObjectBuilder()
                            .add("nickname", loggedUser.getNickname())
                            .add("email", loggedUser.getEmail())
                            .add("role", String.valueOf(loggedUser.getRole()))
                            .add("token", loggedUser.getSessionId()))
                    .add("signed", true)
                    .build()
                    .toString();

            System.out.println("Zalogowano i wysłano odpowiedź");

            return jsonResp;
        }
        else {

            jsonResp = Json.createObjectBuilder()
                    .add("login", jsonUser.getString("login"))
                    .add("signed", false)
                    .build()
                    .toString();

            System.out.println("Niezalogowano i wysłano odpowiedź");

            return jsonResp;
        }
    }

    // logowanie na podstawie istniejącej sesji
    public String loginViaSessionToken(JsonObject jsonUser) {
        System.out.println("Żądanie zalogowania dla tokenu: " + jsonUser.getInt("token") + " (użytkownik - " + jsonUser.getString("nickname") + ")");

        UserManagement um = new UserManagement();

        User loggedUser = um.loginToken(jsonUser.getInt("token"));

        if (loggedUser != null) {

            String jsonResp = Json.createObjectBuilder()
                    .add("signedUser", Json.createObjectBuilder()
                            .add("nickname", loggedUser.getNickname())
                            .add("email", loggedUser.getEmail())
                            .add("role", String.valueOf(loggedUser.getRole()))
                            .add("token", loggedUser.getSessionId()))
                    .add("signed", true)
                    .build()
                    .toString();

            System.out.println("Zalogowano na podstawie aktywnej sesji i wysłano odpowiedź");

            return jsonResp;
        }
        else {

            String jsonResp = Json.createObjectBuilder()
                    .add("login", jsonUser.getString("nickname"))
                    .add("signed", false)
                    .build()
                    .toString();

            System.out.println("Nie zalogowano na podstawie aktywnej sesji i wysłano odpowiedź");

            return jsonResp;
        }
    }

    // wylogowanie
    public String logout(JsonObject jsonUser) {
        System.out.println("Żądanie wylogowania dla tokenu: " + jsonUser.getInt("token") + " (użytkownik - " + jsonUser.getString("nickname") + ")");

        UserManagement um = new UserManagement();

        if (um.logout(jsonUser.getString("nickname"), jsonUser.getInt("token"))) {

            String jsonResp = Json.createObjectBuilder()
                    .add("signedOut", true)
                    .build()
                    .toString();

            System.out.println("Pomyślnie wylogowano użytkownika");

            return jsonResp;
        }
        else {

            String jsonResp = Json.createObjectBuilder()
                    .add("signedOut", false)
                    .build()
                    .toString();

            System.out.println("Nie udało się wylogować użytkownika");

            return jsonResp;
        }
    }

    //obsluga listy znajomych
    public String getFriendsList(JsonObject jsonUser) {
        System.out.println("Żądanie wyswietlenia listy znajomych dla tokenu: " + jsonUser.getInt("token") + " (użytkownik - " + jsonUser.getString("nickname") + ")");

        String nickname = jsonUser.getString("nickname");
        int token = jsonUser.getInt("token");
        UserManagement um = new UserManagement();

        Vector<User> friends = um.getFriends(nickname, token);

        JsonArrayBuilder groupArrayFriends = Json.createArrayBuilder();
        for(User user : friends) {
            int id = user.getId();
            String nick = user.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayFriends.add(jsonObj);
        }

        DBConnection db = new DBConnection();
        Vector<User> users = db.getAllUsers();

        JsonArrayBuilder groupArrayUsers = Json.createArrayBuilder();
        for(User user : users) {
            int id = user.getId();
            String nick = user.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayUsers.add(jsonObj);
        }

        String jsonResp = Json.createObjectBuilder()
                .add("friends", groupArrayFriends)
                .add("players", groupArrayUsers)
                .build()
                .toString();

        return jsonResp;
    }
    // tworzenie gry
    public String newGame(JsonObject jsonUser){
        System.out.println("Tworzenie nowej gry");

        Management m = Management.getInstance();

        User player=null;
        for(User u : m.users){
            if(u.getSessionId()==jsonUser.getInt("token")){
                player=u;
                break;
            }
        }
        m.searchingForGame.add(player);

        User opponent=null;
        int game_id=0;

        for(User u : m.searchingForGame){
            if(u.getId()!=player.getId()){
                m.searchingForGame.remove(u);
                m.searchingForGame.remove(player);
                m.newGame(player.getId(), u.getId());
                m.usersInGame.add(player.getId());
                m.usersInGame.add(u.getId());
                opponent=u;

                break;
            }
        }
        String jsonResp;
        if(opponent!=null) {
            jsonResp = Json.createObjectBuilder()
                    .add("user", Json.createObjectBuilder()
                        .add("nickname", player.getNickname())
                        .add("token", player.getSessionId())
                    )
                    .add("newGame", true)
                    .add("gameId", game_id)
                    .add("opponent", opponent.getNickname())
                    .build()
                    .toString();
        }else{
            jsonResp = Json.createObjectBuilder()
                    .add("user", Json.createObjectBuilder()
                        .add("nickname", player.getNickname())
                        .add("token", player.getSessionId())
                    )
                    .add("newGame", false)
                    .add("gameId", 0)
                    .add("opponent", "Non")
                    .build()
                    .toString();
        }

        return jsonResp;
    }

    // aktualizajca planszy
    public String updateBoard(JsonObject jsonUser){
        System.out.println("Żądanie aktualizacji planszy");                         // dla gracza: " + jsonUser.getInt("id"));

        Management m = Management.getInstance();

        Game game = m.getGameById(jsonUser.getInt("game_id"));
        Board board = game.getBoard();

        if(game!=null) {
            String jsonResp = Json.createObjectBuilder()
                    .add("game", game.getId())
                    .add("player1", game.getPlayersId()[0])
                    .add("player2", game.getPlayersId()[1])
                    .add("board", Json.createObjectBuilder()
                            .add("A1", board.getPiecePlacement()[0][0])
                            .add("A2", board.getPiecePlacement()[0][1])
                            .add("A3", board.getPiecePlacement()[0][2])
                            .add("A4", board.getPiecePlacement()[0][3])
                            .add("A5", board.getPiecePlacement()[0][4])
                            .add("A6", board.getPiecePlacement()[0][5])
                            .add("A7", board.getPiecePlacement()[0][6])
                            .add("A8", board.getPiecePlacement()[0][7])

                            .add("B1", board.getPiecePlacement()[1][0])
                            .add("B2", board.getPiecePlacement()[1][1])
                            .add("B3", board.getPiecePlacement()[1][2])
                            .add("B4", board.getPiecePlacement()[1][3])
                            .add("B5", board.getPiecePlacement()[1][4])
                            .add("B6", board.getPiecePlacement()[1][5])
                            .add("B7", board.getPiecePlacement()[1][6])
                            .add("B8", board.getPiecePlacement()[1][7])

                            .add("C1", board.getPiecePlacement()[2][0])
                            .add("C2", board.getPiecePlacement()[2][1])
                            .add("C3", board.getPiecePlacement()[2][2])
                            .add("C4", board.getPiecePlacement()[2][3])
                            .add("C5", board.getPiecePlacement()[2][4])
                            .add("C6", board.getPiecePlacement()[2][5])
                            .add("C7", board.getPiecePlacement()[2][6])
                            .add("C8", board.getPiecePlacement()[2][7])

                            .add("D1", board.getPiecePlacement()[3][0])
                            .add("D2", board.getPiecePlacement()[3][1])
                            .add("D3", board.getPiecePlacement()[3][2])
                            .add("D4", board.getPiecePlacement()[3][3])
                            .add("D5", board.getPiecePlacement()[3][4])
                            .add("D6", board.getPiecePlacement()[3][5])
                            .add("D7", board.getPiecePlacement()[3][6])
                            .add("D8", board.getPiecePlacement()[3][7])

                            .add("E1", board.getPiecePlacement()[4][0])
                            .add("E2", board.getPiecePlacement()[4][1])
                            .add("E3", board.getPiecePlacement()[4][2])
                            .add("E4", board.getPiecePlacement()[4][3])
                            .add("E5", board.getPiecePlacement()[4][4])
                            .add("E6", board.getPiecePlacement()[4][5])
                            .add("E7", board.getPiecePlacement()[4][6])
                            .add("E8", board.getPiecePlacement()[4][7])

                            .add("F1", board.getPiecePlacement()[5][0])
                            .add("F2", board.getPiecePlacement()[5][1])
                            .add("F3", board.getPiecePlacement()[5][2])
                            .add("F4", board.getPiecePlacement()[5][3])
                            .add("F5", board.getPiecePlacement()[5][4])
                            .add("F6", board.getPiecePlacement()[5][5])
                            .add("F7", board.getPiecePlacement()[5][6])
                            .add("F8", board.getPiecePlacement()[5][7])

                            .add("G1", board.getPiecePlacement()[6][0])
                            .add("G2", board.getPiecePlacement()[6][1])
                            .add("G3", board.getPiecePlacement()[6][2])
                            .add("G4", board.getPiecePlacement()[6][3])
                            .add("G5", board.getPiecePlacement()[6][4])
                            .add("G6", board.getPiecePlacement()[6][5])
                            .add("G7", board.getPiecePlacement()[6][6])
                            .add("G8", board.getPiecePlacement()[6][7])

                            .add("H1", board.getPiecePlacement()[7][0])
                            .add("H2", board.getPiecePlacement()[7][1])
                            .add("H3", board.getPiecePlacement()[7][2])
                            .add("H4", board.getPiecePlacement()[7][3])
                            .add("H5", board.getPiecePlacement()[7][4])
                            .add("H6", board.getPiecePlacement()[7][5])
                            .add("H7", board.getPiecePlacement()[7][6])
                            .add("H8", board.getPiecePlacement()[7][7])
                    )
                    .build()
                    .toString();
            System.out.println("Pomyslnie wyslano plansze!");
            return jsonResp;
        }
        else{
            String jsonResp = Json.createObjectBuilder()
                    .add("game", game.getId())
                    .add("player1", game.getPlayersId()[0])
                    .add("player2", game.getPlayersId()[1])
                    .add("board", false)
                    .build()
                    .toString();
            System.out.println("Nie udało się wyslac planszy!");
            return jsonResp;
        }
    }

    //rejestracja
    public String registerUser(JsonObject jsonUser) {
        System.out.println("Żądanie rejestracji uzytkownika o nazwie: " + jsonUser.getString("nickname"));

        String login = jsonUser.getString("login");
        String passwd = jsonUser.getString("password");
        String nick = jsonUser.getString("nickname");
        String email = jsonUser.getString("email");

        UserManagement um = new UserManagement();

        DBConnection db = new DBConnection();
        if(db.checkDataRegistration(login, nick, email)) {
            String jsonResp = Json.createObjectBuilder()
                    .add("user", Json.createObjectBuilder()
                        .add("login", login)
                        .add("password", passwd)
                        .add("email", email))
                    .add("register", false)
                    .build()
                    .toString();

            System.out.println("Blad rejestracji uzytkownika o loginie: " + login);
            return jsonResp;
        }
        else {
            um.registration(db.getNextUserId(), login, passwd, email, nick);
            User user = um.login(login, passwd);

            String jsonResp = Json.createObjectBuilder()
                    .add("user", Json.createObjectBuilder()
                        .add("nickname", nick)
                        .add("token", user.getSessionId())
                        .add("email", email)
                        .add("role", String.valueOf(user.getRole())))
                    .add("register", true)
                    .build()
                    .toString();

            System.out.println("Zarejestrowano uzytkownika o loginie: " + login);
            return jsonResp;
        }
    }

    //dodawanie znajomych
    public String addFriend(JsonObject jsonUser, JsonObject jsonFriend) {
        System.out.println("Żądanie dodania znajomego o nazwie: " + jsonFriend.getString("nickname") + "dla uzytkownika o nazwie: " + jsonUser.getString("nickname"));

        String userNick = jsonUser.getString("nickname");
        int userToken = jsonUser.getInt("token");
        int friendId = jsonFriend.getInt("id");
        String friendNick = jsonFriend.getString("nickname");

        DBConnection db = new DBConnection();
        int userId = db.getUserId(userNick);
        User user = new User();
        user.setId(userId);

        user.addFriend(friendId, friendNick);

        UserManagement um = new UserManagement();

        Vector<User> friends = um.getFriends(userNick, userToken);

        JsonArrayBuilder groupArrayFriends = Json.createArrayBuilder();
        for(User u : friends) {
            int id = u.getId();
            String nick = u.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayFriends.add(jsonObj);
        }

        Vector<User> users = db.getAllUsers();

        JsonArrayBuilder groupArrayUsers = Json.createArrayBuilder();
        for(User u : users) {
            int id = u.getId();
            String nick = u.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayUsers.add(jsonObj);
        }

        String jsonResp = Json.createObjectBuilder()
                .add("friends", groupArrayFriends)
                .add("players", groupArrayUsers)
                .add("addFriend", true)
                .build()
                .toString();

        return jsonResp;
    }

    //usuwanie znajomych
    public String deleteFriend(JsonObject jsonUser, JsonObject jsonFriend) {
        System.out.println("Żądanie usuniecia znajomego o id: " + jsonFriend.getInt("id") + "dla uzytkownika o nazwie: " + jsonUser.getString("nickname"));

        String userNick = jsonUser.getString("nickname");
        int userToken = jsonUser.getInt("token");
        int friendId = jsonFriend.getInt("id");

        DBConnection db = new DBConnection();
        int userId = db.getUserId(userNick);
        User user = new User();
        user.setId(userId);

        String friendNick = db.getUserNickname(friendId);
        user.deleteOneFriend(friendNick);

        UserManagement um = new UserManagement();

        Vector<User> friends = um.getFriends(userNick, userToken);

        JsonArrayBuilder groupArrayFriends = Json.createArrayBuilder();
        for(User u : friends) {
            int id = u.getId();
            String nick = u.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayFriends.add(jsonObj);
        }

        Vector<User> users = db.getAllUsers();

        JsonArrayBuilder groupArrayUsers = Json.createArrayBuilder();
        for(User u : users) {
            int id = u.getId();
            String nick = u.getNickname();

            JsonObjectBuilder jsonObj = Json.createObjectBuilder()
                    .add("id", id)
                    .add("nickname", nick);
            groupArrayUsers.add(jsonObj);
        }

        String jsonResp = Json.createObjectBuilder()
                .add("friends", groupArrayFriends)
                .add("players", groupArrayUsers)
                .add("deleteFriend", true)
                .build()
                .toString();

        return jsonResp;
    }

    //"Czy przydzielono mi jakas gre?" (oczekiwanie na liste otwartych gier wraz z lista graczy)
    public String checkGame(JsonObject jsonUser) {
        System.out.println("Żądanie sprawdzenia czy ID klienta znajduje się w liście playersId, w ktorejkolwiek z otwartych gier dla gracza: " + jsonUser.getInt("nickname"));

        String nickname = jsonUser.getString("nickname");
        int token = jsonUser.getInt("token");

        DBConnection db = new DBConnection();
        int idUser = db.getUserId("nickname");

        Management m = Management.getInstance();
        Vector<Game> games = m.games;
        Vector<Integer> playersId = m.usersInGame;

        Boolean check = false;
        for(Integer i : playersId) {
            if(i == idUser) {
                check = true;
                break;
            }
        }

        int idGame = -1;
        for(Game g : games) {
            if(g.getPlayersId()[0] == idUser || g.getPlayersId()[1] == idUser) {
                idGame = g.getId();
                break;
            }
        }

        if(check) {
            String jsonResp = Json.createObjectBuilder()
                    .add("user", Json.createObjectBuilder()
                        .add("nickname", nickname)
                        .add("token", token)
                        .add("id", idUser))
                    .add("game", Json.createObjectBuilder()
                        .add("id", idGame))
                    .add("checkGame", true)
                    .build()
                    .toString();
            return jsonResp;
        }
        else {
            String jsonResp = Json.createObjectBuilder()
                    .add("users", Json.createObjectBuilder()
                            .add("nickname", nickname)
                            .add("token", token))
                    .add("ckeckGame", false)
                    .build()
                    .toString();
            return jsonResp;
        }
    }

    // odczytanie ciała żądania
    public String readRequestBody(HttpServletRequest req) {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        try {

            reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);

            String data = buffer.toString();

            return data;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // wysłanie jsona w stringu jako odpowiedź
    public void sendResponse(HttpServletResponse res, String jsonResp) {
        PrintWriter out = null;
        try {
            out = res.getWriter();
            res.setCharacterEncoding("UTF-8");
            res.setContentType("application/json");
            out.print(jsonResp);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
