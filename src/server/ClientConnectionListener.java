package server;

import java.net.Socket;

interface ClientConnectionListener {
	void clientConnectionReceived(Socket clientSocket);
}
