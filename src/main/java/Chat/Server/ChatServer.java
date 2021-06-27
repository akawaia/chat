package Chat.Server;

import Chat.AuthServices.AuthService;
import Chat.AuthServices.DataBase;
import Chat.AuthServices.DataBaseAuthService;
import Chat.Common.ChatMessage;
import Chat.Common.MessageType;
import Chat.Common.PropClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> onlineUsersList;
    private final AuthService authService;
    private static final Logger LOGGER = (Logger) LogManager.getLogger(ChatServer.class);

    public ChatServer() {
        this.onlineUsersList = new ArrayList<>();
        this.authService = new DataBaseAuthService();

    }

    public void startChatServer() {

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(PropClass.properties("port")))) {
            LOGGER.info("Server started");
            while (true) {
                Socket socket = serverSocket.accept();
                LOGGER.info("Client connected");
                new ClientHandler(socket, this).startHandler();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribed(ClientHandler clientHandler) {
        onlineUsersList.add(clientHandler);
        sendOnlineUserList();
    }


    public synchronized void unsubscribed(ClientHandler clientHandler) {
        onlineUsersList.remove(clientHandler);
        sendOnlineUserList();
    }

    public synchronized void sendBroadcastMessage(ChatMessage chatMessage) {
        for (ClientHandler clientHandler : onlineUsersList) {
            clientHandler.sendMessage(chatMessage);
        }
    }

    public synchronized void sendPrivateMessage(ChatMessage chatMessage) {
        for (ClientHandler clientHandler : onlineUsersList) {
            if (clientHandler.getCurrentName().equals(chatMessage.getTo())) clientHandler.sendMessage(chatMessage);
        }
    }

    public synchronized boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : onlineUsersList) {
            if (clientHandler.getCurrentName().equals(username)) return true;
        }
        return false;
    }

    public synchronized void sendOnlineUserList() {
        ChatMessage list = new ChatMessage();
        list.setOnlineUsers(new ArrayList<>());
        list.setMessageType(MessageType.CLIENT_LIST);

        for (ClientHandler clientHandler : onlineUsersList) {
            list.getOnlineUsers().add(clientHandler.getCurrentName());
        }
        for (ClientHandler clientHandler : onlineUsersList) {
            clientHandler.sendMessage(list);
        }
    }

    public AuthService getAuthService() {
        return authService;
    }


}
