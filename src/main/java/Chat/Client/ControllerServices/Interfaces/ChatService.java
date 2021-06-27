package Chat.Client.ControllerServices.Interfaces;

public interface ChatService {

    void send(String message);

    void receive(String receiveMessage);

    void connect();

    boolean isConnected();
}
