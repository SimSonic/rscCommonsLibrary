package ru.simsonic.rscCommonsLibrary;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Formatter;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.misc.BASE64Decoder;

public final class HashAndCipherUtilities
{
	public static String binToHex(byte[] binary)
	{
		Formatter formatter = new Formatter();
		for(byte b : binary)
			formatter.format("%02x", b);
		return formatter.toString();
	}
	public static String binToHex(String binary)
	{
		return binToHex(binary.getBytes());
	}
	public static String stringToMD5(String string)
	{
		try
		{
			final StringBuilder sb = new StringBuilder();
			for(byte b : MessageDigest.getInstance("MD5").digest(string.getBytes()))
				sb.append(String.format("%02x", b));
			return sb.toString();
		} catch(NoSuchAlgorithmException ex) {
		}
		return "";
	}
	public static String stringToSHA1(String string)
	{
		try
		{
			final StringBuilder sb = new StringBuilder();
			for(byte b : MessageDigest.getInstance("SHA-1").digest(string.getBytes()))
				sb.append(String.format("%02x", b));
			return sb.toString();
		} catch(NoSuchAlgorithmException ex) {
		}
		return "";
	}
	public static String fileToMD5(File file) throws IOException
	{
		try
		{
			final MessageDigest md5 = MessageDigest.getInstance("MD5");
			final FileInputStream fis = new FileInputStream(file);
			final DigestInputStream dis = new DigestInputStream(fis, md5);
			while(dis.read() != -1) {}
			return binToHex(md5.digest());
		} catch(NoSuchAlgorithmException ex) {
			throw new IOException(ex);
		}
	}
	public static String fileToMD5(String filename) throws IOException
	{
		return fileToMD5(new File(filename));
	}
	public static String fileToSHA1(File file) throws IOException
	{
		try
		{
			final MessageDigest md5 = MessageDigest.getInstance("SHA-1");
			final FileInputStream fis = new FileInputStream(file);
			final DigestInputStream dis = new DigestInputStream(fis, md5);
			while(dis.read() != -1) {}
			return binToHex(md5.digest());
		} catch(NoSuchAlgorithmException ex) {
			throw new IOException(ex);
		}
	}
	public static String fileToSHA1(String filename) throws IOException
	{
		return fileToSHA1(new File(filename));
	}
	public static <T> T loadObject(File source, Class<T> dataClass) throws IOException, NullPointerException
	{
		try(final JsonReader reader = new JsonReader(new FileReader(source)))
		{
			return new Gson().fromJson(reader, dataClass);
		} catch(JsonParseException ex) {
			throw new IOException(ex);
		}
	}
	public static <T> boolean saveObject(File target, Object data, Class<T> dataClass) throws IOException, NullPointerException
	{
		try(final JsonWriter writer = new JsonWriter(new FileWriter(target)))
		{
			writer.setIndent("\t");
			new Gson().toJson(data, dataClass, writer);
			writer.flush();
			return true;
		} catch(JsonParseException ex) {
			throw new IOException(ex);
		}
	}
	public static <T> T loadEncryptedObject(File source, Class<T> dataClass)
	{
		try(final JsonReader reader = new JsonReader(createCipherReader(source)))
		{
			return new Gson().fromJson(reader, dataClass);
		} catch(NullPointerException | IOException | JsonParseException ex) {
			System.out.println(ex);
		}
		return null;
	}
	public static <T> boolean saveEncryptedObject(File target, Object data, Class<T> dataClass)
	{
		try(final JsonWriter writer = new JsonWriter(createCipherWriter(target)))
		{
			writer.setIndent("\t");
			new Gson().toJson(data, dataClass, writer);
			writer.flush();
			return true;
		} catch(NullPointerException | IOException | JsonParseException ex) {
		}
		return false;
	}
	private static final String cipherError = "Cannot create cipher instance";
	public static InputStream cipheredInputStream(InputStream plain) throws IOException
	{
		// Шифрование открытого входного потока
		try
		{
			return new CipherInputStream(plain, createCipher(Cipher.ENCRYPT_MODE));
		} catch(Exception ex) {
			throw new IOException(cipherError, ex);
		}
	}
	public static OutputStream cipheredOutputStream(OutputStream plain) throws IOException
	{
		// Шифрование открытого выходного потока
		try
		{
			return new CipherOutputStream(plain, createCipher(Cipher.ENCRYPT_MODE));
		} catch(Exception ex) {
			throw new IOException(cipherError, ex);
		}
	}
	public static InputStream decipheredInputStream(InputStream plain) throws IOException
	{
		// Расшифровка входного потока в открытый
		try
		{
			return new CipherInputStream(plain, createCipher(Cipher.DECRYPT_MODE));
		} catch(Exception ex) {
			throw new IOException(cipherError, ex);
		}
	}
	public static OutputStream decipheredOutputStream(OutputStream plain) throws IOException
	{
		// Расшифровка выходного потока в открытый
		try
		{
			return new CipherOutputStream(plain, createCipher(Cipher.DECRYPT_MODE));
		} catch(Exception ex) {
			throw new IOException(cipherError, ex);
		}
	}
	public static OutputStreamWriter createCipherWriter(File file)
	{
		try
		{
			return new OutputStreamWriter(cipheredOutputStream(new FileOutputStream(file)));
		} catch(IOException ex) {
			System.out.println(ex);
		}
		return null;
	}
	public static InputStreamReader createCipherReader(File file)
	{
		try
		{
			return new InputStreamReader(decipheredInputStream(new FileInputStream(file)));
		} catch(IOException ex) {
			System.out.println(ex);
		}
		return null;
	}
	private static Cipher createCipher(int mode) throws IOException
	{
		try
		{
			final String algorythm = new String(new BASE64Decoder().decodeBuffer("UEJFV2l0aE1ENUFuZERFUw==")); // "PBEWithMD5AndDES";
			final String secretkey = new String(new BASE64Decoder().decodeBuffer("cGFzc3dvcmRmaWxl"));         // "passwordfile";
			final Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
			final SecretKey pbeKey = SecretKeyFactory.getInstance(algorythm).generateSecret(new PBEKeySpec(secretkey.toCharArray()));
			final byte[] salt = new byte[8];
			(new Random(43287234L)).nextBytes(salt);
			cipher.init(mode, pbeKey, new PBEParameterSpec(salt, 5));
			return cipher;
		} catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException ex) {
			throw new IOException(ex);
		} catch(IOException ex) {
			throw ex;
		}
	}
}
