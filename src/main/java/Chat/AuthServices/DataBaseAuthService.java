package Chat.AuthServices;

import java.sql.*;

public class DataBaseAuthService implements AuthService {

    private Connection connection;
    private Statement statement;
    private static final String SELECT_USERNAME_BY_LOGIN_AND_PASSWORD = "select username from chatUsers where login=? and password =?;";
    private static final String SELECT_USERNAME_BY_USERNAME = "select username from chatUsers where username=?;";
    private static final String SELECT_LOGIN_BY_LOGIN = "select login from chatUsers where login=?;";
    private static final String registration = "insert into chatUsers (username,login,password) values (?,?,?);";
    private static final String updatePassword = "update chatUsers set password = ? where login = ?;";
    private static final String updateUsername = "update chatUsers set username = ? where login = ? and password = ?;";

    public DataBaseAuthService() {
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
        statement = connection.createStatement();
        statement.execute("create table if not exists chatUsers (username text primary key not null, login text not null, password text not null);");
    }


    @Override
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

    @Override
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

    @Override
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


    @Override
    public void changeUsername(String newUsername, String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(updateUsername)) {
            ps.setString(1, newUsername);
            ps.setString(2, login);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void changePassword(String login, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(updatePassword)) {
            ps.setString(1, newPassword);
            ps.setString(2, login);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public void registration(String username, String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(registration)) {
            ps.setString(1, username);
            ps.setString(2, login);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
