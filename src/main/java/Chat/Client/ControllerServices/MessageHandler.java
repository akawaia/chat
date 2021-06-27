package Chat.Client.ControllerServices;

import Chat.Client.ChatController;
import Chat.Client.ControllerServices.Interfaces.ChatService;
import Chat.Common.ChatMessage;
import Chat.Common.MessageType;

public class MessageHandler {
    private ChatService chatService;
    private ChatController chatController;

    public MessageHandler(ChatService chatService, ChatController chatController) {
        this.chatService = chatService;
        this.chatController = chatController;
    }

    public void sendAuth(String login, String password) {
        if (login.isEmpty() || password.isEmpty()) return;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setLogin(login);
        chatMessage.setPassword(password);
        chatMessage.setMessageType(MessageType.SEND_AUTH);

        chatService.send(chatMessage.toJson());
    }

    public void regUsers(String username, String login, String password, String passwordRepeat) {
        ChatMessage message = new ChatMessage();
        if (username.trim().isEmpty() || login.trim().isEmpty() || password.trim().isEmpty() || passwordRepeat.trim().isEmpty()) {
            message.setMessageType(MessageType.ERROR);
            message.setBody("Something field is empty, try again");
            chatService.send(message.toJson());
            System.out.println("Something field is empty, try again");
            return;
        }
        message.setMessageType(MessageType.REGISTRATION);
        message.setLogin(login);
        message.setPassword(password);
        message.setPasswordRepeat(passwordRepeat);
        message.setUsername(username);

        chatService.send(message.toJson());
    }

    public void changePass(String login, String oldPassword, String newPassword, String newPassword1) {
        ChatMessage message = new ChatMessage();
        message.setMessageType(MessageType.CHANGE_PASSWORD);
        message.setLogin(login);
        message.setBody(oldPassword);
        message.setPassword(newPassword);
        message.setPasswordRepeat(newPassword1);

        chatService.send(message.toJson());
    }

    public void changeUsername(String username, String password, String login) {
        ChatMessage message = new ChatMessage();
        if (username.equals(chatController.getCurrentName())) {
            message.setMessageType(MessageType.ERROR);
            message.setBody("You used this username");
            chatService.send(message.toJson());
            return;
        }
        message.setMessageType(MessageType.CHANGE_USERNAME);
        message.setLogin(login);
        message.setUsername(username);
        message.setPassword(password);
        message.setBody(chatController.getCurrentName());
        chatService.send(message.toJson());
    }

    public void sendMessage(String text) {
        ChatMessage message = new ChatMessage();
        if (chatController.getOnlineUsers().getSelectionModel().getSelectedItem().equals("PUBLIC")) {
            message.setMessageType(MessageType.PUBLIC);
        } else {
            message.setMessageType(MessageType.PRIVATE);
            message.setTo((String) chatController.getOnlineUsers().getSelectionModel().getSelectedItem());
        }
        message.setBody(text);
        message.setFrom(chatController.getCurrentName());

        chatService.send(message.toJson());
    }

}
