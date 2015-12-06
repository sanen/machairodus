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
package org.machairodus.topology.cmd;

public enum Command {
	START("start"), START_GROUP("start_group"), START_ALL("start_all"),
	STOP("stop"), STOP_GROUP("stop_group"), STOP_ALL("stop_all"), 
	APPEND("append"), 
	REMOVE("remove"),
	QUARTZ("quartz"), CMD("cmd");
	
	private String value;
	private Command(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public static final Command value(String value) {
		if("start".equals(value))
			return START;
		else if("start_group".equals(value))
			return START_GROUP;
		else if("start_all".equals(value))
			return START_ALL;
		else if("stop".equals(value))
			return STOP;
		else if("stop_group".equals(value))
			return STOP_GROUP;
		else if("stop_all".equals(value))
			return STOP_ALL;
		else if("append".equals(value))
			return APPEND;
		else if("remove".equals(value))
			return REMOVE;
		else if("quartz".equals(value))
			return QUARTZ;
		else if("cmd".equals(value))
			return CMD;
		else 
			return null;
	}
	
	public static final String[] commands() {
		return new String[] {
			"command=start&id=?",
			"command=start_group&group=?",
			"command=start_all",
			"command=stop&id=?",
			"command=stop_group&group=?",
			"command=stop_all",
			"command=append&group=?&size=?&auto_start=?",
			"command=remove&group=?&size=?",
			"command=quartz",
			"command=cmd"
		};
	}
}
