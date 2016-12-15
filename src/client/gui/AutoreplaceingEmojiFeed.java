package client.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

import sun.misc.Launcher;

class AutoreplaceingEmojiFeed extends JTextPane {
	private ImageIcon TEARLAUGH = getImage("1f602.png", false);
	private ImageIcon SHIT = getImage("1f4a9.png", false);
	private ImageIcon UNICORN = getImage("1f984.png", false);
	private ImageIcon FY = getImage("1f595-1f3fc.png", false);
	private ImageIcon STRONG = getImage("1f4aa-1f3fc.png", false);
	private ImageIcon OKAY = getImage("1f44c-1f3fc.png", false);
	private ImageIcon CLAP = getImage("1f44f-1f3fb.png", false);
	private ImageIcon PEACE = getImage("270c-1f3fb.png", false);
	private ImageIcon BEER = getImage("1f37b.png", false);
	private ImageIcon ROCKET = getImage("1f680.png", false);
	private ImageIcon THUMBSUP = getImage("1f44d-1f3fb.png", false);
	private ImageIcon GUN = getImage("1f52b.png", false);
	private ImageIcon YDS = getImage("YDS.png", true);
	private ImageIcon MG = getImage("mg.png", true);
	private ImageIcon IS = getImage("is.png", true);
	private ImageIcon TR = getImage("tr.png", true);
	private ImageIcon KENNY = getImage("kenny.png", true);
	private ImageIcon ROY = getImage("röi.png", true);
	private ImageIcon SASCHA = getImage("schascha.png", true);
	private ImageIcon FLOW = getImage("flow.png", true);
	private ImageIcon COLIN = getImage("colin.png", true);
	private ImageIcon KM = getImage("km.png", true);
	private ArrayList<File> emojiImages = new ArrayList<File>();

	public AutoreplaceingEmojiFeed() {
		super();

		final String path = "client/gui/emojiImages";
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		try {
			// Run with JAR files
			if (jarFile.isFile()) {
				System.out.println("jaaar");
				JarFile jar;
				jar = new JarFile(jarFile);
				final Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					final String name = entries.nextElement().getName();
					if (name.startsWith(path + "/")) {
						System.out.println("EmojiFeed: " + name);
						emojiImages.add(new File(name));
					}
				}
				jar.close();
				// Run in IDE
			} else {
				System.out.println("IDE");
				final URL url = Launcher.class.getResource("/" + path + "/");
				if (url != null) {
					final File apps = new File(url.toURI());
					File[] files = apps.listFiles();
					if (files != null) {
						for (File file : files) {
							emojiImages.add(file);
						}
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		initListener();
	}

	private void initListener() {
		getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent event) {
				final DocumentEvent e = event;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (e.getDocument() instanceof StyledDocument) {
							try {
								StyledDocument doc = (StyledDocument) e.getDocument();
								int start = Utilities.getRowStart(AutoreplaceingEmojiFeed.this,
										Math.max(0, e.getOffset() - 1));
								int end = Utilities.getWordStart(AutoreplaceingEmojiFeed.this,
										e.getOffset() + e.getLength());
								String text = doc.getText(start, end - start);

								replacePhraseWithImage(":'D", TEARLAUGH, text, doc, start, end, false);
								replacePhraseWithImage("/sh", SHIT, text, doc, start, end, false);
								replacePhraseWithImage("/un", UNICORN, text, doc, start, end, false);
								replacePhraseWithImage("/fy", FY, text, doc, start, end, false);
								replacePhraseWithImage("/st", STRONG, text, doc, start, end, false);
								replacePhraseWithImage("/ok", OKAY, text, doc, start, end, false);
								replacePhraseWithImage("/cl", CLAP, text, doc, start, end, false);
								replacePhraseWithImage("/pe", PEACE, text, doc, start, end, false);
								replacePhraseWithImage("/be", BEER, text, doc, start, end, false);
								replacePhraseWithImage("/ro", ROCKET, text, doc, start, end, false);
								replacePhraseWithImage("/tu", THUMBSUP, text, doc, start, end, false);
								replacePhraseWithImage("/gu", GUN, text, doc, start, end, false);
								replacePhraseWithImage("/yds", YDS, text, doc, start, end, true);
								replacePhraseWithImage("/mg", MG, text, doc, start, end, true);
								replacePhraseWithImage("/is", IS, text, doc, start, end, true);
								replacePhraseWithImage("/tr", TR, text, doc, start, end, true);
								replacePhraseWithImage("/kenny", KENNY, text, doc, start, end, true);
								replacePhraseWithImage("/röi", ROY, text, doc, start, end, true);
								replacePhraseWithImage("/sascha", SASCHA, text, doc, start, end, true);
								replacePhraseWithImage("/flo", FLOW, text, doc, start, end, true);
								replacePhraseWithImage("/colin", COLIN, text, doc, start, end, true);
								replacePhraseWithImage("/lauch", KM, text, doc, start, end, true);

								replaceUTFPhrasesWithEmojis(text, doc, start, end);

							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}

			public void removeUpdate(DocumentEvent e) {
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	private void replacePhraseWithImage(String phrase, ImageIcon image, String text, StyledDocument doc, int startDoc,
			int EndDoc, boolean meme) {
		int i = text.indexOf(phrase);
		while (i >= 0) {
			final SimpleAttributeSet attrs = new SimpleAttributeSet(
					doc.getCharacterElement(startDoc + i).getAttributes());
			if (StyleConstants.getIcon(attrs) == null) {
				StyleConstants.setIcon(attrs, image);
				try {
					doc.remove(startDoc + i, phrase.length());
					doc.insertString(startDoc + i, phrase, attrs);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			i = text.indexOf(phrase, i + 2);
		}
	}

	private ImageIcon getImage(String emojiImage, boolean meme) {
		try {
			URL inputUrl = getClass().getClassLoader().getResource("client/gui/emojiImages/" + emojiImage);
			Image image = ImageIO.read(inputUrl);
			if (meme) {
				return new ImageIcon(getScaledEmoji(image, 120, 100));
			} else {
				return new ImageIcon(getScaledEmoji(image, 30, 30));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Image getScaledEmoji(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	private void replaceUTFPhrasesWithEmojis(String text, StyledDocument doc, int startDoc, int EndDoc) {
		try {
			for (File nextFile : emojiImages) {
				String unicodeCode = nextFile.getName().replace(".png", "");
				String unicodePhrase = "<" + unicodeCode + ">";
				int i = text.indexOf(unicodePhrase);
				while (i >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(startDoc + i).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, getImage(nextFile.getName(), false));
						try {
							doc.remove(startDoc + i, unicodePhrase.length());
							doc.insertString(startDoc + i, unicodePhrase, attrs);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
					i = text.indexOf(unicodePhrase, i + 2);
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public ArrayList<File> getEmojiImages() {
		return emojiImages;
	}

}