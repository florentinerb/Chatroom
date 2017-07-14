package server;

import protocol.FileMessage;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.UserState;

interface ClientMessageListener {
	void clientMessageReceived(TextMessage message);

	void clientShutdown(ClientConnection clientConnection);

	void userStateReceived(UserState userState);

	void typingStateReceived(TypingState typingState);

	void fileMessageReceived(FileMessage fileMessage);
}
