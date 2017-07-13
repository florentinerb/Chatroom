package client.gui;

import javax.swing.JLabel;

import protocol.TypingState;

class TypingLabelController implements Runnable {
	private long lastTypingMessageTimer;
	private JLabel label;
	private long timeElapsed;
	private String username;
	private boolean alive = true;

	public TypingLabelController(JLabel label, String username) {
		this.label = label;
		this.username = username;

		lastTypingMessageTimer = System.currentTimeMillis();
		Thread thread = new Thread(this);
		thread.start();
	}

	public void receivedTypingInfos(TypingState typingState) {
		if (!typingState.getName().equals(username)) {
			label.setText(typingState.getName() + " is typing...");
		}
		lastTypingMessageTimer = System.currentTimeMillis();
	}

	@Override
	public void run() {
		while (alive) {
			timeElapsed = System.currentTimeMillis() - lastTypingMessageTimer;
			if (timeElapsed > 3000) {
				label.setText("");
			}
		}
	}

	public void shutdown() {
		alive = false;
	}

}
