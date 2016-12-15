package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.io.FileUtils;

class ConnectionAcceptorThread implements Runnable {
	private ServerSocket serverSocket;
	private Thread connectionAcceptorThread;
	private ClientConnectionListener clientConnectionListener;
	private volatile boolean alive = true;
	private List<String> blacklist = new ArrayList<String>();

	ConnectionAcceptorThread(ClientConnectionListener clientConnectionListener) {
		createSSLSocketWithContext();
		this.clientConnectionListener = clientConnectionListener;
		connectionAcceptorThread = new Thread(this);
		connectionAcceptorThread.start();

	}

	private void createSSLSocketWithContext() {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			KeyStore keyStore = KeyStore.getInstance("JKS");

			File f = new File(System.getProperty("user.home") + "/Chat/keystore.key");
			if (!f.exists()) {
				URL inputUrl = getClass().getClassLoader().getResource("certificates/keystore.key");
				File dest = new File(System.getProperty("user.home") + "/Chat/keystore.key");
				FileUtils.copyURLToFile(inputUrl, dest);
			}

			keyStore.load(new FileInputStream(System.getProperty("user.home") + "/Chat/keystore.key"),
					"lehrlingschat2016".toCharArray());

			keyManagerFactory.init(keyStore, "lehrlingschat2016".toCharArray());
			context.init(keyManagerFactory.getKeyManagers(), null, null);

			SSLServerSocketFactory factory = context.getServerSocketFactory();

			serverSocket = factory.createServerSocket(1234);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (alive) {
			try {
				Socket s = serverSocket.accept();
				if (!blacklist.contains(s.getRemoteSocketAddress().toString().substring(0, 13))) {
					clientConnectionListener.clientConnectionReceived(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addClientToBlacklist(String ip) {
		System.out.println("added: " + ip.split(":")[0]);
		blacklist.add(ip.split(":")[0]);
	}
}
