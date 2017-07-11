package client.configuration;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;

public class Configuration {
	private static File configFile;
	private static Properties properties;

	private Configuration() {

	}

	public static void loadConfiguration() throws IllegalStateException {
		properties = new Properties();
		configFile = new File(
				System.getProperty("user.home") + File.separator + "Chat" + File.separator + "config.properties");
		try (FileInputStream fileInputStream = new FileInputStream(configFile);) {
			properties.load(fileInputStream);
		} catch (IOException e) {
			try {
				if (!configFile.getParentFile().exists()) {
					configFile.getParentFile().mkdir();
					if (!configFile.exists()) {
						try {
							configFile.createNewFile();
							properties.load(new FileInputStream(configFile));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			} catch (Exception e2) {
				System.out.println("Filename is not valid");
			}
			System.out.println("File doesn't exist");
		}
	}

	public static String getUsername() throws IllegalStateException {
		try {
			return properties.getProperty("username");
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	public static String getServerIP() throws IllegalStateException {
		try {
			return properties.getProperty("serverIP");
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	public static Color getColor() throws IllegalStateException {
		try {
			Float r = new Float(properties.getProperty("r"));
			Float g = new Float(properties.getProperty("g"));
			Float b = new Float(properties.getProperty("b"));
			return new Color(r, g, b);
		} catch (Exception e) {
			return null;
		}
	}

	public static void setUsername(String newUserName) {
		try (OutputStream tryoutput = new FileOutputStream(configFile.getAbsoluteFile());) {
			OutputStream output = new FileOutputStream(configFile.getAbsoluteFile());

			properties.setProperty("username", newUserName);

			properties.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public static void generateColor() {
		try (OutputStream tryoutput = new FileOutputStream(configFile.getAbsoluteFile());) {
			OutputStream output = new FileOutputStream(configFile.getAbsoluteFile());

			Random rand = new Random();
			Float r = rand.nextFloat();
			Float g = rand.nextFloat();
			Float b = rand.nextFloat();

			properties.setProperty("r", r.toString());
			properties.setProperty("b", b.toString());
			properties.setProperty("g", g.toString());

			properties.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public static void setLookAndFeel(String lookAndFeel) {
		try (OutputStream tryoutput = new FileOutputStream(configFile.getAbsoluteFile());) {
			OutputStream output = new FileOutputStream(configFile.getAbsoluteFile());

			properties.setProperty("lookAndFeel", lookAndFeel);

			properties.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public static String getLookAndFeel() throws IllegalStateException {
		try {
			return properties.getProperty("lookAndFeel");
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	public static void addServerIp(String serverIp) {
		try (OutputStream tryoutput = new FileOutputStream(configFile.getAbsoluteFile());) {
			OutputStream output = new FileOutputStream(configFile.getAbsoluteFile());

			properties.setProperty("serverIps", properties.getProperty("serverIps") + serverIp + ";");

			properties.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public static String getServerIps() throws IllegalStateException {
		try {
			return properties.getProperty("serverIps");
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}

	public static void removeServerIp(String serverIp) throws IllegalStateException {
		try (OutputStream tryoutput = new FileOutputStream(configFile.getAbsoluteFile());) {
			OutputStream output = new FileOutputStream(configFile.getAbsoluteFile());

			properties.setProperty("serverIps", properties.getProperty("serverIps").replace(serverIp + ";", ""));

			properties.store(output, null);
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

}
