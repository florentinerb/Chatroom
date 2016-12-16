package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

import protocol.EncryptDecryptTextMessage;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;
import protocol.UserState;

class ClientConnection implements Runnable {
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Socket s;
	private ClientMessageListener clientMessageListener;
	private Thread clientConnectionThread;
	private volatile boolean alive = true;
	private final int NUMBEROFLOGMESSAGES = 60;
	private EncryptDecryptTextMessage edtm = new EncryptDecryptTextMessage();

	private String name;
	private Boolean locked;

	@SuppressWarnings("unchecked")
	ClientConnection(Socket s, ClientMessageListener clientMessageListener) throws IOException {
		locked = false;

		this.clientMessageListener = clientMessageListener;
		List<TextMessage> logs = new ArrayList<TextMessage>();

		try {
			FileInputStream fis = new FileInputStream(Server.getLogFile());
			ObjectInputStream ois = new ObjectInputStream(fis);

			try {
				logs = (List<TextMessage>) ois.readObject();
			} catch (Exception e) {
				System.out.println("No logs available");
			}
			ois.close();
		} catch (Exception ex) {
			System.out.println("No logs available");
		}

		this.s = s;
		out = new ObjectOutputStream(this.s.getOutputStream());
		in = new ObjectInputStream(this.s.getInputStream());
		alive = true;

		List<SealedObject> sealedLogs = new ArrayList<SealedObject>();
		List<TextMessage> shortenedLogs;
		if (!logs.isEmpty()) {
			if (logs.size() > NUMBEROFLOGMESSAGES) {
				shortenedLogs = logs.subList(logs.size() - NUMBEROFLOGMESSAGES, logs.size());
			} else {
				shortenedLogs = logs.subList(0, logs.size());
			}

			for (TextMessage tm : shortenedLogs) {
				if (tm.getReceiverName().equals("") || tm.getReceiverName().equals(name)
						|| tm.getSenderName().equals(name)) {
					try {
						sealedLogs.add(edtm.sealTextMessage(tm));
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		out.writeObject(sealedLogs);

		try {
			this.setName((String) in.readObject());
		} catch (Exception e) {
			System.out.println("Error occurred when casting userName to String");
		}

		clientConnectionThread = new Thread(this);
		clientConnectionThread.start();
	}

	public void sendMessage(TextMessage message) throws IOException {
		if (message.getReceiverName().equals("")) {
			try {
				out.writeObject(edtm.sealTextMessage(message));
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				e.printStackTrace();
			}
		} else {
			if (message.getReceiverName().equals(name) || message.getSenderName().equals(name)) {
				try {
					out.writeObject(edtm.sealTextMessage(message));
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void sendTypingState(TypingState typingState) throws IOException {
		out.writeObject(typingState);
	}

	public void sendActiveUsers(List<User> users) throws IOException {
		out.writeObject(users);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				Object inObject = in.readObject();
				if (inObject instanceof SealedObject) {
					Object unsealedObject = edtm.unsealTextMessage((SealedObject) inObject);
					if (unsealedObject instanceof TextMessage) {
						TextMessage message = (TextMessage) unsealedObject;
						clientMessageListener.clientMessageReceived(message);
					}
				}
				if (inObject instanceof UserState) {
					UserState userState = (UserState) inObject;
					clientMessageListener.userStateReceived(userState);
				}
				if (inObject instanceof TypingState) {
					TypingState typingState = (TypingState) inObject;
					clientMessageListener.typingStateReceived(typingState);
				}
			} catch (Exception e) {
				System.out.println("Client disconnected!");
				alive = false;
				shutdown();
			}
		}
		shutdown();
	}

	private void shutdown() {
		clientMessageListener.clientShutdown(this);
	}

	public Socket getSocket() {
		return s;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
