package client.gui;

import javax.swing.JLabel;

import protocol.TypingState;

class TypingLabelController implements Runnable {
	private JLabel label;
	private String username;
	static Thread thread;

	public TypingLabelController(JLabel label, String username) {
		this.label = label;
		this.username = username;
	}

	public void receivedTypingInfos(TypingState typingState) {
		if (!typingState.getName().equals(username)) {
			label.setText(typingState.getName() + " is typing...");
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(this);
				thread.start();
			}
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		label.setText("");
	}

}
