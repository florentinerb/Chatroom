package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class SettingsFrame extends BasicFrame implements ActionListener, ConversionFinishedNotifier {

	private JButton removeEmojiButton;
	private JFrame settingsFrame;
	private JPanel settingsPane;
	private JPanel centerPane;
	private DefaultListModel<ImageIcon> allEmojiListModel;
	private JList<ImageIcon> allEmojiList;
	private JProgressBar progressBar;
	private JButton addEmojiButton;
	private JList<ImageIcon> favEmojiList;
	private Thread alecThread;
	private JScrollPane allEmojiScrollPane;
	private DefaultListModel<ImageIcon> favEmojiListModel;
	private JScrollPane favEmojiScrollPane;

	public SettingsFrame(DefaultListModel<ImageIcon> favEmojiListModel, AutoreplaceingEmojiFeed feed) {
		this.favEmojiListModel = favEmojiListModel;
		this.favEmojiList = new JList<ImageIcon>(favEmojiListModel);
		favEmojiScrollPane = new JScrollPane(favEmojiList);
		addEmojiButton = new JButton(">");
		addEmojiButton.addActionListener(this);
		removeEmojiButton = new JButton("<");
		removeEmojiButton.addActionListener(this);
		favEmojiScrollPane.setPreferredSize(new Dimension(70, 0));

		settingsFrame = new JFrame("Settings");
		centerWindow(settingsFrame);
		settingsPane = new JPanel();

		allEmojiList = new JList<ImageIcon>();
		allEmojiScrollPane = new JScrollPane(allEmojiList);
		allEmojiScrollPane.setPreferredSize(new Dimension(70, 0));

		centerPane = new JPanel();
		centerPane.setLayout(new BorderLayout());
		settingsPane.setLayout(new BorderLayout());
		settingsPane.add(allEmojiScrollPane, BorderLayout.WEST);
		settingsPane.add(favEmojiScrollPane, BorderLayout.EAST);
		centerPane.add(addEmojiButton, BorderLayout.WEST);
		centerPane.add(removeEmojiButton, BorderLayout.EAST);
		settingsPane.add(centerPane, BorderLayout.CENTER);

		settingsFrame.setVisible(true);
		settingsFrame.setSize(250, 50);
		settingsFrame.setResizable(false);
		settingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		AllEmojiListConverter aelc = new AllEmojiListConverter(feed, this);
		if (alecThread == null && allEmojiListModel == null) {

			progressBar = new JProgressBar(0, feed.getEmojiImages().size());
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			settingsFrame.add(progressBar, BorderLayout.NORTH);

			alecThread = new Thread(aelc);
			alecThread.start();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addEmojiButton) {
			for (ImageIcon imageIcon : allEmojiList.getSelectedValuesList()) {
				if (!favEmojiListModel.contains(imageIcon)) {
					favEmojiListModel.addElement(imageIcon);
					saveListToFile();
					if (favEmojiListModel.size() > 0) {
						favEmojiList.setModel(favEmojiListModel);
					} else {
						favEmojiList.setModel(allEmojiListModel);
					}
				}
			}
		}

		if (e.getSource() == removeEmojiButton) {
			for (ImageIcon imageIcon : favEmojiList.getSelectedValuesList()) {
				favEmojiListModel.remove(favEmojiListModel.indexOf(imageIcon));
				saveListToFile();
				favEmojiList.setModel(favEmojiListModel);
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

	@Override
	public void conversionFinished(DefaultListModel<ImageIcon> allEmojiListModel) {
		this.allEmojiListModel = allEmojiListModel;
		settingsFrame.remove(progressBar);
		allEmojiList.setModel(allEmojiListModel);
		settingsFrame.add(settingsPane);
		settingsFrame.setSize(250, 400);
	}

	@Override
	public void stepComplete(int number) {
		progressBar.setValue(number);
	}

	public JFrame getFrame() {
		return settingsFrame;
	}

}
