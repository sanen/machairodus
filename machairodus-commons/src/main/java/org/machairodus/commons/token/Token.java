/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.machairodus.commons.token;

import java.io.Serializable;

import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.crypt.DecryptException;
import org.nanoframework.commons.format.DateFormat;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

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
