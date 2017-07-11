package client.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class FileToImageConverter {

	private static final String IMAGEPATH = "client/gui/emojiImages";
	private static SwingGUI gui;

	public FileToImageConverter(SwingGUI gui) {
		FileToImageConverter.gui = gui;
	}

	public static ImageIcon fileToImageIcon(File file, Integer w, Integer h) {
		if (file.getName().contains(".png")) {
			try {
				URL inputUrl = gui.getClass().getClassLoader().getResource(IMAGEPATH + "/" + file.getName());
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
}
