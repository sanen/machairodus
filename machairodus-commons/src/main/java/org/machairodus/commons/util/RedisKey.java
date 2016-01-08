package org.machairodus.commons.util;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.Assert;

public class RedisKey extends BaseEntity {
	private static final long serialVersionUID = -8685252571420957L;
	private String key;
	private RedisStorageType type;

	private RedisKey(String key, RedisStorageType type) {
		this.key = key;
		this.type = type;
	}
	
	public static final RedisKey create(String key, RedisStorageType type) {
		Assert.hasLength(key);
		Assert.notNull(type);
		return new RedisKey(key, type);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public RedisStorageType getType() {
		return type;
	}

	public void setType(RedisStorageType type) {
		this.type = type;
	}

}