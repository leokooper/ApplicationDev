package ru.jfxchat.chat.core.server;

import java.sql.*;

public class AuthService {
        private Connection connection;
        private Statement stmt;
        private PreparedStatement psFindNick;
        private PreparedStatement psUserRegister;

    public void connect() throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
        checkTable();
        psFindNick = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
        psUserRegister = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?,?,?);");
        //userRegistration("login4", "pass4", "nick4");
        testUsers();
    }

    private void checkTable() throws SQLException{
        stmt.execute("CREATE TABLE IF NOT EXISTS users (\n" +
                "   id          INTEGER     PRIMARY KEY     AUTOINCREMENT,\n" +
                "   login       TEXT        UNIQUE,\n" +
                "   password    INTEGER,\n"    +
                "   nick        TEXT        UNIQUE\n" +
                ");");
    }

    public void testUsers() throws SQLException {
        stmt.execute("DELETE FROM users");
        for (int i = 1; i <= 20; i++) {
            userRegistration("login" + i, "password" + i, "nick" + i);
        }
    }

    public boolean userRegistration (String login, String password, String nick) throws SQLException{

        try {
            int passHash = password.hashCode();
            psUserRegister.setString(1, login);
            psUserRegister.setInt(2, passHash);
            psUserRegister.setString(3, nick);
            return psUserRegister.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new SQLException("Ошибка регистрации пользователя");
        }
    }

    public String getNickByLoginAndPass(String login, String pass) {
        try {
            //ResultSet rs = stmt.executeQuery("SELECT nick FROM users WHERE login = '" + login + "' AND password = '" + pass + "';");
            psFindNick.setString(1, login);
            int passHash = pass.hashCode();
            psFindNick.setInt(2, passHash);
            ResultSet rs = psFindNick.executeQuery();
            if (rs.next()){
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect(){
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
