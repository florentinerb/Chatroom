package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.FileUtils;

import client.configuration.Configuration;
import protocol.EncryptDecryptTextMessage;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;
import protocol.UserState;

class ClientConnection implements Runnable {
	private int port = 1234;
	private String serveradress = "10.1.201.136";
	private Socket s;
	private ObjectInputStream inObject;
	private ObjectOutputStream out;
	private EncryptDecryptTextMessage edtm = new EncryptDecryptTextMessage();

	private ClientConnectionMessageReceiver clientConnectionMessageReceiver;
	private Thread clientConnectionThread;
	private volatile boolean alive = true;

	ClientConnection(ClientConnectionMessageReceiver clientConnectionMessageReceiver, String name)
			throws IOException, ClassNotFoundException {
		this.clientConnectionMessageReceiver = clientConnectionMessageReceiver;

		certificateCheck();
		checkOptionalServerIp();
		setupConnection();
		readLogs();

		out.writeObject(name);
		clientConnectionThread = new Thread(this);
		alive = true;
		clientConnectionThread.start();
	}

	public void certificateCheck() throws IOException {
		File f = new File(System.getProperty("user.home") + "/Chat/truststore.key");
		if (!f.exists()) {
			URL inputUrl = getClass().getClassLoader().getResource("certificates/truststore.key");
			File dest = new File(System.getProperty("user.home") + "/Chat/truststore.key");
			FileUtils.copyURLToFile(inputUrl, dest);
		}
	}

	public void checkOptionalServerIp() {
		String optionalServerIp = Configuration.getServerIP();
		if (optionalServerIp != null) {
			if (!"0".equals(optionalServerIp)) {
				this.serveradress = optionalServerIp;
			}
		} else {
			Configuration.setServerIP("0");
		}
	}

	public void setupConnection() throws UnknownHostException, IOException {
		System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.home") + "/Chat/truststore.key");
		System.setProperty("javax.net.ssl.trustStorePassword", "lehrlingschat2016");
		SocketFactory factory = SSLSocketFactory.getDefault();

		s = factory.createSocket(serveradress, port);

		inObject = new ObjectInputStream(s.getInputStream());
		out = new ObjectOutputStream(s.getOutputStream());

	}

	public void readLogs() throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		ArrayList<SealedObject> sealedLogs = (ArrayList<SealedObject>) inObject.readObject();
		List<TextMessage> logs = new ArrayList<TextMessage>();

		for (SealedObject s : sealedLogs) {
			logs.add(edtm.unsealTextMessage(s));
		}
		clientConnectionMessageReceiver.logsReceivedFromServer(logs);
	}

	public void sendMessage(TextMessage message) throws IOException {
		System.out.println(message.getTimeNameMessage());
		try {
			try {
				out.writeObject(edtm.sealTextMessage(message));
			} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUserState(UserState userState) throws IOException {
		try {
			out.writeObject(userState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendTypingState(TypingState typingState) throws IOException {
		try {
			out.writeObject(typingState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		int failCount = 0;
		while (alive) {
			try {
				Object receivedObject = inObject.readObject();
				if (receivedObject instanceof SealedObject) {
					Object unsealedReceivedObject = edtm.unsealTextMessage((SealedObject) receivedObject);
					if (unsealedReceivedObject instanceof TextMessage) {
						clientConnectionMessageReceiver
								.messageReceivedFromServer(edtm.unsealTextMessage((SealedObject) receivedObject));
					}
				} else if (receivedObject instanceof List) {
					clientConnectionMessageReceiver.activeUsersReceivedFromServer((ArrayList<User>) receivedObject);
				} else if (receivedObject instanceof TypingState) {
					clientConnectionMessageReceiver.typingStateReceivedFromServer((TypingState) receivedObject);
				}
			} catch (IOException | ClassNotFoundException e) {
				failCount++;
				if (failCount > 4) {
					System.out.println("Client shutdown due to errors");
					shutdown();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception while reading message was thrown but swallowed!");
			}
		}
	}

	private void shutdown() {
		alive = false;
	}

	public Socket getSocket() {
		return s;
	}

	public void setSocket(Socket s) {
		this.s = s;
	}

}
