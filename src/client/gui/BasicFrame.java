package client.gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class BasicFrame {

	public void centerWindow(JFrame frame) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
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

}