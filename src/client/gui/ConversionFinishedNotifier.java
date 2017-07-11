package client.gui;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

public interface ConversionFinishedNotifier {
	public void conversionFinished(DefaultListModel<ImageIcon> allEmojiListModel);

	public void stepComplete(int number);
}
