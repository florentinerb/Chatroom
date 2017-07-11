package client.gui;

import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

public class AllEmojiListConverter implements Runnable {
	private AutoreplaceingEmojiFeed feed;
	private ConversionFinishedNotifier cfn;

	public AllEmojiListConverter(AutoreplaceingEmojiFeed feed, ConversionFinishedNotifier cfn) {
		this.feed = feed;
		this.cfn = cfn;
	}

	@Override
	public void run() {
		DefaultListModel<ImageIcon> allEmojiListModel = new DefaultListModel<ImageIcon>();
		for (File file : feed.getEmojiImages()) {
			ImageIcon ii = FileToImageConverter.fileToImageIcon(file, 30, 30);
			if (ii != null) {
				allEmojiListModel.addElement(ii);
				cfn.stepComplete(feed.getEmojiImages().indexOf(file) + 1);
			}
		}
		cfn.conversionFinished(allEmojiListModel);
	}
}
