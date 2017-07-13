package client.gui;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.text.BadLocationException;

import client.Client;
import client.MessageReceiver;
import client.configuration.Configuration;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;

class SwingGUI implements MessageReceiver, Runnable, LockMessageReceiver, ChatFrameActionReceiver, LoginReceiver {

	private Client client;
	private String userName;
	private Color userColor;
	private String serverIp;
	private LoginFrame loginFrameInstance;
	private ChatFrame chatFrameInstance;
	private SettingsFrame settingsFrameInstance;
	private Thread lockListenerThread;

	public SwingGUI() throws UnknownHostException, IOException {
		new FileToImageConverter(this);
		loginFrameInstance = new LoginFrame(this);
	}

	private Color getPropertiesColor() {
		Configuration.loadConfiguration();
		Color color = Configuration.getColor();

		if (color != null) {
			userColor = color;
		} else {
			Configuration.generateColor();
			userColor = Configuration.getColor();
		}

		return color;
	}

	@Override
	public void clientInfoReceived(String message) {
		chatFrameInstance.clientInfoReceived(message);
	}

	@Override
	public void messageReceived(TextMessage message) throws IOException {
		chatFrameInstance.messageReceived(message);
	}

	@Override
	public void logsReceived(List<TextMessage> logs) {
		chatFrameInstance.logsReceived(logs);
	}

	// TODO Not Working
	@Override
	public void activeUsersReceived(List<User> activeUsers) {
		chatFrameInstance.activeUsersReceived(activeUsers);
	}

	@Override
	public void run() {
		new LockListener(this);
	}

	@Override
	public void onLock() {
		client.userStateSend(true);
	}

	@Override
	public void onUnlock() {
		client.userStateSend(false);
	}

	@Override
	public void typingStateReceived(TypingState typingState) {
		chatFrameInstance.typingStateReceived(typingState);
	}

	@Override
	public void typing() {
		client.typingStateSend(new TypingState(userName));
	}

	@Override
	public void clientShutdownMessage() {
		chatFrameInstance.clientShutdownMessage();
	}

	@Override
	public void settingsFrameCalled() {
		if (settingsFrameInstance == null || !settingsFrameInstance.getFrame().isVisible()) {
			settingsFrameInstance = new SettingsFrame(chatFrameInstance.getFavEmojiListModel(),
					chatFrameInstance.getFeed());
		}
	}

	@Override
	public void reconnectWasCalled() {
		restartClient();
	}

	@Override
	public void messageSent(String message, Color color, String receiver) {
		client.messageSent(message, color, receiver);
	}

	@Override
	public void login(String userName, String serverIp) {
		this.userName = userName;
		this.serverIp = serverIp;
		loginFrameInstance.getFrame().dispose();
		Color color = getPropertiesColor();
		try {
			Configuration.setUsername(userName);
		} catch (Exception e1) {
			System.out.println("File doesn't exist");
		}

		try {
			chatFrameInstance = new ChatFrame(userName, color, this);
			client = new Client(userName, this, color, serverIp);

			if (lockListenerThread == null) {
				lockListenerThread = new Thread(this);
				lockListenerThread.start();
			}
		} catch (IOException | ClassNotFoundException e1) {
			clientShutdownMessage();
			System.out.println("Kann nicht auf den Server verbinden.");
			try {
				chatFrameInstance.appendName(
						"*Verbindung gescheitert. Sie können leider keine Nachrichten versenden oder empfangen.*",
						false);
			} catch (BadLocationException | IOException e) {
				e.printStackTrace();
			}
			chatFrameInstance.scrollToBottom();
		}

	}

	@Override
	public void restartClient() {
		try {
			chatFrameInstance.shutdown();
			if (client != null) {
				client.shutdown();
				client = null;
			}
			chatFrameInstance = null;

			chatFrameInstance = new ChatFrame(userName, userColor, this);
			client = new Client(userName, this, userColor, serverIp);
		} catch (IOException | ClassNotFoundException e1) {
			System.out.println("Kann nicht auf den Server verbinden.");
			clientShutdownMessage();
			try {
				chatFrameInstance.appendName("*Verbindung gescheitert*\n\n", false);
				chatFrameInstance.scrollToBottom();
			} catch (BadLocationException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}