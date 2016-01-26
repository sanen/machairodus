/**
 * Copyright 2015-2016 the original author or authors.
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
package org.machairodus.topology.quartz.defaults.etcd;

import org.machairodus.topology.entity.BaseEntity;
import org.machairodus.topology.quartz.QuartzConfig;
import org.machairodus.topology.util.ObjectCompare;

public class EtcdOrder extends BaseEntity {
	private static final long serialVersionUID = 6645140062880311456L;

	private OrderAction action;
	private String group;
	private String id;
	private Integer size;
	private Boolean autoStart;
	private QuartzConfig config;
	private String className;

	public boolean valid() {
		if (action == null)
			return false;

		if (group == null || group.trim().length() == 0)
			return false;

		if (ObjectCompare.isInList(action, OrderAction.START, OrderAction.STOP, OrderAction.REMOVE) && (id == null || id.trim().length() == 0))
			return false;

		if (action == OrderAction.NEW && (config == null || className == null || className.trim().length() == 0))
			return false;

		if (action == OrderAction.APPEND && (size == null || size <= 0 || autoStart == null))
			return false;

		return true;
	}

	public OrderAction getAction() {
		return action;
	}

	public void setAction(OrderAction action) {
		this.action = action;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Boolean getAutoStart() {
		return autoStart;
	}

	public void setAutoStart(Boolean autoStart) {
		this.autoStart = autoStart;
	}

	public QuartzConfig getConfig() {
		return config;
	}

	public void setConfig(QuartzConfig config) {
		this.config = config;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public enum OrderAction {
		NEW, APPEND, START, STOP, REMOVE, START_GROUP, STOP_GROUP, REMOVE_GROUP, START_ALL, STOP_ALL, REMOVE_ALL;
	}
}
