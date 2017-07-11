package client;

import java.io.IOException;
import java.util.List;

import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;

interface ClientConnectionMessageReceiver {
	void messageReceivedFromServer(TextMessage message) throws IOException;

	void logsReceivedFromServer(List<TextMessage> logs);

	void activeUsersReceivedFromServer(List<User> activeUsers);

	void typingStateReceivedFromServer(TypingState typingState);

	void clientInfoReceived(String message);

	void clientShutdownMessage();

}
