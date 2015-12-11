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
package org.machairodus.topology;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.machairodus.topology.quartz.QuartzFactory;

public class MachairodusListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				QuartzFactory.getInstance().closeAll();
			}
		}));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		/** 只为备用，防止ShutdownHook无效时 */
		QuartzFactory.getInstance().closeAll();
	}

}
