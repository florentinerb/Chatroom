package protocol;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;


public class EncryptDecryptTextMessage {

	private final byte[] key = "MyDifficultPassw".getBytes();
	private final String transformation = "AES";

	public SealedObject sealTextMessage(TextMessage message)
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		try {
			SecretKeySpec sks = new SecretKeySpec(key, transformation);

			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.ENCRYPT_MODE, sks);
			SealedObject sealedObject = new SealedObject(message, cipher);

			return sealedObject;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public TextMessage unsealTextMessage(SealedObject sealedObject) {
		try {
			SecretKeySpec sks = new SecretKeySpec(key, transformation);
			Cipher cipher;
				cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, sks);
			return (TextMessage) sealedObject.getObject(cipher);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ClassNotFoundException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
		
		}
		return null;
	}

}