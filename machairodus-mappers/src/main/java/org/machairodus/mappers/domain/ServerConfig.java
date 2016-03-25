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
package org.machairodus.mappers.domain;

import java.sql.Timestamp;

import org.nanoframework.commons.entity.BaseEntity;
import org.nanoframework.commons.util.StringUtils;

public class ServerConfig extends BaseEntity {
	private static final long serialVersionUID = 6334193116614361971L;

	private Long id;
	private String name;
	private String address;
	private String username;
	private String passwd;
	private Timestamp createTime;
	private Long createUserId;
	private String createUserName;
	private Timestamp modifyTime;
	private Long modifyUserId;
	private String modifyUserName;
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String ADDRESS = "address";
	public static final String USERNAME = "username";
	public static final String PASSWD = "passwd";
	public static final String CREATE_TIME = "createTime";
	public static final String CREATE_USER_ID = "createUserId";
	public static final String CREATE_UESR_NAME = "createUserName";
	public static final String MODIFY_TIME = "modifyTime";
	public static final String MODIFY_USER_ID = "modifyUserId";
	public static final String MODIFY_USER_NAME = "modifyUserName";
	
	public boolean validate() {
		if(StringUtils.isBlank(name))
			return false;
		
		if(StringUtils.isBlank(address))
			return false;
		
		if(StringUtils.isBlank(username))
			return false;
		
		if(StringUtils.isBlank(passwd))
			return false;
		
		return true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public Timestamp getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Long getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(Long modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getModifyUserName() {
		return modifyUserName;
	}

	public void setModifyUserName(String modifyUserName) {
		this.modifyUserName = modifyUserName;
	}

}
