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
import java.util.List;

import org.nanoframework.commons.entity.BaseEntity;

public class Func extends BaseEntity {
	private static final long serialVersionUID = -5627769862166286294L;

	private Long id;
	private String code;
	private String name;
	private String description;
	private String uri;
	private Long parentId;
	private Integer virtual;
	private String hierarchy;
	private String icon;
	private Integer status;
	private Timestamp createTime;
	private Long createUserId;
	private Timestamp modifyTime;
	private Long modifyUserId;
	private Integer deleted;
	
	private List<Func> children;

	public static final String ID = "id";
	public static final String CODE = "code";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String URI = "uri";
	public static final String PARENT_ID = "parentId";
	public static final String VIRTUAL = "virtual";
	public static final String HIERARCHY = "hierarchy";
	public static final String ICON = "icon";
	public static final String STATUS = "status";
	public static final String CREATE_TIME = "createTime";
	public static final String CREATE_USER_ID = "createUserId";
	public static final String MODIFY_TIME = "modifyTime";
	public static final String MODIFY_USER_ID = "modifyUserId";
	public static final String DELETED = "deleted";
	
	public static final String CHILDREN = "children";
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getVirtual() {
		return virtual;
	}

	public void setVirtual(Integer virtual) {
		this.virtual = virtual;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	public List<Func> getChildren() {
		return children;
	}

	public void setChildren(List<Func> children) {
		this.children = children;
	}

}
