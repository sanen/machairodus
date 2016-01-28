package org.machairodus.topology.util;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午8:59:44
 */
@SuppressWarnings("restriction")
public class CryptUtil {
	
	private static final String DEFAULT_PASSWORD = "nano-framework";
	
	public static String encrypt(String content) {
		return encrypt(content, DEFAULT_PASSWORD);
	}
	
	public static String encrypt(String content, String password) {
		if(null == password || 0 == password.length())
			password = DEFAULT_PASSWORD;
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes(Charset.forName("UTF-8")));
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = (content).getBytes("UTF-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);//加密
			
			String encodeStr = new String(BASE64.getInstance().encode(parseByte2HexStr(result).getBytes()));
			int idx;
			if((idx = encodeStr.indexOf("=")) > -1) {
				String tmp = encodeStr.substring(0, idx);
				int len = encodeStr.substring(idx).length();
				encodeStr = tmp + len;
			} else 
				encodeStr += "0";
			
			return encodeStr;
		} catch (Exception e) {
			throw new EncryptException(e.getMessage(), e);
		} 

	}
	
	public static String decrypt(String date) {
		return decrypt(date, DEFAULT_PASSWORD);
	}

	public static String decrypt(String data, String password) {
		if(StringUtils.isEmpty(password))
			password = DEFAULT_PASSWORD;
		
		String tmp = data.substring(0, data.length() - 1);
		int len = Integer.parseInt(data.substring(data.length() - 1));
		if(len > 0) {
			for(int idx = 0; idx < len; idx ++) 
				tmp += "=";
			
			data = tmp;
		} else 
			data = tmp;
		
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] content = parseHexStr2Byte(new String(decoder.decodeBuffer(data)));
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(password.getBytes(Charset.forName("UTF-8")));
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");//创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);//初始化
			byte[] result = cipher.doFinal(content);//解密
			return new String(result);
			
		} catch (Exception e) {
			throw new DecryptException(e.getMessage(), e);
		} 
	}
	
	private static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0XFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	} 
	
	private static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		
		byte[] result = new byte[hexStr.length()/2];
		for (int i = 0;i< hexStr.length()/2; i++) {
			int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
			int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	} 
	
}
