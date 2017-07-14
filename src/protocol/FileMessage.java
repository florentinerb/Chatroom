package protocol;

import java.io.Serializable;

public class FileMessage implements Serializable {
	private String name;
	private byte[] file;
	private String fileType;
	private String sender;

	public FileMessage(byte[] file, String fileType, String name, String sender) {
		this.file = file;
		this.fileType = fileType;
		this.name = name;
		this.sender = sender;
	}

	public String getName() {
		return name;
	}

	public byte[] getFile() {
		return file;
	}

	public String getFileType() {
		return fileType;
	}

	public String getSender() {
		return sender;
	}
}
