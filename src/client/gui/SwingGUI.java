package client.gui;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;

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
import java.io.IOException;
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
	private JTextField inputMessage = new JTextField();
	private JButton login = new JButton("login");
	private JFrame loginFrame = new JFrame("Login");
	private JPanel chatPanel = new JPanel();
	private JPanel statusPanel = new JPanel();
	private JPanel emojiPanel = new JPanel();
	private JFrame chatFrame = new JFrame();
	private JTextField inputName = new JTextField();
	private AutoreplaceingEmojiFeed feed = new AutoreplaceingEmojiFeed();
	private DefaultListModel<User> activeUsersListModel = new DefaultListModel<User>();
	private JList<User> userStates = new JList<User>(activeUsersListModel);
	private JButton send = new JButton("send");
	private JCheckBox autoScrollCheckbox = new JCheckBox("AutoScroll", true);
	private JLabel typingLabel = new JLabel("");
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
	private JComboBox<String> receiverList = new JComboBox<String>();

	public SwingGUI() throws UnknownHostException, IOException {
		try {
			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
		} catch (Exception e) {
			System.out.println("Seaglass isn't available");
		}
		this.createLoginWindow();

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
		getProperties();

		login.addActionListener(this);

		loginFrame.setLayout(new BorderLayout());
		loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		inputName.setBorder(new RoundedCornerBorder());
		login.setBorder(new RoundedCornerBorder());

		JPanel loginPanel = new JPanel();
		loginPanel.setLayout(new BorderLayout());
		loginPanel.add(inputName, CENTER);
		loginPanel.add(login, EAST);

		loginFrame.add(loginPanel, NORTH);
		setIcon(loginFrame);

		inputName.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (c == '/') {
					e.consume();
				}
			}
		});

		loginFrame.setSize(300, 54);
		centerWindow(loginFrame);
		loginFrame.setVisible(true);
		loginFrame.setResizable(false);

		SwingUtilities.getRootPane(login).setDefaultButton(login);
	}

	private void createChatWindow() {

		tlp = new TypingLabelController(typingLabel, userName);
		Thread lockListenerThread = new Thread(this);
		lockListenerThread.start();

		chatFrame.setTitle("Chat");
		chatFrame.setLayout(new BorderLayout());
		chatFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		chatFrame.setMinimumSize(new Dimension(445, 200));

		send.setSize(30, 10);
		send.addActionListener(this);
		autoScrollCheckbox.addActionListener(this);
		feed.setEditable(false);

		JPanel topPanel = new JPanel();
		topPanel.add(autoScrollCheckbox);
		typingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
		topPanel.add(typingLabel);

		send.setBorder(new RoundedCornerBorder());
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
		emojiPanel.setLayout(new BorderLayout());

		DefaultListModel<ImageIcon> listModel = new DefaultListModel<ImageIcon>();
		final JList<ImageIcon> emojiList = new JList<ImageIcon>();
		JScrollPane scrollPane = new JScrollPane(emojiList);
		emojiList.setModel(listModel);

		for (File file : feed.getEmojiImages()) {
			ImageIcon ii = fileToImageIcon(file);
			if (ii != null) {
				listModel.addElement(ii);
			}
		}

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ImageIcon selectedImage = emojiList.getSelectedValue();
				String iconfilename = "<" + selectedImage.getDescription() + ">";
				inputMessage.setText(inputMessage.getText() + iconfilename);
			}
		};

		emojiList.addMouseListener(mouseListener);
		emojiPanel.add(scrollPane);
	}

	private ImageIcon fileToImageIcon(File file) {
		if (file.getName().contains(".png")) {
			try {

				URL inputUrl = getClass().getClassLoader().getResource(IMAGEPATH + "/" + file.getName());
				Image image;
				image = ImageIO.read(inputUrl);

				BufferedImage resizedImg = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = resizedImg.createGraphics();

				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawImage(image, 0, 0, 30, 30, null);
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
			System.out.println(message.getTimeNameMessage() + message.getReceiverName());
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