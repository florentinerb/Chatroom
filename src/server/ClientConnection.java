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
import protocol.FileMessage;
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

	ClientConnection(Socket s, ClientMessageListener clientMessageListener) throws IOException {
		locked = false;

		this.clientMessageListener = clientMessageListener;

		this.s = s;
		out = new ObjectOutputStream(this.s.getOutputStream());
		readAndSendLogs();
		in = new ObjectInputStream(this.s.getInputStream());
		alive = true;

		try {
			this.setName((String) in.readObject());
		} catch (Exception e) {
			System.out.println("Error occurred when casting userName to String");
		}

		clientConnectionThread = new Thread(this);
		clientConnectionThread.start();
	}

	@SuppressWarnings("unchecked")
	private void readAndSendLogs() throws IOException {
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

		List<SealedObject> sealedLogs = new ArrayList<SealedObject>();
		List<TextMessage> shortenedLogs;
		if (!logs.isEmpty()) {
			if (logs.size() > NUMBEROFLOGMESSAGES) {
				shortenedLogs = logs.subList(logs.size() - NUMBEROFLOGMESSAGES, logs.size());
			} else {
				shortenedLogs = logs.subList(0, logs.size());
			}

			for (TextMessage tm : shortenedLogs) {
				try {
					sealedLogs.add(edtm.sealTextMessage(tm));
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
					e.printStackTrace();
				}
			}
		}
		out.writeObject(sealedLogs);
	}

	public void sendMessage(TextMessage message) throws IOException {
		if (canUserSeeMessage(message)) {
			try {
				out.writeObject(edtm.sealTextMessage(message));
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				e.printStackTrace();
			}
		}
	}

	private Boolean canUserSeeMessage(TextMessage message) {
		if (message.getReceiverName().equals("")) {
			return true;
		} else {
			if (message.getReceiverName().equals(name) || message.getSenderName().equals(name)) {
				return true;
			}
		}
		return false;
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
				if (inObject instanceof FileMessage) {
					FileMessage fileMessage = (FileMessage) inObject;
					clientMessageListener.fileMessageReceived(fileMessage);
				}

			} catch (Exception e) {
				System.out.println("Client disconnected!");
				alive = false;
				shutdown();
			}
		}
		shutdown();
	}

	public void threadShutdown() {
		alive = false;
		try {
			in.close();
			out.close();
		} catch (IOException e) {
		}
	}

	public void shutdown() {
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

	public void sendFileMessage(FileMessage fileMessage) {
		try {
			out.writeObject(fileMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
