package zdaly.etl.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;

public class HashUtil {
	
	public static void main(String args[]) {
		List<String> ids = new ArrayList<>();
		if(args.length < 2) {
			System.err.println("Usage: java HashUtil <Salt> <Message>");
		}
		String salt = args[0];
		String message = args[1];
		List<String> messages = new ArrayList<>();
		messages.add(message);
//		Random rn = new Random();
//		for(int i=0; i< 10000; i++) {
//			ids.add(rn.nextInt(1000000000) + "");
//		}
//		System.out.println(Sha256Test.encode(ids));
//		System.out.println(Sha256Test.encode2(ids));
//		System.out.println(Sha256Test.encodeAES(ids));
		HashUtil.encode(messages);
		HashUtil.encode(messages, salt);
		HashUtil.encodeAES(messages);
	}
	
	/**
	 * Returns a SHA256 Hash
	 * @param message
	 * @return
	 */
	public static String encode(String message) {
		String hashEncoded = null;
		byte[] hash = null;
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hashEncoded = Base64.encodeBase64String(hash);
		return hashEncoded;
	}
	
	/**
	 * Returns a list of SHA256 Hashes
	 * @param messages
	 * @return
	 */
	public static List<String> encode(List<String> messages) {
		List<String> encodedHashes = new ArrayList<>();
		byte[] hash = null;
		MessageDigest digest;
		String hashString = "";
		for(String message: messages) {
			try {
				digest = MessageDigest.getInstance("SHA-256");
				hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hashString = Base64.encodeBase64String(hash);
			encodedHashes.add(hashString);
		}
		return encodedHashes;
	}
	
	/**
	 * Returns a List of SHA256 hashes
	 * @param messages
	 * @param salt
	 * @return
	 */
	public static List<String> encode(List<String> messages, String salt) {
		List<String> encodedHashes = new ArrayList<>();
		for(String message: messages) {
		    // Initialize SHA-256
		    MessageDigest digest = null;
		    try {
		        digest = MessageDigest.getInstance("SHA-256");
		        digest.update(salt.getBytes());
		    } catch (NoSuchAlgorithmException e) {
		        System.err.println(e.getMessage());
		    }
	
		    // Hashing entered key to construct a new key/salt
		    byte[] hash = digest.digest(message.getBytes());
	
		    // build cipher from the chars
		    encodedHashes.add(Base64.encodeBase64String(hash));
		}
	    return encodedHashes;
	}
	
	/**
	 * Returns a SHA256 hash using salt
	 * @param message
	 * @param salt
	 * @return
	 */
	public static String encode(String message, String salt) {
		String hashEncoded = null;
	    
	    // Initialize SHA-256
	    MessageDigest digest = null;
	    try {
	        digest = MessageDigest.getInstance("SHA-256");
	        digest.update(salt.getBytes());
	    } catch (NoSuchAlgorithmException e) {
	        System.err.println(e.getMessage());
	    }

	    // Hashing entered key to construct a new key/salt
	    byte[] hash = digest.digest(message.getBytes());

	    // build cipher from the chars
	    hashEncoded = Base64.encodeBase64String(hash);
	    return hashEncoded;
	}

	
	
	public static List<String> encodeAES(List<String> messages) {	
		long starttime = System.currentTimeMillis();
		List<String> hashes = new ArrayList<String>();
		String decryptedText = "";
			

        KeyGenerator KeyGen;
        for(String message: messages) {
			try {
				KeyGen = KeyGenerator.getInstance("AES");
			
		        KeyGen.init(128);
		
		        SecretKey SecKey = KeyGen.generateKey();
		
		        Cipher AesCipher = Cipher.getInstance("AES");
		
		
		        byte[] byteText = message.getBytes();
		
		        AesCipher.init(Cipher.ENCRYPT_MODE, SecKey);
		        byte[] byteCipherText = AesCipher.doFinal(byteText);
		        hashes.add(Base64.encodeBase64String(byteCipherText));
		
	//	        byte[] cipherText = encryptedText.getBytes();
		
	//	        AesCipher.init(Cipher.DECRYPT_MODE, SecKey);
	//	        byte[] bytePlainText = AesCipher.doFinal(cipherText);
	//	        decryptedText = Base64.encodeBase64String(bytePlainText);
	//	        System.out.println("Decrypted text: " + decryptedText);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		long end = System.currentTimeMillis();
		System.out.println("Time take: " + (end-starttime) + "ms");
		return hashes;
	}

}
