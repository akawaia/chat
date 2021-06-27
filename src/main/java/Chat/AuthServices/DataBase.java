package Chat.AuthServices;

import java.sql.*;

public class DataBase {

    private Connection connection;
    private static final String SELECT_USERNAME_BY_LOGIN_AND_PASSWORD = "select username from chatUsers where login=? and password =?;";
    private static final String SELECT_USERNAME_BY_USERNAME = "select username from chatUsers where username=?;";
    private static final String SELECT_LOGIN_BY_LOGIN = "select login from chatUsers where login=?;";
    private static final String REGISTRATION = "insert into chatUsers (username,login,password) values (?,?,?);";
    private static final String UPDATE_PASSWORD = "update chatUsers set password = ? where login = ?;";
    private static final String UPDATE_USERNAME = "update chatUsers set username = ? where login = ? and password = ?;";

    public DataBase() {
        try {
            connect();
            createTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:chatUser.db");
    }

    private void createTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("create table if not exists chatUsers (username text primary key not null, login text not null, password text not null);");
    }



    public String getUsernameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USERNAME_BY_LOGIN_AND_PASSWORD)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) return resultSet.getString("username");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "";
    }


    public String getUsernameByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USERNAME_BY_USERNAME)) {
            ps.setString(1, username);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) return "";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return username;
    }


    public String getLoginByLogin(String login) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_LOGIN_BY_LOGIN)) {
            ps.setString(1, login);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) return "";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return login;
    }



    public void changeUsername(String newUsername, String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_USERNAME)) {
            ps.setString(1, newUsername);
            ps.setString(2, login);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public void changePassword(String login, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, newPassword);
            ps.setString(2, login);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    public void registration(String username, String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(REGISTRATION)) {
            ps.setString(1, username);
            ps.setString(2, login);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
