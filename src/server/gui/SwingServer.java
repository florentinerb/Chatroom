package server.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import server.Server;

class SwingServer implements ActionListener {
	private Server server;
	private JButton start = new JButton("start");
	private JButton stop = new JButton("stop");
	private JLabel info = new JLabel();
	private JFrame controlFrame = new JFrame();
	private TrayIcon trayIcon;
	private PopupMenu menu;
	
	public SwingServer() throws IOException{
		createControllFrame();
	}
	
	private void createControllFrame() throws IOException {
		if (SystemTray.isSupported()) {
			showSystemTrayGUI();
		} else {
			showSwingGUI();
		}
	}
	
	private void showSystemTrayGUI() throws IOException {
		SystemTray sysTray = SystemTray.getSystemTray();

		Image systemTrayImage = ImageIO
				.read(getClass().getClassLoader().getResource("client/gui/emojiImages/274e.png"));
		Image resizedSystemTrayImage = getScaledIcon(systemTrayImage);

		menu = new PopupMenu();
		MenuItem start = new MenuItem("Start");
		menu.add(start);

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start();
				try {
					Image systemTrayImage = ImageIO
							.read(getClass().getClassLoader().getResource("client/gui/emojiImages/23fa.png"));
					trayIcon.setImage(getScaledIcon(systemTrayImage));
					MenuItem stop = new MenuItem("Stop");
					menu.remove(0);

					stop.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							stop();
						}
					});

					menu.add(stop);
					trayIcon.setPopupMenu(menu);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		

		trayIcon = new TrayIcon(resizedSystemTrayImage, "JChatServer", menu);

		try {
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void showSwingGUI() {
		controlFrame.setTitle("Chat Server");
		controlFrame.setLayout(new BorderLayout());
		controlFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		start.setSize(30, 10);
		start.addActionListener(this);

		stop.setSize(30, 10);
		stop.addActionListener(this);

		controlFrame.add(info, BorderLayout.CENTER);
		controlFrame.add(start, BorderLayout.EAST);
		controlFrame.add(stop, BorderLayout.WEST);

		List<Image> icons = new ArrayList<Image>();
		try {
			icons.add(ImageIO.read(getClass().getClassLoader().getResource("client/gui/emojiImages/2709.png")));
			controlFrame.setIconImages(icons);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		controlFrame.setLocation(dim.width / 2 - controlFrame.getSize().width / 2,
				dim.height / 2 - controlFrame.getSize().height / 2);

		controlFrame.setSize(255, 70);
		controlFrame.setVisible(true);
		controlFrame.setResizable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start){
			start();
		}
		
		if(e.getSource() == stop){
			stop();
		}
		
	}

	private void start(){
		try {
			if(server == null){
				server = new Server();
				info.setText("Server is running!");
				
			} else {
				start.setText("Server running");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void stop() {
		try {
			server.stop();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		info.setText("Server stopped!");
	}

	private Image getScaledIcon(Image srcImg) {
		int w = 16;
		int h = 16;
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

}
