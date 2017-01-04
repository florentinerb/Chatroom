package client.gui;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import client.Client;
import client.MessageReceiver;
import client.configuration.Configuration;
import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;

class SwingGUI extends JFrame implements MessageReceiver, ActionListener, Runnable, LockMessageReceiver {

	private Client client;
	private String userName;
	private JTextField inputMessage;
	private JButton login;
	private JFrame loginFrame;
	private JPanel chatPanel;
	private JPanel statusPanel;
	private JPanel emojiPanel;
	private JFrame chatFrame;
	private JTextField inputName;
	private AutoreplaceingEmojiFeed feed;
	private DefaultListModel<User> activeUsersListModel;
	private JList<User> userStates;
	private JButton send;
	private JCheckBox autoScrollCheckbox;
	private JLabel typingLabel;
	private JScrollPane scrollPane;
	private double antiSpamTime;
	private Style nameStyle;
	private Style messageStyle;
	private Style dateStyle;
	private Style breakStyle;
	private Style pointStyle;
	private List<TextMessage> logs;
	private TextMessage lastMessage;
	private TypingLabelController tlp;
	private Color userColor;
	private final String IMAGEPATH = "client/gui/emojiImages";
	private JComboBox<String> receiverList;
	private JButton settingsButton;
	private JFrame settingsFrame;
	private JPanel settingsPane;
	private JScrollPane emojiScrollPane;
	private JScrollPane allEmojiScrollPane;
	private JList<ImageIcon> emojiList;
	private JButton addEmojiButton;
	private JList<ImageIcon> allEmojiList;
	private JList<ImageIcon> favEmojiList;
	private DefaultListModel<ImageIcon> favEmojiListModel;
	private JButton removeEmojiButton;
	private DefaultListModel<ImageIcon> allEmojiListModel;
	private JScrollPane favEmojiScrollPane;
	private JPanel centerPane;

	public SwingGUI() throws UnknownHostException, IOException {
		try {
			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
		} catch (Exception e) {
			System.out.println("Seaglass isn't available");
		}
		this.createLoginWindow();
	}

	public void initStyles() {
		nameStyle = feed.addStyle("nameStyle", null);
		StyleConstants.setForeground(nameStyle, Color.black);
		StyleConstants.setFontSize(nameStyle, 15);
		StyleConstants.setBold(nameStyle, true);
		StyleConstants.setAlignment(nameStyle, StyleConstants.ALIGN_LEFT);

		dateStyle = feed.addStyle("dateStyle", null);
		StyleConstants.setForeground(dateStyle, Color.gray);
		StyleConstants.setFontSize(dateStyle, 11);
		StyleConstants.setAlignment(dateStyle, StyleConstants.ALIGN_LEFT);

		messageStyle = feed.addStyle("messageStyle", null);
		StyleConstants.setForeground(messageStyle, Color.black);
		StyleConstants.setFontSize(messageStyle, 13);
		StyleConstants.setAlignment(messageStyle, StyleConstants.ALIGN_LEFT);

		breakStyle = feed.addStyle("breakStyle", null);
		StyleConstants.setForeground(breakStyle, Color.black);
		StyleConstants.setFontSize(breakStyle, 5);

		pointStyle = feed.addStyle("pointStyle", null);
		StyleConstants.setFontSize(pointStyle, 20);
	}

	private void createLoginWindow() throws IOException {
		login = new JButton("login");
		loginFrame = new JFrame("Login");
		inputName = new JTextField();
		feed = new AutoreplaceingEmojiFeed();
		initStyles();

		getProperties();

		login.addActionListener(this);

		loginFrame.setLayout(new BorderLayout());
		loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		inputName.setBorder(new RoundedCornerBorder());

		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BorderLayout());
		loginPanel.add(inputName, CENTER);
		loginPanel.add(login, EAST);

		loginFrame.add(loginPanel, CENTER);
		setIcon(loginFrame);

