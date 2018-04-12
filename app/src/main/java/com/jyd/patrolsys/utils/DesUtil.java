package com.jyd.patrolsys.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DesUtil {

	private static String key = "test1234";// 默认的key

	/**
	 * 根据默认key解密数据
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static synchronized String decrypt(String message){
		try{
			String md5z= message.substring(0, message.indexOf("$"));
			String pwd = message.substring(message.indexOf("$") + 1);
			if (!MD5Util.checkPassword(pwd, md5z)) {
				return "MD5值不正确";
			}
			byte[] bytesrc = convertHexString(pwd);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
			IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
			cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
			byte[] retByte = cipher.doFinal(bytesrc);
	//		String b=new String(retByte,"UTF-8");
			String b = java.net.URLDecoder.decode(new String(retByte,"UTF-8"), "UTF-8");//编码问题
			return b;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 解密数据
	 * 
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String message, String key) throws Exception {
		byte[] bytesrc = convertHexString(message);
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		byte[] retByte = cipher.doFinal(bytesrc);
		String b = java.net.URLDecoder.decode(new String(retByte), "utf-8");
		// return new String(retByte);
		return b;
	}

	public static byte[] encrypt(String message, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

		return cipher.doFinal(message.getBytes("UTF-8"));
	}

	public static byte[] convertHexString(String ss) {
		byte digest[] = new byte[ss.length() / 2];
		for (int i = 0; i < digest.length; i++) {
			String byteString = ss.substring(2 * i, 2 * i + 2);
			int byteValue = Integer.parseInt(byteString, 16);
			digest[i] = (byte) byteValue;
		}

		return digest;
	}

	public static void main(String[] args) throws Exception {
		// String key = "12345678";
		 String value = "爱到底21TTTe";
		//
		 String a = encryptData(value);
		System.out.println(a);
		// long startTime = System.currentTimeMillis();// 获取当前时间
		// String b = encryptData(MD5Util.getMD5String(a) + "$" + a, key);
		// long endTime = System.currentTimeMillis();
		// System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
		// System.out.println("加密后的数据为:" + b);
		// System.out.println("加密后的数据为:" +a);
		// System.out.println("解密后的数据:" + decrypt(a, key));
//		System.out.println("解密后的数据:" + DesUtil.decrypt(a));
//		System.out.println("密："+"9BEDA8C15F8FBD703738A653D5B7AEA8");
//		System.out.println("解密后的数据:" + DesUtil.decrypt("65EEC44D59EB636545BF2D692816390B"));
//		String str = "51b9883ba4deac221219bc03408b14b3$9BEDA8C15F8FBD703738A653D5B7AEA8";
//		String cc = str.substring(0, str.indexOf("$"));
//		String aa = str.substring(str.indexOf("$") + 1);
//		if (MD5Util.checkPassword(aa, cc)) {
//			System.out.println(aa);
//			System.out.println("解密后的数据:" + Des.decrypt(aa));
//		}
		
		String mess = decrypt(a);
		System.out.println(mess);
	}

	public static String toHexString(byte b[]) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String plainText = Integer.toHexString(0xff & b[i]);
			if (plainText.length() < 2)
				plainText = "0" + plainText;
			hexString.append(plainText);
		}
		return hexString.toString();
	}

	/**
	 * 加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static synchronized String encryptData(String data, String key) throws Exception {
		String jiami = java.net.URLEncoder.encode(data, "utf-8");
		String a = toHexString(encrypt(jiami, key));
		return a;
	}

	/**
	 * 根据默认key加密数据(增加了md5校验，以及拼接规则)
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static synchronized String encryptData(String data) throws Exception {
		String jiami = java.net.URLEncoder.encode(data, "utf-8");
		String a = toHexString(encrypt(jiami, key));
		return MD5Util.getMD5String(a)+"$"+a;
	}
}