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
package org.machairodus.manager.util;

public enum Definition {
	INDEX("index"),
	PERMISSION_USER("permission.user"),
	PERMISSION_ROLE("permission.role"),
	PERMISSION_FUNC("permission.func"),
	CONFIGURE_SERVER("configure.server"),
	CONFIGURE_NODE("configure.node"),
	CONFIGURE_SERVICE("configure.service"),
	SCHEDULE_BALANCER("schedule.balancer"),
	SCHEDULE_SCHEDULER("schedule.scheduler"), 
	MONITOR_LOAD("monitor.load"),
	STATISTICS_SCHEDULER("statistics.scheduler");
	
	private String value;
	private Definition(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
	
	public static final Definition value(String value) {
		switch(value) {
			case "index": 
				return INDEX;
			case "permission.user": 
				return PERMISSION_USER;
			case "permission.role": 
				return PERMISSION_ROLE;
			case "permission.func":
				return PERMISSION_FUNC;
			case "configure.server": 
				return CONFIGURE_SERVER;
			case "configure.node": 
				return CONFIGURE_NODE;
			case "configure.service": 
				return CONFIGURE_SERVICE;
			case "schedule.balancer": 
				return SCHEDULE_BALANCER;
			case "schedule.scheduler": 
				return SCHEDULE_SCHEDULER;
			case "monitor.load": 
				return MONITOR_LOAD;
			case "statistics.scheduler": 
				return STATISTICS_SCHEDULER;
			default: 
				throw new IllegalArgumentException("Unknown Definition Type");
		}
	}
}
