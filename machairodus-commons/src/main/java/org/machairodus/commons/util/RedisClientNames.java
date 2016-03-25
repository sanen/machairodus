/**
 * Copyright 2015 the original author or authors.
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
package org.machairodus.commons.util;

import org.nanoframework.commons.util.Assert;

public enum RedisClientNames {
	SHIRO("shiro"), MANAGER("manager");
	
	private String value;
	private RedisClientNames(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public static final RedisClientNames value(String value) {
		Assert.hasLength(value, "value must not be empty.");
		
		switch(value) {
			case "shiro": 
				return SHIRO;
				
			case "manager": 
				return MANAGER;
				
			default: 
				throw new IllegalArgumentException("Unknown redis client name");
		}
	}
}
