package org.machairodus.topology.entity;

/**
 * 实体类操作异常类
 * 
 * @author yanghe
 * @date 2015年6月9日 上午8:57:21 
 *
 */
public class EntityException extends RuntimeException {
	private static final long serialVersionUID = 3642079270948106738L;

	public EntityException() {

	}
	
	public EntityException(String message) {
		super(message);
		
	}
	
	public EntityException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "实体类操作异常: " + super.getMessage();
	}
	
}
