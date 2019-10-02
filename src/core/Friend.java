package core;

public class Friend {
    private int id;
    private String nick;

    public Friend() {}

    public Friend(int id, String nick) {
        this.id = id;
        this.nick = nick;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }
}
