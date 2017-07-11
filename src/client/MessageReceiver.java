package client;

import java.io.IOException;
import java.util.List;

import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;

public interface MessageReceiver {
	void messageReceived(TextMessage message) throws IOException;

	void logsReceived(List<TextMessage> logs);

	void activeUsersReceived(List<User> activeUsers);

	void typingStateReceived(TypingState typingState);

	void clientInfoReceived(String message);

	void clientShutdownMessage();

}
