package Chat.Server;

import Chat.Common.ChatMessage;
import Chat.Common.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;
    private ChatServer chatServer;
    private String currentName;
    private static final Logger LOGGER = (Logger) LogManager.getLogger(ClientHandler.class);


    public ClientHandler(Socket socket, ChatServer chatServer) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.chatServer = chatServer;

    }

    public void startHandler() {
        new Thread(()->{
            try {
                Thread thread = closeResourcesIfNotLogin();
                thread.start();
                readMessage(thread);
            } catch (IOException e) {
                LOGGER.info("Client disconnected");
            } finally {
                closeHandler();
            }
        }).start();
    }

    public void readMessage(Thread thread) throws IOException {
        while (!Thread.currentThread().isInterrupted() || socket.isConnected()) {
            String receiveMessage = in.readUTF();
            ChatMessage message = ChatMessage.fromJson(receiveMessage);
            switch (message.getMessageType()) {
                case PUBLIC:
                    chatServer.sendBroadcastMessage(message);
                    break;
                case PRIVATE:
                    chatServer.sendPrivateMessage(message);
                    break;
                case SEND_AUTH:
                    LOGGER.info("Start client auth");
                    authenticate(message, thread);
                    break;
                case REGISTRATION:
                    LOGGER.info("Start registration");
                    registration(message);
                    break;
                case CHANGE_PASSWORD:
                    LOGGER.info("Start to change pass");
                    changePassword(message);
                    break;
                case CHANGE_USERNAME:
                    LOGGER.info("Start change username.");
                    changeUsername(message);
            }
        }
    }

    private void changeUsername(ChatMessage message) {
        ChatMessage response = new ChatMessage();
        if (chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getPassword()).equals(message.getBody())) {
            if (chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getPassword()).isEmpty()) {
                errorHandler("Wrong login or password.");
            } else if (!chatServer.getAuthService().getUsernameByUsername(message.getUsername()).equals(message.getUsername())) {
                errorHandler("This username already used. Try again with other username.");
            } else {
                chatServer.getAuthService().changeUsername(message.getUsername(), message.getLogin(), message.getPassword());
                this.currentName = chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getPassword());
                chatServer.sendOnlineUserList();
                response.setMessageType(MessageType.CHANGE_USERNAME_CONFIRM);
                response.setUsername(this.currentName);
                sendMessage(response);
                LOGGER.info("Success change username");
            }
        } else if (!chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getPassword()).equals(message.getBody())) {
            errorHandler("You can`t change this username.");
        } else {
            errorHandler("Something went wrong.");
        }

    }


    private void registration(ChatMessage message) {
        ChatMessage response = new ChatMessage();
        if (!chatServer.getAuthService().getUsernameByUsername(message.getUsername()).equals(message.getUsername())) {
            errorHandler("This username already used. Try again with other username.");
        } else if (!chatServer.getAuthService().getLoginByLogin(message.getLogin()).equals(message.getLogin())) {
            errorHandler("This login already used. Try again with other login.");
        } else if (!message.getPassword().equals(message.getPasswordRepeat())) {
            errorHandler("Password mismatch");
        } else {
            chatServer.getAuthService().registration(message.getUsername(), message.getLogin(), message.getPassword());
            response.setMessageType(MessageType.REG_CONFIRM);
            sendMessage(response);
            LOGGER.info("Success registration user with username: " + message.getUsername());
        }
    }

    public void authenticate(ChatMessage message, Thread thread) {
        ChatMessage response = new ChatMessage();
        String username = chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getPassword());
        if (username.isEmpty()) {
            errorHandler("This account doesn't exist. Try again with other login and password");
        } else if (chatServer.isUserOnline(username)) {
            errorHandler("This user is already online");
        } else {
            thread.interrupt();
            this.currentName = username;
            chatServer.subscribed(this);
            response.setMessageType(MessageType.AUTH_CONFIRM);
            response.setBody(username);
            sendMessage(response);
            LOGGER.info("Success auth");
        }
    }

    private void changePassword(ChatMessage message) {
        ChatMessage response = new ChatMessage();
        if (!chatServer.getAuthService().getUsernameByLoginAndPassword(message.getLogin(), message.getBody()).isEmpty()) {
            if (message.getBody().equals(message.getPassword())) {
                errorHandler("Old password equals new password.");
            } else if (message.getPassword().equals(message.getPasswordRepeat())) {
                chatServer.getAuthService().changePassword(message.getLogin(), message.getPassword());
                response.setMessageType(MessageType.PASSWORD_CONFIRM);
                sendMessage(response);
                LOGGER.info("Success change pass");
            }
        } else {
            errorHandler("Wrong login or old password. Repeat again");
        }
    }


    private Thread closeResourcesIfNotLogin() {
        ChatMessage response = new ChatMessage();
        long timeOff = 180_000;
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(timeOff);
                response.setMessageType(MessageType.ERROR);
                response.setBody("Waiting time exceeded. Please, reload you client and try again");
                sendMessage(response);
                closeHandler();
            } catch (InterruptedException e) {
                LOGGER.trace("Thread <close handler> is interrupted");
            }
        });
        return thread;
    }

    public void sendMessage(ChatMessage chatMessage) {
        try {
            out.writeUTF(chatMessage.toJson());
        } catch (IOException e) {
            LOGGER.error(e.getStackTrace());
        }
    }

    public String getCurrentName() {
        return currentName;
    }

    private void closeHandler() {
        try {
            chatServer.unsubscribed(this);
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e.getStackTrace());
        }
    }

    private synchronized void errorHandler (String whatTheMistake) {
        ChatMessage response = new ChatMessage();
        response.setMessageType(MessageType.ERROR);
        response.setBody(whatTheMistake);
        sendMessage(response);
        LOGGER.error(whatTheMistake);
    }

}
