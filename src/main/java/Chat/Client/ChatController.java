package Chat.Client;

import Chat.Client.ControllerServices.ChatHistory;
import Chat.Client.ControllerServices.Interfaces.ChatService;
import Chat.Client.ControllerServices.ChatServiceImpl;
import Chat.Client.ControllerServices.Interfaces.WhatTheMessage;
import Chat.Common.ChatMessage;
import Chat.Common.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable, WhatTheMessage {

    public VBox chatPanel;
    public TextField inputField;
    public javafx.scene.layout.VBox chatBox;
    public ListView onlineUsers;
    public ScrollPane scrollPaneChat;
    public ScrollPane scrollPaneUsers;

    public AnchorPane authPanel;
    public TextField loginField;
    public PasswordField passwordField;
    public Button buttonLogIn;

    public AnchorPane registrationPanel;
    public TextField loginRegistration;
    public PasswordField passwordRegistration;
    public TextField usernameRegistration;
    public PasswordField passwordRegistration1;

    public AnchorPane changePassPanel;
    public TextField loginChangePass;
    public PasswordField oldPasswordChangePass;
    public PasswordField newPasswordChangePass;
    public PasswordField newPasswordChangePass1;

    public AnchorPane changeNamePanel;
    public TextField newUsernameChangeUsername;
    public TextField loginChangeUsername;
    public PasswordField passwordChangeUsername;

    private final int PORT = 3048;
    private final String host = "localhost";
    private ChatService chatService;
    private final ChatHistory chatHistory = new ChatHistory();
    private String currentName;
    private List<Label> chatHistoryList = new ArrayList<>();
    private int index = 0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPaneChat.setStyle("-fx-background-color:transparent;");
        scrollPaneUsers.setStyle("-fx-background-color:transparent;");
        this.chatService = new ChatServiceImpl(host, PORT, this);
    }

    @Override
    public void whatTheMessage(String json) {
        ChatHistory history = new ChatHistory();
        Platform.runLater(() -> {
            ChatMessage message = ChatMessage.fromJson(json);
            System.out.println("Message received");
            switch (message.getMessageType()) {
                case PRIVATE:
                case PUBLIC:
                    appendTextToChatArea(message);
                    break;
                case AUTH_CONFIRM:
                    authPanel.setVisible(false);
                    chatPanel.setVisible(true);
                    this.currentName = message.getBody();
                    AppClient.stage1.setTitle(currentName);
                    history.appendChatHistoryToChatArea(this.currentName, chatHistoryList, chatBox);
                    chatHistoryList.clear();
                    scrollPaneChat.vvalueProperty().bind(chatBox.heightProperty());
                    break;
                case CLIENT_LIST:
                    refreshOnlineUserList(message);
                    break;
                case ERROR:
                    showError(message);
                    break;
                case REG_CONFIRM:
                case PASSWORD_CONFIRM:
                    backOnAuthPanel();
                    break;
                case CHANGE_USERNAME_CONFIRM:
                    backOnChatPanel();

                    File oldName = new File(this.currentName + ".txt");
                    this.currentName = message.getUsername();
                    File newName = new File(this.currentName + ".txt");

                    if (newName.exists()) {
                        newName.delete();
                    }
                    oldName.renameTo(newName);
                    AppClient.stage1.setTitle(currentName);
                    break;
            }
        });
    }

    private void refreshOnlineUserList(ChatMessage message) {
        message.getOnlineUsers().add(0, "PUBLIC");
        this.onlineUsers.setItems(FXCollections.observableArrayList(message.getOnlineUsers())); /*Не додумался*/
        this.onlineUsers.getSelectionModel().selectFirst();


    }

    private void appendTextToChatArea(ChatMessage message) {
        String modMessage = "";

        if (message.getFrom().equals(currentName)) {
            modMessage = "ME: ";
        } else if (message.getMessageType() == MessageType.PUBLIC) {
            modMessage = String.format(("[pub][%s]: "), message.getFrom());
        } else if (message.getMessageType() == MessageType.PRIVATE) {
            modMessage = String.format(("[PRIV][%s]: "), message.getFrom());
        }
        String text = modMessage + message.getBody();
        chatHistoryList.add(new Label(text));

        if (message.getFrom().equals(currentName)) {
            chatHistoryList.get(index).setAlignment(Pos.TOP_RIGHT);
            chatHistoryList.get(index).setStyle("-fx-background-color:#26c7ba;");
        }else if (message.getMessageType().equals(MessageType.PRIVATE)){
            chatHistoryList.get(index).setStyle("-fx-background-color:#ffcd54;");
        } else {
            chatHistoryList.get(index).setAlignment(Pos.TOP_LEFT);
            chatHistoryList.get(index).setStyle("-fx-background-color:#e1e73d;");
        }
        chatHistoryList.get(index).setPrefWidth(598);
        chatHistoryList.get(index).setWrapText(true);

        chatBox.getChildren().add(chatHistoryList.get(index));
        scrollPaneChat.vvalueProperty().bind(chatBox.heightProperty());
        index++;

        chatHistory.saveChatHistory(chatHistoryList, this.currentName, message);
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void sendMessage(ActionEvent actionEvent) {
        String text = inputField.getText();
        if (text.trim().isEmpty()) {
            inputField.clear();
            return;
        }
        ChatMessage message = new ChatMessage();
        if (onlineUsers.getSelectionModel().getSelectedItem().equals("PUBLIC")) {
            message.setMessageType(MessageType.PUBLIC);
        } else {
            message.setMessageType(MessageType.PRIVATE);
            message.setTo((String) onlineUsers.getSelectionModel().getSelectedItem());
        }
        message.setBody(text);
        message.setFrom(this.currentName);

        chatService.send(message.toJson());
        inputField.clear();
    }

    public void sendAuth(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) return;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setLogin(login);
        chatMessage.setPassword(password);
        chatMessage.setMessageType(MessageType.SEND_AUTH);

        chatService.send(chatMessage.toJson());
    }

    public void regUsers(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();

        String username = usernameRegistration.getText();
        String login = loginRegistration.getText();
        String password = passwordRegistration.getText();
        String passwordRepeat = passwordRegistration1.getText();

        ChatMessage message = new ChatMessage();
        if (username.trim().isEmpty() || login.trim().isEmpty() || password.trim().isEmpty() || passwordRepeat.trim().isEmpty()) {
            message.setMessageType(MessageType.ERROR);
            message.setBody("Something field is empty, try again");
            showError(message);
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

    public void changePass(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();

        String login = loginChangePass.getText();
        String oldPassword = oldPasswordChangePass.getText();
        String newPassword = newPasswordChangePass.getText();
        String newPassword1 = newPasswordChangePass1.getText();

        ChatMessage message = new ChatMessage();
        message.setMessageType(MessageType.CHANGE_PASSWORD);
        message.setLogin(login);
        message.setBody(oldPassword);
        message.setPassword(newPassword);
        message.setPasswordRepeat(newPassword1);

        chatService.send(message.toJson());
    }

    public void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong!");
        alert.setHeaderText(e.getMessage());

        VBox dialog = new VBox();
        Label label = new Label("Trace:");
        TextArea textArea = new TextArea();

        StringBuilder builder = new StringBuilder();
        for (StackTraceElement el : e.getStackTrace()) {
            builder.append(el).append(System.lineSeparator());
        }
        textArea.setText(builder.toString());
        dialog.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialog);
        alert.showAndWait();
    }

    public void showError(ChatMessage msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong!");
        alert.setHeaderText(msg.getMessageType().toString());

        VBox dialog = new VBox();
        Label label = new Label("Error:");
        TextArea textArea = new TextArea();

        textArea.setText(msg.getBody());
        dialog.getChildren().addAll(label, textArea);
        alert.getDialogPane().setContent(dialog);
        alert.showAndWait();
    }

    public void changeUsername(ActionEvent actionEvent) {
        String username = newUsernameChangeUsername.getText();
        String password = passwordChangeUsername.getText();
        String login = loginChangeUsername.getText();

        ChatMessage message = new ChatMessage();
        if (username.equals(currentName)) {
            message.setMessageType(MessageType.ERROR);
            message.setBody("You used this username");
            showError(message);
            return;
        }
        message.setMessageType(MessageType.CHANGE_USERNAME);
        message.setLogin(login);
        message.setUsername(username);
        message.setPassword(password);
        message.setBody(this.currentName);
        chatService.send(message.toJson());
    }

    public void changeUsernameMenuBar(ActionEvent actionEvent) {
        changeNamePanel.setVisible(true);
        chatPanel.setVisible(false);
    }


    public void entranceChangePassPanelHyperLink(ActionEvent actionEvent) {
        authPanel.setVisible(false);
        changePassPanel.setVisible(true);
    }

    public void entranceRegPanelButton(ActionEvent actionEvent) {
        authPanel.setVisible(false);
        registrationPanel.setVisible(true);
    }

    public void backOnAuthPanelFx(ActionEvent actionEvent) {
        backOnAuthPanel();
    }

    public void backOnAuthPanel() {
        changePassPanel.setVisible(false);
        registrationPanel.setVisible(false);
        authPanel.setVisible(true);
    }

    public void backOnChatPanel(ActionEvent actionEvent) {
        backOnChatPanel();
    }

    private void backOnChatPanel() {
        changeNamePanel.setVisible(false);
        chatPanel.setVisible(true);
    }

}
