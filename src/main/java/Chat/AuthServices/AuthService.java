package Chat.AuthServices;

public interface AuthService {
    String getUsernameByLoginAndPassword(String login, String Password);
    void changeUsername(String newUsername, String login, String Password);
    void changePassword(String login, String newPassword);
    void registration (String username, String login, String password);
    String getUsernameByUsername(String username);
    String getLoginByLogin(String login);

}
