package protocol;

import java.awt.Color;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextMessage implements Serializable {
	private String message;
	private Date time;
	private String name;
	private Color color;

	public TextMessage(String message, String name, Color color) {
		this.message = message;
		this.name = name;
		this.color = color;
		time = new Date();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getTime() {
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		time = new Date();
		return sdfTime.format(time);
	}

	public String getTimeNameMessage() {
		return MessageFormat.format("{0} - {1} : {2}", getTime(), name, message);
	}

	public String getMessage() {
		return message;
	}

	public String getName() {
		return name;
	}
}
