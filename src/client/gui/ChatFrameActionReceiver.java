package client.gui;

import java.awt.Color;

public interface ChatFrameActionReceiver {

	public void restartClient();

	public void settingsFrameCalled();

	public void reconnectWasCalled();

	public void messageSent(String message, Color color, String receiver);

	public void typing();

}
