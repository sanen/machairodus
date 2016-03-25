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

public class SchedulerConfig extends BaseEntity {
	private static final long serialVersionUID = -4061526992615772489L;

	private Long id;
	private String name;
	private String uri;
	private String options;
	private Integer type;
	private String description;
	private Timestamp createTime;
	private Long createUserId;
	private String createUserName;
	private Timestamp modifyTime;
	private Long modifyUserId;
	private String modifyUserName;
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String URI = "uri";
	public static final String OPTIONS = "options";
	public static final String TYPE = "type";
	public static final String DESCRIPTION = "description";
	public static final String CREATE_TIME = "createTime";
	public static final String CREATE_USER_ID = "createUserId";
	public static final String CREATE_USER_NAME = "createUserName";
	public static final String MODIFY_TIME = "modifyTime";
	public static final String MODIFY_USER_ID = "modifyUserId";
	public static final String MODIFY_USER_NAME = "modifyUserName";

	public boolean validate() {
		if(StringUtils.isBlank(name))
			return false;
		
		if(StringUtils.isBlank(uri))
			return false;
		
		if(type == null)
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
