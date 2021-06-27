package Chat.Client.ControllerServices;

import Chat.Client.ChatController;
import Chat.Client.ControllerServices.Interfaces.ChatService;
import Chat.Common.PropClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Network {

    private final Socket socket;
    private ChatService chatService;
    private DataInputStream in;
    private DataOutputStream out;
    private static final Logger LOGGER = (Logger) LogManager.getLogger(Network.class);
    private ChatController chatController;

    public Network(ChatService chatService) throws IOException {
        this.socket = new Socket(PropClass.properties("host"), Integer.parseInt(PropClass.properties("port")));
        this.chatService = chatService;
        in = new DataInputStream(this.socket.getInputStream());
        out = new DataOutputStream(this.socket.getOutputStream());
    }

    public void readMessage() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    chatService.receive(in.readUTF());
                } catch (EOFException | SocketException e) {
                    LOGGER.error(e.getMessage());
                    break;
                }
                catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(String messageForSend) {
        try {
            out.writeUTF(messageForSend);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
