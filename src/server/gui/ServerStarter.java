package server.gui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

class ServerStarter {
	private static RandomAccessFile randomFile;

	public static void main(String[] args) throws IOException {
		try{
			File theDir = new File(System.getProperty("user.home") + "/Chat");
			if (!theDir.exists()) {
				boolean result = false;

				try {
					theDir.mkdir();
					result = true;
				} catch (SecurityException se) {
					System.out.println("Error occured while starting the application");
				}
				if (result) {
					System.out.println("DIR created");
				}
			}

		    randomFile = new RandomAccessFile(System.getProperty("user.home") + "/Chat/singleServer.class","rw");

		    FileChannel channel = randomFile.getChannel();

		    if(channel.tryLock() == null){
				System.out.println("Already Running...");
		    } else {
				new SwingServer();
		    }
		} catch (Exception e) {
		    System.out.println(e.toString());
		}
	}
}