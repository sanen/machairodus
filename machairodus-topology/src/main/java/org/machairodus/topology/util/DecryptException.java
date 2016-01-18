package org.machairodus.topology.util;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:00:03
 */
public class DecryptException extends RuntimeException {
	private static final long serialVersionUID = 2618331308933845339L;

	public DecryptException() {

	}
	
	public DecryptException(String message) {
		super(message);
		
	}
	
	public DecryptException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "解密异常: " + super.getMessage();
	}
	
}