		inputName.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == '/') {
					e.consume();
				}
			}
		});

		loginFrame.setSize(300, 60);
		centerWindow(loginFrame);
		loginFrame.setVisible(true);
		// loginFrame.setResizable(false);

		SwingUtilities.getRootPane(login).setDefaultButton(login);

		activeUsersListModel = new DefaultListModel<User>();
		receiverList = new JComboBox<String>();
	}

	private void createChatWindow() {
		inputMessage = new JTextField();
		chatPanel = new JPanel();
		statusPanel = new JPanel();
		emojiPanel = new JPanel();
		chatFrame = new JFrame();
		userStates = new JList<User>(activeUsersListModel);
		send = new JButton("send");
		autoScrollCheckbox = new JCheckBox("AutoScroll", true);
		typingLabel = new JLabel("");
		settingsButton = new JButton();

		tlp = new TypingLabelController(typingLabel, userName);
		Thread lockListenerThread = new Thread(this);
		lockListenerThread.start();

		chatFrame.setTitle("Chat");
		chatFrame.setLayout(new BorderLayout());
		chatFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		chatFrame.setMinimumSize(new Dimension(445, 200));

		send.setSize(30, 10);
		send.addActionListener(this);
		settingsButton.addActionListener(this);
		autoScrollCheckbox.addActionListener(this);
		feed.setEditable(false);
		feed.setBackground(Color.white);

		JPanel topPanel = new JPanel();
		topPanel.add(autoScrollCheckbox);
		typingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
		topPanel.add(typingLabel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(send);

		inputMessage.setBorder(new RoundedCornerBorder());
		JPanel chatActionPanel = new JPanel();
		chatActionPanel.setLayout(new BorderLayout());
		chatActionPanel.add(receiverList, BorderLayout.WEST);
		chatActionPanel.add(inputMessage, BorderLayout.CENTER);
		chatActionPanel.add(send, BorderLayout.EAST);

		scrollPane = new JScrollPane(feed, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		chatPanel.setLayout(new BorderLayout());
		chatPanel.add(topPanel, BorderLayout.NORTH);
		chatPanel.add(scrollPane, BorderLayout.CENTER);
		chatPanel.add(chatActionPanel, BorderLayout.SOUTH);

		JLabel onlineLabel = new JLabel("Online");
		onlineLabel.setFont(new Font("Arial", Font.BOLD, 13));
		Border border = onlineLabel.getBorder();
		Border margin = new EmptyBorder(5, 5, 5, 5);
		onlineLabel.setBorder(new CompoundBorder(border, margin));

		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(onlineLabel, BorderLayout.NORTH);
		statusPanel.add(userStates, BorderLayout.CENTER);

		JLabel emojiLabel = new JLabel("Emojis");
		emojiLabel.setFont(new Font("Arial", Font.BOLD, 13));
		Border borderEmoji = emojiLabel.getBorder();
		Border marginEmoji = new EmptyBorder(5, 5, 5, 5);
		onlineLabel.setBorder(new CompoundBorder(borderEmoji, marginEmoji));

		inputMessage.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				typing();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				typing();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				typing();
			}

		});

		createEmojiBar();

		chatFrame.add(emojiPanel, BorderLayout.WEST);
		chatFrame.add(chatPanel, BorderLayout.CENTER);
		chatFrame.add(statusPanel, BorderLayout.EAST);

		setIcon(chatFrame);

		chatFrame.setSize(500, 433);
		centerWindow(chatFrame);
		chatFrame.setVisible(true);

		SwingUtilities.getRootPane(send).setDefaultButton(send);

		if (logs != null) {
			for (TextMessage message : logs) {
				try {
					messageReceived(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void createEmojiBar() {
		DefaultListModel<ImageIcon> favListModelFromFile = readFavEmojiFile();

		if (favListModelFromFile == null) {
			favEmojiListModel = new DefaultListModel<ImageIcon>();
		} else {
			if (favListModelFromFile.size() > 0) {
				favEmojiListModel = favListModelFromFile;
			} else {
				favEmojiListModel = new DefaultListModel<ImageIcon>();
			}
		}
		allEmojiList = new JList<ImageIcon>();
		allEmojiScrollPane = new JScrollPane(allEmojiList);

		favEmojiList = new JList<ImageIcon>(favEmojiListModel);
		favEmojiScrollPane = new JScrollPane(favEmojiList);
		favEmojiList.setModel(favEmojiListModel);
		addEmojiButton = new JButton(">");
		addEmojiButton.addActionListener(this);

		emojiPanel.setLayout(new BorderLayout());

		emojiList = new JList<ImageIcon>();
		emojiScrollPane = new JScrollPane(emojiList);
		
		allEmojiListModel = new DefaultListModel<ImageIcon>();
		for (File file : feed.getEmojiImages()) {
			ImageIcon ii = fileToImageIcon(file, 30, 30);
			if (ii != null) {
				allEmojiListModel.addElement(ii);
			}
		}

		if (favListModelFromFile == null) {
			emojiList.setModel(allEmojiListModel);
		} else {
			if (favListModelFromFile.size() > 0) {
				emojiList.setModel(favEmojiListModel);
			} else {
				emojiList.setModel(allEmojiListModel);
			}

		}

		emojiScrollPane.setPreferredSize(new Dimension(70, 0));
		allEmojiScrollPane.setPreferredSize(new Dimension(70, 0));

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ImageIcon selectedImage = emojiList.getSelectedValue();
				String iconfilename = "<" + selectedImage.getDescription() + ">";
				inputMessage.setText(inputMessage.getText() + iconfilename);
			}
		};

		settingsButton.setIcon(fileToImageIcon(new File("2699.png"), 15, 15));

		emojiList.addMouseListener(mouseListener);
		emojiPanel.add(settingsButton, BorderLayout.NORTH);
		emojiPanel.add(emojiScrollPane, BorderLayout.CENTER);
	}

	private ImageIcon fileToImageIcon(File file, Integer w, Integer h) {
		if (file.getName().contains(".png")) {
			try {
				URL inputUrl = getClass().getClassLoader().getResource(IMAGEPATH + "/" + file.getName());
				Image image;
				image = ImageIO.read(inputUrl);

				BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = resizedImg.createGraphics();

				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawImage(image, 0, 0, w, h, null);
				g2.dispose();
				ImageIcon ii = new ImageIcon(resizedImg);
				ii.setDescription(file.getName().replace(".png", ""));
				return ii;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private DefaultListModel<ImageIcon> readFavEmojiFile() {
		ObjectInputStream oos;
		DefaultListModel<ImageIcon> favListModelFromFile = null;
		try {
			oos = new ObjectInputStream(
					new FileInputStream(new File(System.getProperty("user.home") + "/Chat/favEmojis.config")));
			favListModelFromFile = (DefaultListModel<ImageIcon>) oos.readObject();
			oos.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("favEmojiConfig could not be found!");
		}

		if (favListModelFromFile == null) {
			return null;
		} else {
			return favListModelFromFile;
		}
	}

	private void createSettingsFrame() {
		removeEmojiButton = new JButton("<");
		removeEmojiButton.addActionListener(this);
		favEmojiScrollPane.setPreferredSize(new Dimension(70, 0));

		settingsFrame = new JFrame("Settings");
		centerWindow(settingsFrame);
		settingsPane = new JPanel();
		
		centerPane = new JPanel();
		centerPane.setLayout(new BorderLayout());
		settingsPane.setLayout(new BorderLayout());
		allEmojiList.setModel(allEmojiListModel);
		settingsPane.add(allEmojiScrollPane, BorderLayout.WEST);
		settingsPane.add(favEmojiScrollPane, BorderLayout.EAST);
		centerPane.add(addEmojiButton, BorderLayout.WEST);
		centerPane.add(removeEmojiButton, BorderLayout.EAST);
		settingsPane.add(centerPane, BorderLayout.CENTER);

		settingsFrame.add(settingsPane);

		settingsFrame.setVisible(true);
		settingsFrame.setSize(250, 400);
		settingsFrame.setResizable(false);
		settingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void scrollToBottom() {
		feed.setCaretPosition(feed.getDocument().getLength());
	}

	private void getProperties() {
		Configuration.loadConfiguration();
		String usernameFromFile = Configuration.getUsername();

		getPropertiesColor();

		if (usernameFromFile != null) {
			inputName.setText(usernameFromFile);
		}
	}

	private void getPropertiesColor() {
		Configuration.loadConfiguration();
		Color color = Configuration.getColor();

		if (color != null) {
			userColor = color;
		} else {
			Configuration.generateColor();
			userColor = Configuration.getColor();
		}
	}

	private void login() throws ClassNotFoundException, BadLocationException, IOException {
		userName = inputName.getText();

		try {
			Configuration.setUsername(userName);
		} catch (Exception e1) {
			System.out.println("File doesn't exist");
		}

		try {
			client = new Client(userName, this, userColor);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Kann nicht auf den Server verbinden.");
			appendName(
					"*Verbindung konnte nicht hergestellt werden. Sie können leider keine Nachrichten versenden oder empfangen.*");
		}

	}

	@Override
	public void messageReceived(TextMessage message) throws IOException {
		try {
			if (lastMessage != null && lastMessage.getSenderName().equals(message.getSenderName())
					&& lastMessage.getColor().equals(message.getColor())
					&& lastMessage.getReceiverName().equals(message.getReceiverName())) {
				appendMessage(message.getMessage());
			} else {
				appendPointWithNameGeneratedColor(message.getSenderName(), message.getColor());
				if (!message.getReceiverName().equals("")) {
					appendName(message.getSenderName() + " @ " + message.getReceiverName());
				} else {
					appendName(message.getSenderName());
				}
				appendDate(message.getTime());
				appendMessage(message.getMessage());
			}
		} catch (BadLocationException e) {
			System.out.println("Message could not be appended!");
		}

		if (autoScrollCheckbox.isSelected() == true) {
			scrollToBottom();
		}

		chatFrame.toFront();
		lastMessage = message;
		typingLabel.setText("");
	}

	@Override
	public void logsReceived(List<TextMessage> logs) {
		this.logs = logs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == autoScrollCheckbox) {
			if (autoScrollCheckbox.isSelected() == true) {
				scrollToBottom();
			}
		}

		if (e.getSource() == send) {
			Double timeDifference = System.currentTimeMillis() - antiSpamTime;
			if (inputMessage.getText().length() > 0 && timeDifference > 1000) {
				Boolean wordTooLong = false;
				for (String word : inputMessage.getText().split(" ")) {
					if (word.length() > 60) {
						wordTooLong = true;
					}
				}
				if (!wordTooLong) {
					client.messageSent(inputMessage.getText() + "\n", userColor,
							receiverList.getSelectedItem().toString());
					inputMessage.setText("");
					antiSpamTime = System.currentTimeMillis();
				}
			}
		}

		if (e.getSource() == login) {
			if (inputName.getText().length() >= 1 && inputName.getText().length() <= 18) {
				try {
					login();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				loginFrame.dispose();
				createChatWindow();
			}
		}

		if (e.getSource() == settingsButton) {
			if (settingsFrame == null || !settingsFrame.isVisible()) {
				createSettingsFrame();
			}
		}

		if (e.getSource() == addEmojiButton) {
			for (ImageIcon imageIcon : allEmojiList.getSelectedValuesList()) {
				if (!favEmojiListModel.contains(imageIcon)) {
					favEmojiListModel.addElement(imageIcon);
					saveListToFile();
					if (favEmojiListModel.size() > 0) {
						emojiList.setModel(favEmojiListModel);
					} else {
						emojiList.setModel(allEmojiListModel);
					}
				}
			}
		}

		if (e.getSource() == removeEmojiButton) {
			for (ImageIcon imageIcon : favEmojiList.getSelectedValuesList()) {
				favEmojiListModel.remove(favEmojiListModel.indexOf(imageIcon));
				saveListToFile();
				if (favEmojiListModel.size() > 0) {
					emojiList.setModel(favEmojiListModel);
				} else {
					emojiList.setModel(allEmojiListModel);
				}
			}
		}
	}

	private void saveListToFile() {
		try {
			RandomAccessFile favEmojisFile = new RandomAccessFile(
					System.getProperty("user.home") + "/Chat/favEmojis.config", "rw");
			favEmojisFile.close();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File(System.getProperty("user.home") + "/Chat/favEmojis.config")));
			oos.writeObject(favEmojiListModel);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void appendName(String str) throws BadLocationException, IOException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), str + "  ", nameStyle);
	}

	private void appendDate(String str) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), str + "  ", dateStyle);
	}

	private void appendMessage(String str) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), "\n", breakStyle);
		document.insertString(document.getLength(), str, messageStyle);

	}

	private void appendPointWithNameGeneratedColor(String name, Color color) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), "\n", breakStyle);
		StyleConstants.setForeground(pointStyle, color);
		document.insertString(document.getLength(), "• ", pointStyle);
	}

	@Override
	public void activeUsersReceived(List<User> activeUsers) {
		activeUsersListModel.clear();
		receiverList.removeAllItems();
		receiverList.addItem("");

		for (User user : activeUsers) {
			activeUsersListModel.addElement(user);
		}

		for (User user : activeUsers) {
			if (!user.getName().equals(userName))
				receiverList.addItem(user.getName());
		}

	}

	public void setIcon(JFrame frame) {
		List<Image> icons = new ArrayList<Image>();
		try {
			icons.add(ImageIO.read(getClass().getClassLoader().getResource("client/gui/emojiImages/270d-1f3fc.png")));
			frame.setIconImages(icons);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void centerWindow(JFrame frame) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
	}

	@Override
	public void run() {
		new LockListener(this);
	}

	@Override
	public void onLock() {
		client.userStateSend(true);
	}

	@Override
	public void onUnlock() {
		client.userStateSend(false);
	}

	private void typing() {
		client.typingStateSend(new TypingState(userName));
	}

	@Override
	public void typingStateReceived(TypingState typingState) {
		tlp.receivedTypingInfos(typingState);
	}

}