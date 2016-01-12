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

public class NodeConfig extends BaseEntity {
	private static final long serialVersionUID = 2789087606159146020L;

	private Long id;
	private Long serverId;
	private String serverName;
	private String serverAddress;
	private String name;
	private Integer port;
	private Integer jmxPort = 0;
	private Integer type;
	private Integer weight = 0;
	private Integer startup;
	private Integer pid;
	private Timestamp createTime;
	private Long createUserId;
	private String createUserName;
	private Timestamp modifyTime;
	private Long modifyUserId;
	private String modifyUserName;
	
	public static final String ID = "id";
	public static final String SERVER_ID = "serverId";
	public static final String SERVER_NAME = "serverName";
	public static final String SERVER_ADDRESS = "serverAddress";
	public static final String NAME = "name";
	public static final String PORT = "port";
	public static final String JMX_PORT = "jmxPort";
	public static final String TYPE = "type";
	public static final String WEIGHT = "weight";
	public static final String STARTUP = "startup";
	public static final String PID = "pid";
	public static final String CREATE_TIME = "createTime";
	public static final String CREATE_USER_ID = "createUserId";
	public static final String CREATE_USER_NAME = "createUserName";
	public static final String MODIFY_TIME = "modifyTime";
	public static final String MODIFY_USER_ID = "modifyUserId";
	public static final String MODIFY_USER_NAME = "modifyUserName";

	public boolean validate() {
		if(StringUtils.isBlank(name))
			return false;
		
		if(serverId == null)
			return false;
		
		if(port == null)
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

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(Integer jmxPort) {
		this.jmxPort = jmxPort;
	}

	public Integer getType() {
		return type;
	}
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Integer getStartup() {
		return startup;
	}

	public void setStartup(Integer startup) {
		this.startup = startup;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
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
