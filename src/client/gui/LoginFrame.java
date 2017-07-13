package client.gui;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import client.configuration.Configuration;

public class LoginFrame extends Frame implements ActionListener {

	private JButton login;
	private JFrame loginFrame;
	private JTextField inputName;
	private JComboBox<String> lookAndFeelCombobox;
	private JList<String> serverIpList;
	private JTextField inputNewServerIp;
	private JButton buttonAddNewServerIp;
	private JButton buttonRemoveServerIp;
	private JPanel loginPanel;
	private JPanel serverIpPanel;
	private JPanel lookAndFeelPanel;
	private JPanel addServerIpPanel;
	private DefaultListModel<String> serverIpsListModel;
	private LoginReceiver loginReceiver;
	private JButton userColorButton;
	private JColorChooser colorChooser;
	private boolean toggled = false;
	private Pattern pattern;
	private Matcher matcher;
	private JScrollPane serverIpListScrolPane;
	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	public LoginFrame(LoginReceiver loginReceiver) throws IOException {
		this.loginReceiver = loginReceiver;
		login = new JButton("login");
		loginFrame = new JFrame("Login");
		inputName = new JTextField();
		lookAndFeelCombobox = new JComboBox<String>();
		serverIpList = new JList<String>();
		inputNewServerIp = new JTextField();
		buttonAddNewServerIp = new JButton("+");
		buttonRemoveServerIp = new JButton("-");
		userColorButton = new JButton("Profile Color");

		lookAndFeelCombobox.addItem("Acryl");
		lookAndFeelCombobox.addItem("Seaglass");

		getProperties();
		getServerIps();

		login.addActionListener(this);
		buttonAddNewServerIp.addActionListener(this);
		buttonRemoveServerIp.addActionListener(this);
		userColorButton.addActionListener(this);

		loginFrame.setLayout(new BorderLayout());
		loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		inputName.setBorder(new RoundedCornerBorder());

		serverIpListScrolPane = new JScrollPane(serverIpList);

		loginPanel = new JPanel();
		serverIpPanel = new JPanel();
		lookAndFeelPanel = new JPanel();
		addServerIpPanel = new JPanel();
		loginPanel.setLayout(new BorderLayout());
		loginPanel.add(inputName, CENTER);
		loginPanel.add(login, EAST);
		lookAndFeelPanel.add(userColorButton, BorderLayout.SOUTH);
		lookAndFeelPanel.add(lookAndFeelCombobox, BorderLayout.EAST);
		serverIpPanel.add(serverIpListScrolPane, BorderLayout.NORTH);
		addServerIpPanel.add(inputNewServerIp);
		addServerIpPanel.add(buttonAddNewServerIp);
		addServerIpPanel.add(buttonRemoveServerIp, BorderLayout.SOUTH);
		serverIpPanel.add(addServerIpPanel, BorderLayout.CENTER);

		loginFrame.add(loginPanel, BorderLayout.NORTH);
		loginFrame.add(lookAndFeelPanel, BorderLayout.SOUTH);
		loginFrame.add(serverIpPanel, BorderLayout.CENTER);
		setIcon(loginFrame);

		Color colorFromConfig = Configuration.getColor();

		colorChooser = new JColorChooser(colorFromConfig);
		colorChooser.setPreviewPanel(new JPanel());
		setRGBPanel();
		colorChooser.setBorder(null);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Configuration.setColor(colorChooser.getColor());
			}
		});

		inputName.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == '/') {
					e.consume();
				}
			}
		});

		lookAndFeelCombobox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				String lookAndFeel = lookAndFeelCombobox.getSelectedItem().toString();
				updateLookAndFeel(lookAndFeel, loginFrame);
				Configuration.setLookAndFeel(lookAndFeel);
			}
		});

		setLookAndFeelFromConfig();
		

		pattern = Pattern.compile(IPADDRESS_PATTERN);

	  

		loginFrame.setSize(300, 300);
		centerWindow(loginFrame);
		loginFrame.setVisible(true);
		loginFrame.setResizable(false);

		SwingUtilities.getRootPane(login).setDefaultButton(login);
	}
	
	private void setRGBPanel(){
		AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
		for (AbstractColorChooserPanel accp : panels) {
			if (!accp.getDisplayName().equals("RGB")) {
				colorChooser.removeChooserPanel(accp);
			}
		}
	}

	private void getServerIps() {
		serverIpsListModel = new DefaultListModel<String>();
		serverIpList.setModel(serverIpsListModel);

		Configuration.loadConfiguration();
		String serverIps = Configuration.getServerIps();

		inputNewServerIp.setText("IP-Adress");

		if (serverIps != null) {
			for (String ip : serverIps.split(";")) {
				serverIpsListModel.addElement(ip);
			}
		}

		serverIpList.setSelectedIndex(0);
	}

	private void getProperties() {
		Configuration.loadConfiguration();
		String usernameFromFile = Configuration.getUsername();

		if (usernameFromFile != null) {
			inputName.setText(usernameFromFile);
		}
	}

	protected void toggleColorChooser() {
		if (toggled) {
			loginFrame.setSize(300, 300);
			loginFrame.remove(colorChooser);
		} else {
			loginFrame.setSize(900, 370);
			colorChooser.setBounds(userColorButton.getX(), userColorButton.getY() + 20, 600, 300);
			colorChooser.setVisible(true);
			loginFrame.add(colorChooser, BorderLayout.EAST);
		}
		toggled = !toggled;
		loginFrame.validate();
		loginFrame.repaint();
	}

	private void setLookAndFeelFromConfig() {
		String lookAndFeelFromConfig = Configuration.getLookAndFeel();
		if (lookAndFeelFromConfig != null) {
			lookAndFeelCombobox.setSelectedItem(lookAndFeelFromConfig);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == login) {
			if (inputName.getText().length() >= 1 && inputName.getText().length() <= 18) {
				try {
					loginReceiver.login(inputName.getText(), serverIpList.getSelectedValue());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		if (e.getSource() == buttonAddNewServerIp) {
			String newServerIp = inputNewServerIp.getText();
			if (validate(newServerIp)) {
				Configuration.loadConfiguration();
				Configuration.addServerIp(newServerIp);
				getServerIps();
			}
		}

		if (e.getSource() == buttonRemoveServerIp) {
			String serverIpToRemove = serverIpsListModel.get(serverIpList.getSelectedIndex());
			if (serverIpToRemove != null) {
				Configuration.loadConfiguration();
				Configuration.removeServerIp(serverIpToRemove);
				getServerIps();
			}
		}

		if (e.getSource() == userColorButton) {
			toggleColorChooser();
		}

	}

	public JFrame getFrame() {
		return loginFrame;
	}

	public boolean validate(final String ip) {
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public void updateLookAndFeel(String lookAndFeel, JFrame frame) {
		try {
			String lookAndFeelToSet = null;
			switch (lookAndFeel) {
			case "Seaglass":
				lookAndFeelToSet = "com.seaglasslookandfeel.SeaGlassLookAndFeel";
				break;
			case "Acryl":
				lookAndFeelToSet = "com.jtattoo.plaf.acryl.AcrylLookAndFeel";
				break;
			default:
				lookAndFeelToSet = "com.seaglasslookandfeel.SeaGlassLookAndFeel";
				break;
			}

			UIManager.setLookAndFeel(lookAndFeelToSet);
			SwingUtilities.updateComponentTreeUI(frame);

			setRGBPanel();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Your chosen LAF isn't available");
		}
	}

}
