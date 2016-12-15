package protocol;

import java.io.Serializable;

public class TypingState implements Serializable {
	private String username;

	public TypingState(String name) {
		this.username = name;

	}

	public String getName() {
		return username;
	}
}
