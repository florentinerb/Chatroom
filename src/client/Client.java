package client;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import protocol.FileMessage;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;
import protocol.UserState;

public class Client implements ClientConnectionMessageReceiver {
	private String name;

	private MessageReceiver messageReceiver;
	private ClientConnection clientConnection;

	public Client(String name, MessageReceiver messageReceiver, Color color, String serverIp)
			throws UnknownHostException, IOException, ClassNotFoundException {
		this.messageReceiver = messageReceiver;
		this.name = name;
		
		clientConnection = new ClientConnection(this, name, serverIp);
	}

	@Override
	public void messageReceivedFromServer(TextMessage message) throws IOException {
		messageReceiver.messageReceived(message);
	}

	@Override
	public void logsReceivedFromServer(List<TextMessage> logs) {
		messageReceiver.logsReceived(logs);
	}

	@Override
	public void activeUsersReceivedFromServer(List<User> activeUsers) {
		messageReceiver.activeUsersReceived(activeUsers);
	}

	@Override
	public void typingStateReceivedFromServer(TypingState typingState) {
		messageReceiver.typingStateReceived(typingState);
	}

	public void messageSent(String text, Color userColor, String receiverName) {
		TextMessage message = new TextMessage(text, name, userColor, receiverName);
		try {
			clientConnection.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void userStateSend(Boolean locked) {
		UserState userState = new UserState(clientConnection.getSocket().getLocalAddress().toString(), locked);
		try {
			clientConnection.sendUserState(userState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void typingStateSend(TypingState typingState) {
		try {
			clientConnection.sendTypingState(typingState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clientInfoReceived(String message) {
		messageReceiver.clientInfoReceived(message);
	}

	@Override
	public void clientShutdownMessage() {
		messageReceiver.clientShutdownMessage();
	}

	@Override
	public void fileMessageReceived(String sender, String fileName) {
		messageReceiver.fileMessageReceived(sender, fileName);
	}

	public void shutdown() {
		clientConnection.shutdown();
	}

	public void sendFile(FileMessage fileMessage) {
		clientConnection.sendFile(fileMessage);
	}

}
