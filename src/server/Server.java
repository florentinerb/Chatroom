package server;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;
import protocol.UserState;

public class Server implements ClientConnectionListener, ClientMessageListener {
	private List<ClientConnection> clients = new ArrayList<ClientConnection>();
	private static File logFile = new File(System.getProperty("user.home") + "/Chat/logs.txt");
	private static List<TextMessage> allMessagesList = new ArrayList<TextMessage>();
	private ConnectionAcceptorThread connectionAcceptorThread;
	private static final String KICKPHRASE = "***blacklist";

	@SuppressWarnings("unchecked")
	public Server() throws IOException {
		if (!logFile.exists()) {
			logFile.createNewFile();
		} else {
			FileInputStream fis = new FileInputStream(Server.logFile);
			try (ObjectInputStream ois = new ObjectInputStream(fis);) {
				allMessagesList = (List<TextMessage>) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("No logs!");
			}

		}
		connectionAcceptorThread = new ConnectionAcceptorThread(this);
	}

	@Override
	public void clientShutdown(ClientConnection client) {
		try {
			client.getSocket().close();
			clients.remove(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendActiveClients();
	}

	@Override
	public void clientConnectionReceived(Socket clientSocket) {
		try {
			clients.add(new ClientConnection(clientSocket, this));
			sendActiveClients();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendActiveClients() {
		List<User> activeUsers = new ArrayList<User>();
		for (ClientConnection cc : clients) {
			activeUsers.add(new User(cc.getName(), cc.getLocked()));
		}
		for (ClientConnection cc : clients) {
			try {
				cc.sendActiveUsers(activeUsers);
			} catch (IOException e) {
				System.out.println("Could not send active Users!");
			}
		}
	}

	@Override
	public void clientMessageReceived(TextMessage message) {
		if (message.getMessage().length() < 1000) {
			System.out.println(message.getTimeNameMessage());
			if (message.getMessage().contains(KICKPHRASE)) {
				String nameOfKickedChatMember = message.getMessage().replaceAll("\n", "")
						.substring(message.getMessage().lastIndexOf(KICKPHRASE) + 1);
				for (ClientConnection client : clients) {
					if (client.getName().equals(nameOfKickedChatMember)) {
						try {
							client.sendMessage(new TextMessage("*You've been kicked*", "Server", Color.RED, null));
						} catch (IOException e) {
							e.printStackTrace();
						}
						connectionAcceptorThread
								.addClientToBlacklist(client.getSocket().getRemoteSocketAddress().toString());
						clientShutdown(client);
					}
				}
			} else {
				allMessagesList.add(message);
				List<TextMessage> messagesToWrite = allMessagesList;
				try {
					FileOutputStream fos = new FileOutputStream(logFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(messagesToWrite);
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (ClientConnection cc : clients) {
					try {
						cc.sendMessage(message);
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public void stop() throws IOException {
		System.exit(0);
	}

	@Override
	public void userStateReceived(UserState userState) {
		for (ClientConnection client : clients) {
			if (client.getSocket().getRemoteSocketAddress().toString().split(":")[0].equals(userState.getIpAdress())) {
				client.setLocked(userState.isLocked());
			}
		}
		sendActiveClients();
	}

	@Override
	public void typingStateReceived(TypingState typingState) {
		for (ClientConnection cc : clients) {
			try {
				cc.sendTypingState(typingState);
			} catch (IOException e) {
			}
		}
	}

	static File getLogFile() {
		return logFile;
	}

}
