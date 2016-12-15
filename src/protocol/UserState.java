package protocol;

import java.io.Serializable;

public class UserState implements Serializable {
	private String ipAdress;
	private Boolean locked;

	public UserState(String ipAdress, Boolean locked) {
		this.ipAdress = ipAdress;
		this.locked = locked;
	}

	public String getIpAdress() {
		return ipAdress;
	}

	public Boolean isLocked() {
		return locked;
	}
}
