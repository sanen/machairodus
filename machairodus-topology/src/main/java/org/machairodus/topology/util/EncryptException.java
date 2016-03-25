package org.machairodus.topology.util;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:00:11
 */
public class EncryptException extends RuntimeException {
	private static final long serialVersionUID = 5713797904458714409L;

	public EncryptException() {

	}
	
	public EncryptException(String message) {
		super(message);
		
	}
	
	public EncryptException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "加密异常: " + super.getMessage();
	}
	
}
