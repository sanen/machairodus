package org.machairodus.manager.service;

import javax.management.openmbean.CompositeData;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.Assert;

public class Pointer extends BaseEntity {
	private static final long serialVersionUID = 7350989467756362845L;

	private String scene;
	private Long time;
	private Long tps;

	public static Pointer from(CompositeData data) {
		Assert.notNull(data, "Pointer composite data must not be null.");
		
		Pointer pointer = new Pointer();
		for(String attr : pointer._getAttributeNames()) {
			pointer._setAttributeValue(attr, data.get(attr));
		}
		
		return pointer;
	}
	
	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Long getTps() {
		return tps;
	}

	public void setTps(Long tps) {
		this.tps = tps;
	}

}
