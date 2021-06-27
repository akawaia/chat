package Chat.Client.ControllerServices;

import Chat.Common.ChatMessage;
import Chat.Common.PropClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(ChatHistory.class);
    private int sizeChatHistory = Integer.parseInt(PropClass.properties("sizeChatHistory"));

    public void saveChatHistory(List<Label> chatHistoryList, String currentName, ChatMessage message) {
        ChatMessage chatMessage = new ChatMessage();
        File file = new File(currentName + ".txt");
        if (file.exists()) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
                for (int i = chatHistoryList.size() - 1; i < chatHistoryList.size(); i++) {
                    chatMessage.setBody(chatHistoryList.get(i).getText());
                    chatMessage.setFrom(message.getFrom());
                    bufferedWriter.write(chatMessage.toJson() + "\n");
                }
            } catch (IOException e) {
                LOGGER.error(e.getStackTrace());
            }
        } else {
            try {
                file.createNewFile();
                saveChatHistory(chatHistoryList, currentName, message);
            } catch (IOException e) {
                LOGGER.error(e.getStackTrace());
            }
        }
    }

    public void appendChatHistoryToChatArea(String currentName, List<Label> chatHistoryList, VBox chatBox) {
        String fileName = currentName + ".txt";
        ArrayList<String> arrayList = new ArrayList<>();
        File file = new File(fileName);
        if (file.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
                String textForAppend;
                while ((textForAppend = bufferedReader.readLine()) != null) arrayList.add(textForAppend);
                if (arrayList.size() > sizeChatHistory) {
                    for (int i = arrayList.size() - sizeChatHistory; i < arrayList.size(); i++) {
                        workWithJson(currentName, chatHistoryList, arrayList, i, chatBox);
                    }
                } else {
                    for (int i = 0; i < arrayList.size(); i++) {
                        workWithJson(currentName, chatHistoryList, arrayList, i, chatBox);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getStackTrace());
            }
        }else return;
    }

    private void workWithJson(String currentName, List<Label> chatHistoryList, ArrayList<String> arrayList, int i, VBox chatBox) {
        ChatMessage fromJson = ChatMessage.fromJson(arrayList.get(i));
        String from = fromJson.getFrom();
        chatHistoryList.add(new Label(fromJson.getBody()));

        if (from.equals(currentName)) chatHistoryList.get(i).setAlignment(Pos.TOP_RIGHT);
        else chatHistoryList.get(i).setAlignment(Pos.TOP_LEFT);
        chatHistoryList.get(i).setPrefWidth(Double.parseDouble(PropClass.properties("widthLabel")));
        chatHistoryList.get(i).setWrapText(true);
        chatBox.getChildren().add(chatHistoryList.get(i));
    }
}
