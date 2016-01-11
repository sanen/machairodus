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
package org.machairodus.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.nanoframework.commons.util.Constants;
import org.nanoframework.core.plugins.PluginLoader;
import org.nanoframework.core.plugins.defaults.DefaultPluginLoader;

public class ControllerStartup {
	private static void init() {
		PluginLoader loader = new DefaultPluginLoader();
		ServletConfig config = new ServletConfig() {
			private Map<String, String> map = new HashMap<String, String>() {
				private static final long serialVersionUID = -1228713388845687367L; {
				put("context", Constants.MAIN_CONTEXT);
				put("redis", "/machairodus-redis.properties");
				put("log4j2", "/machairodus-log4j.xml");
			}};
			
			@Override
			public String getServletName() {
				return null;
			}

			@Override
			public ServletContext getServletContext() {
				return null;
			}

			@Override
			public String getInitParameter(String name) {
				return map.get(name);
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				return null;
			}
			
		};
		
		loader.init(config);
	}
	
	public static void main(String[] args) {
		init();
		
	}
}
