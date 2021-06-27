package Chat.Client;

import Chat.Client.ControllerServices.ChatHistory;
import Chat.Client.ControllerServices.Interfaces.ChatService;
import Chat.Client.ControllerServices.ChatServiceImpl;
import Chat.Client.ControllerServices.Interfaces.WhatTheMessage;
import Chat.Client.ControllerServices.MessageHandler;
import Chat.Common.ChatMessage;
import Chat.Common.MessageType;
import Chat.Common.PropClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

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


    private ChatService chatService;
    private MessageHandler messageHandler;
    private final ChatHistory CHAT_HISTORY = new ChatHistory();
    private String currentName;
    private List<Label> chatHistoryList = new ArrayList<>();
    private int index = 0;
    private static Stage stage;
    private static final Logger LOGGER = (Logger) LogManager.getLogger(ChatController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Success start client");
        scrollPaneChat.setStyle("-fx-background-color:transparent;");
        scrollPaneUsers.setStyle("-fx-background-color:transparent;");
        this.chatService = new ChatServiceImpl(this);
        this.messageHandler = new MessageHandler(chatService, this);
        LOGGER.info("Chat service and message handler ready to work");
    }

    @Override
    public void whatTheMessage(String json) {
        ChatHistory history = new ChatHistory();
        Platform.runLater(() -> {
            ChatMessage message = ChatMessage.fromJson(json);
            LOGGER.info("Message received");
            switch (message.getMessageType()) {
                case PRIVATE:
                case PUBLIC:
                    appendTextToChatArea(message);
                    break;
                case AUTH_CONFIRM:
                    auth_confirm(history, message);
                    break;
                case CLIENT_LIST:
                    refreshOnlineUserList(message);
                    break;
                case ERROR:
                    showError(message);
                    break;
                case REG_CONFIRM:
                    backOnChatPanel();
                    LOGGER.info("Registration completed");
                    break;
                case PASSWORD_CONFIRM:
                    backOnAuthPanel();
                    LOGGER.info("Change pass completed");
                    break;
                case CHANGE_USERNAME_CONFIRM:
                    change_username_confirm(message);
                    break;
            }
        });
    }

    private void change_username_confirm(ChatMessage message) {
        backOnChatPanel();
        File oldName = new File(this.currentName + ".txt");
        this.currentName = message.getUsername();
        File newName = new File(this.currentName + ".txt");
        if (newName.exists()) {
            newName.delete();
        }
        oldName.renameTo(newName);
        stage.setTitle(currentName);
        LOGGER.info("Change username completed");
    }

    private void auth_confirm(ChatHistory history, ChatMessage message) {
        authPanel.setVisible(false);
        chatPanel.setVisible(true);
        this.currentName = message.getBody();
        stage.setTitle(currentName);
        history.appendChatHistoryToChatArea(this.currentName, chatHistoryList, chatBox);
        chatHistoryList.clear();
        scrollPaneChat.vvalueProperty().bind(chatBox.heightProperty());
        LOGGER.info("Authentication complete");
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
        } else if (message.getMessageType().equals(MessageType.PRIVATE)) {
            chatHistoryList.get(index).setStyle("-fx-background-color:#ffcd54;");
        } else {
            chatHistoryList.get(index).setAlignment(Pos.TOP_LEFT);
            chatHistoryList.get(index).setStyle("-fx-background-color:#e1e73d;");
        }
        chatHistoryList.get(index).setPrefWidth(Double.parseDouble(PropClass.properties("widthLabel")));
        chatHistoryList.get(index).setWrapText(true);

        chatBox.getChildren().add(chatHistoryList.get(index));
        scrollPaneChat.vvalueProperty().bind(chatBox.heightProperty());
        index++;

        CHAT_HISTORY.saveChatHistory(chatHistoryList, this.currentName, message);
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
        messageHandler.sendMessage(text);
        LOGGER.info("Message send");
        inputField.clear();
    }


    public void sendAuth(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();

        String login = loginField.getText();
        String password = passwordField.getText();

        messageHandler.sendAuth(login, password);
        LOGGER.info("Authentication data send");
    }

    public void regUsers(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();

        String username = usernameRegistration.getText();
        String login = loginRegistration.getText();
        String password = passwordRegistration.getText();
        String passwordRepeat = passwordRegistration1.getText();

        messageHandler.regUsers(username, login, password, passwordRepeat);
        LOGGER.info("Registration data send");
    }

    public void changePass(ActionEvent actionEvent) {
        if (!chatService.isConnected()) chatService.connect();

        String login = loginChangePass.getText();
        String oldPassword = oldPasswordChangePass.getText();
        String newPassword = newPasswordChangePass.getText();
        String newPassword1 = newPasswordChangePass1.getText();

        messageHandler.changePass(login, oldPassword, newPassword, newPassword1);
        LOGGER.info("User start to change pass");
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

        messageHandler.changeUsername(username, password, login);
        LOGGER.info("User start to change username");
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

    public String getCurrentName() {
        return currentName;
    }

    public ListView getOnlineUsers() {
        return onlineUsers;
    }

    public static void setStage(Stage stage) {
        ChatController.stage = stage;
    }
}
