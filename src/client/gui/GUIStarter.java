package client.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

class GUIStarter {
	private static RandomAccessFile lockFile;

	public static void main(String[] args) {
		try {
			if (tryLock()) {
				new SwingGUI();
			} else {
				System.out.println("Already running,...");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean tryLock() throws FileNotFoundException {
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
		try {
			lockFile = new RandomAccessFile(System.getProperty("user.home") + "/Chat/singleChat.class", "rw");
			FileChannel channel = lockFile.getChannel();

			if (channel.tryLock() == null) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}