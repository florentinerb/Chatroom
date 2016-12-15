package protocol;

import java.io.Serializable;

public class User implements Serializable {
	private String name;
	private Boolean isLocked;

	public User(String name, Boolean isLocked) {
		this.name = name;
		this.isLocked = isLocked;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		if (isLocked == true) {
			return name + " - Locked";
		} else {
			return name;
		}
	}

}
