package org.machairodus.topology.util;

import org.machairodus.topology.entity.BaseEntity;

/**
 * Http 返回消息对象
 * @author yanghe
 * @date 2015年7月25日 下午8:18:08 
 *
 */
public class ResultMap extends BaseEntity {
	private static final long serialVersionUID = -4058911595479256410L;
	
	/** 描述 */
	private String info;
	/** 状态码 */
	private int status;
	/** 消息内容 */
	private String message;
	
	public static final String INFO = "info";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";

	private ResultMap(int status, String message, String info) {
		this.status = status;
		this.message = message;
		this.info = info;
	}
	
	/**
	 * 创建消息对象 
	 * @param status 状态码
	 * @param message 消息内容
	 * @param info 描述
	 * @return
	 */
	public static ResultMap create(int status, String message, String info) {
		return new ResultMap(status, message, info);
	}
	
	public String getInfo() {
		return info;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
}
