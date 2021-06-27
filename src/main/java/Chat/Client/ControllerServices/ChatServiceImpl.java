package Chat.Client.ControllerServices;

import Chat.Client.ChatController;
import Chat.Client.ControllerServices.Interfaces.ChatService;
import Chat.Client.ControllerServices.Interfaces.WhatTheMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;

public class ChatServiceImpl implements ChatService {
    private final WhatTheMessage whatTheMessage;
    private Network network;
    private static final Logger LOGGER = (Logger) LogManager.getLogger(ChatServiceImpl.class);


    public ChatServiceImpl(WhatTheMessage whatTheMessage) {
        this.whatTheMessage = whatTheMessage;
    }

    @Override
    public void send(String message) {
        network.sendMessage(message);
    }

    @Override
    public void receive(String receiveMessage) {
        whatTheMessage.whatTheMessage(receiveMessage);
    }

    @Override
    public void connect() {
        try {
            this.network = new Network(this);
            network.readMessage();
        } catch (IOException e) {
            LOGGER.error(e.getStackTrace());
        }
    }

    @Override
    public boolean isConnected() {
        return this.network != null;
    }
}
