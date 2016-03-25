package org.machairodus.topology.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class Token implements Serializable {
	private static final long serialVersionUID = -7571893862515329885L;
	
	private String token;
	private String encodeToken;
	private String time;
	
	private static final Logger LOG = LoggerFactory.getLogger(Token.class);
	public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm";
	public static final TypeReference<Token> TYPE = new TypeReference<Token>() { };
	
	public Token() { }
	
	private Token(String token, String encodeToken, String time) {
		this.token = token;
		this.encodeToken = encodeToken;
		this.time = time;
		
	}
	
	public static String encode(String token) {
		String time = DateFormat.format(System.currentTimeMillis(), TIME_PATTERN);
		Token _token = new Token(token, CryptUtil.encrypt(token, token + ":" + time), time);
		return CryptUtil.encrypt(JSON.toJSONString(_token));
	}
	
	public static boolean decode(String token) {
		Token _token = JSON.parseObject(CryptUtil.decrypt(token), TYPE);
		String time = DateFormat.format(System.currentTimeMillis(), TIME_PATTERN);
		
		try {
			String _tokenString = CryptUtil.decrypt(_token.getEncodeToken(), _token.getToken() + ":" + time);
			if(time.equals(_token.getTime()) && _token.getToken().equals(_tokenString)) {
				return true;
			}
			
			return false;
		} catch(DecryptException e) {
			LOG.error("Token decode error: " + e.getMessage());
			return false;
		}
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getEncodeToken() {
		return encodeToken;
	}

	public void setEncodeToken(String encodeToken) {
		this.encodeToken = encodeToken;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
