package Chat.AuthServices;

public class DataBaseAuthService implements AuthService {

    private final DataBase dataBase;


    public DataBaseAuthService() {
        dataBase = new DataBase();
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String Password) {
        return dataBase.getUsernameByLoginAndPassword(login, Password);
    }

    @Override
    public void changeUsername(String newUsername, String login, String Password) {
        dataBase.changeUsername(newUsername, login, Password);
    }

    @Override
    public void changePassword(String login, String newPassword) {
        dataBase.changePassword(login, newPassword);
    }

    @Override
    public void registration(String username, String login, String password) {
        dataBase.registration(username, login, password);
    }

    @Override
    public String getUsernameByUsername(String username) {
        return dataBase.getUsernameByUsername(username);
    }

    @Override
    public String getLoginByLogin(String login) {
        return dataBase.getLoginByLogin(login);
    }
}
