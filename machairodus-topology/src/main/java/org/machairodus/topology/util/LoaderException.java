package org.machairodus.topology.util;

/**
 * 加载异常处理类
 * @author yanghe
 *
 */
public class LoaderException extends RuntimeException {
	private static final long serialVersionUID = 6079958393741484203L;

	public LoaderException() {

	}
	
	public LoaderException(String message) {
		super(message);
		
	}
	
	public LoaderException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "加载异常: " + super.getMessage();
	}
}
