package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

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

import protocol.TextMessage;
import protocol.TypingState;
import protocol.User;

public class ChatFrame extends BasicFrame implements ActionListener {

	private static double antiSpamTime = 2;
	private JComboBox<String> receiverList;
	private JTextField inputMessage;
	private JPanel chatPanel;
	private JPanel statusPanel;
	private JPanel emojiPanel;
	private JFrame chatFrame;
	private JList<User> userStates;
	private JButton send;
	private JCheckBox autoScrollCheckbox;
	private JLabel typingLabel;
	private JButton settingsButton;
	private TypingLabelController tlp;
	private AutoreplaceingEmojiFeed feed;
	private JPanel topPanel;
	private JPanel buttonPanel;
	private JPanel chatActionPanel;
	private JScrollPane scrollPane;
	private DefaultListModel<User> activeUsersListModel;
	private Style nameStyle;
	private Style dateStyle;
	private Style messageStyle;
	private Style breakStyle;
	private Style pointStyle;
	private DefaultListModel<ImageIcon> favListModelFromFile;
	private DefaultListModel<ImageIcon> favEmojiListModel;
	private JList<ImageIcon> favEmojiList;
	private JScrollPane favEmojiScrollPane;
	private TextMessage lastMessage;
	private JButton reconnectButton;
	private String userName;
	private Color userColor;
	private ChatFrameActionReceiver actionReceiver;
	private Style leftAlign;
	private Style rightAlign;

