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

public class SchedulerInfo extends BaseEntity {
	private static final long serialVersionUID = 4238451255679675517L;

	private Long id;
	private Long nodeId;
	private NodeConfig nodeConfig;
	private Long schedulerId;
	private SchedulerConfig schedulerConfig;
	private Timestamp createTime;
	private Long createUserId;
	private String createUserName;
	
	public static final String ID = "id";
	public static final String NODE_ID = "nodeId";
	public static final String NODE_CONFIG = "nodeConfig";
	public static final String SCHEDULER_ID = "schedulerId";
	public static final String SCHEDULER_CONFIG = "schedulerConfig";
	public static final String CREATE_TIME = "createTime";
	public static final String CREATE_USER_ID = "createUserId";
	public static final String CREATE_USER_NAME = "createUserName";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public NodeConfig getNodeConfig() {
		return nodeConfig;
	}

	public void setNodeConfig(NodeConfig nodeConfig) {
		this.nodeConfig = nodeConfig;
	}

	public Long getSchedulerId() {
		return schedulerId;
	}

	public void setSchedulerId(Long schedulerId) {
		this.schedulerId = schedulerId;
	}

	public SchedulerConfig getSchedulerConfig() {
		return schedulerConfig;
	}

	public void setSchedulerConfig(SchedulerConfig schedulerConfig) {
		this.schedulerConfig = schedulerConfig;
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

}