	public ChatFrame(String userName, Color userColor, final ChatFrameActionReceiver actionReceiver) {
		feed = new AutoreplaceingEmojiFeed();
		initStyles();
		this.userName = userName;
		this.userColor = userColor;
		this.actionReceiver = actionReceiver;

		activeUsersListModel = new DefaultListModel<User>();
		receiverList = new JComboBox<String>();

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

		topPanel = new JPanel();
		topPanel.add(autoScrollCheckbox);
		typingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
		topPanel.add(typingLabel);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(send);

		inputMessage.setBorder(new RoundedCornerBorder());
		chatActionPanel = new JPanel();
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
				actionReceiver.typing();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				actionReceiver.typing();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				actionReceiver.typing();
			}

		});

		chatFrame.add(emojiPanel, BorderLayout.WEST);
		chatFrame.add(chatPanel, BorderLayout.CENTER);
		chatFrame.add(statusPanel, BorderLayout.EAST);

		setIcon(chatFrame);

		chatFrame.setSize(600, 500);
		centerWindow(chatFrame);
		SwingUtilities.updateComponentTreeUI(chatFrame);

		chatFrame.setVisible(true);

		SwingUtilities.getRootPane(send).setDefaultButton(send);

		createEmojiBar();
	}

	public void initStyles() {
		leftAlign = feed.addStyle("leftAlign", null);
		StyleConstants.setAlignment(leftAlign, StyleConstants.ALIGN_LEFT);

		rightAlign = feed.addStyle("rightAlign", null);
		StyleConstants.setAlignment(rightAlign, StyleConstants.ALIGN_RIGHT);

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

	private void createEmojiBar() {
		favListModelFromFile = readFavEmojiFile();

		if (favListModelFromFile == null) {
			favEmojiListModel = new DefaultListModel<ImageIcon>();
		} else {
			if (favListModelFromFile.size() > 0) {
				favEmojiListModel = favListModelFromFile;
			} else {
				favEmojiListModel = new DefaultListModel<ImageIcon>();
			}
		}

		favEmojiList = new JList<ImageIcon>(favEmojiListModel);
		favEmojiScrollPane = new JScrollPane(favEmojiList);
		favEmojiList.setModel(favEmojiListModel);

		emojiPanel.setLayout(new BorderLayout());

		if (favListModelFromFile == null) {
			favListModelFromFile = new DefaultListModel<ImageIcon>();
			favEmojiList.setModel(favListModelFromFile);
		}

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				ImageIcon selectedImage = favEmojiList.getSelectedValue();
				String iconfilename = "<" + selectedImage.getDescription() + ">";
				inputMessage.setText(inputMessage.getText() + iconfilename);
			}
		};

		favEmojiScrollPane.setPreferredSize(new Dimension(70, 0));

		settingsButton.setIcon(FileToImageConverter.fileToImageIcon(new File("2699.png"), 15, 15));

		favEmojiList.addMouseListener(mouseListener);
		emojiPanel.add(settingsButton, BorderLayout.NORTH);
		emojiPanel.add(favEmojiScrollPane, BorderLayout.CENTER);
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

	public void scrollToBottom() {
		feed.setCaretPosition(feed.getDocument().getLength());
	}

	public void appendName(String str, Boolean alignRight) throws BadLocationException, IOException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), str + "  ", nameStyle);

		align(alignRight);
	}

	public void appendDate(String str, Boolean alignRight) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), str + "  \n", dateStyle);

		align(alignRight);
	}

	public void appendMessage(String str, Boolean alignRight) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), str, messageStyle);

		align(alignRight);
	}

	public void appendPointWithColor(String name, Color color, Boolean alignRight) throws BadLocationException {
		StyledDocument document = (StyledDocument) feed.getDocument();
		document.insertString(document.getLength(), "\n", breakStyle);
		StyleConstants.setForeground(pointStyle, color);
		document.insertString(document.getLength(), "• ", pointStyle);

		align(alignRight);
	}

	public void clientInfoReceived(String message) {
		try {
			appendName("\n\n" + message, false);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void align(Boolean alignRight) {
		StyledDocument document = (StyledDocument) feed.getDocument();
		if (alignRight) {
			document.setParagraphAttributes(document.getLength(), 1, rightAlign, false);
		} else {
			document.setParagraphAttributes(document.getLength(), 1, leftAlign, false);
		}
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
						if (inputMessage.getText().contains("http://") || inputMessage.getText().contains("https://")) {
							wordTooLong = false;
						} else {
							wordTooLong = true;
						}
					}
				}
				if (!wordTooLong) {
					actionReceiver.messageSent(inputMessage.getText() + "\n", userColor,
							receiverList.getSelectedItem().toString());
					inputMessage.setText("");
					antiSpamTime = System.currentTimeMillis();
				}
			}
		}

		if (e.getSource() == settingsButton) {
			actionReceiver.settingsFrameCalled();
		}

		if (e.getSource() == reconnectButton) {
			actionReceiver.reconnectWasCalled();
		}

	}

	public void messageReceived(TextMessage message) {
		try {
			Boolean alignRight;
			if (userName.equals(message.getSenderName())) {
				alignRight = true;
			} else {
				alignRight = false;
			}

			if (lastMessage != null && lastMessage.getSenderName().equals(message.getSenderName())
					&& lastMessage.getColor().equals(message.getColor())
					&& lastMessage.getReceiverName().equals(message.getReceiverName())) {
				appendMessage(message.getMessage(), alignRight);
			} else {
				appendPointWithColor(message.getSenderName(), message.getColor(), alignRight);
				if (!message.getReceiverName().equals("")) {
					appendName(message.getSenderName() + " @ " + message.getReceiverName(), alignRight);
				} else {
					appendName(message.getSenderName(), alignRight);
				}

				appendDate(message.getTime(), alignRight);

				appendMessage(message.getMessage(), alignRight);
			}
		} catch (BadLocationException | IOException e) {
			System.out.println("Message could not be appended!");
		}

		if (autoScrollCheckbox.isSelected() == true) {
			scrollToBottom();
		}

		chatFrame.toFront();
		lastMessage = message;
		typingLabel.setText("");
	}

	public void activeUsersReceived(List<User> activeUsers) {
		String selectedUser = null;
		try {
			selectedUser = receiverList.getSelectedItem().toString();
			System.out.println(selectedUser);
		} catch (Exception e) {
			System.out.println("No selected user");
		}

		activeUsersListModel.clear();
		receiverList.removeAllItems();
		receiverList.addItem("");

		for (User user : activeUsers) {
			activeUsersListModel.addElement(user);
		}

		for (User user : activeUsers) {
			if (!user.getName().equals(userName)) {
				receiverList.addItem(user.getName());
			}
		}

		if (selectedUser != null) {
			receiverList.setSelectedItem(selectedUser);
		}

		if (autoScrollCheckbox.isSelected() == true) {
			scrollToBottom();
		}

	}

	public void typingStateReceived(TypingState typingState) {
		tlp.receivedTypingInfos(typingState);
	}

	public void clientShutdownMessage() {
		chatActionPanel.remove(inputMessage);
		chatActionPanel.remove(send);
		chatActionPanel.remove(receiverList);
		reconnectButton = new JButton("Reconnect");
		chatActionPanel.add(reconnectButton, BorderLayout.CENTER);
		reconnectButton.addActionListener(this);
	}

	public void logsReceived(List<TextMessage> logs) {
		if (logs != null) {
			for (TextMessage message : logs) {
				messageReceived(message);
			}
		}
	}

	public DefaultListModel<ImageIcon> getFavEmojiListModel() {
		return favEmojiListModel;
	}

	public JScrollPane getFavEmojiScrollPane() {
		return favEmojiScrollPane;
	}

	public AutoreplaceingEmojiFeed getFeed() {
		return feed;
	}

	public JList<ImageIcon> getFavEmojiList() {
		return favEmojiList;
	}

	public JFrame getFrame() {
		return chatFrame;
	}

	public void shutdown() {
		tlp.shutdown();
		chatFrame.dispose();
	}
}
