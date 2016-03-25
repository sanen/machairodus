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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.machairodus.topology.scheduler.SchedulerFactory;
import org.machairodus.topology.scheduler.defaults.monitor.Statistic;
import org.machairodus.topology.util.PropertiesLoader;
import org.machairodus.topology.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachairodusPortal {
	private Logger LOG = LoggerFactory.getLogger(MachairodusPortal.class);
	private String schedulerConfigPath;
	private static AtomicBoolean isInit = new AtomicBoolean(false);
	private final Object LOCK = new Object();
	
	public MachairodusPortal() {
		
	}
	
	public MachairodusPortal(String schedulerConfig) {
		this.schedulerConfigPath = schedulerConfig;
	}
	
	protected void init() {
		init(this.getClass().getResourceAsStream(schedulerConfigPath));
	}
	
	protected void init(InputStream input) {
		if(isInit.get()) {
			LOG.warn("scheduler已经初始化");
			return ;
		}
		
		synchronized (LOCK) {
			if(isInit.get()) {
				LOG.warn("scheduler已经初始化");
				return ;
			}
			
			if(input == null) {
				try {
					File file = ResourceUtils.getFile(schedulerConfigPath);
					if(file != null) {
						input = new FileInputStream(file);
						LOG.debug("scheduler-config path: " + file.getAbsolutePath());
					}
				} catch(Exception e) {
					LOG.error("ResourceUtils.getFile load Error: " + e.getMessage());
				}
				
				if(input == null)
					input = ClassLoader.class.getResourceAsStream(schedulerConfigPath);
				
				if(input == null)
					input = this.getClass().getResourceAsStream(schedulerConfigPath);
			}
			
			if(input == null) {
				LOG.warn("未配置scheduler-config或配置错误");
				return ;
			}
			
			Properties properties = null;
			try {
				properties = PropertiesLoader.load(schedulerConfigPath, input);
			} catch(Exception e) {
				LOG.error("加载配置异常: " + e.getMessage());
			}
			
			if(properties == null || properties.isEmpty()) {
				LOG.warn("无法正确加载scheduler-config属性文件");
				return ;
			}
			
			try {
				for(Entry<Object, Object> entry : properties.entrySet()) {
					String key, value;
					System.setProperty((key = (String) entry.getKey()), (value = (String) entry.getValue()));
					if(LOG.isDebugEnabled()) 
						LOG.debug("Put Property to System: ( " + key + ": " + value + " )");
				}
				
				SchedulerFactory.load();
				boolean autoRun = Boolean.valueOf(properties.getProperty(SchedulerFactory.AUTO_RUN, "true"));
				if(autoRun)
					SchedulerFactory.getInstance().startAll();
				
			} catch(Exception e) {
				LOG.error("加载任务异常: " + e.getMessage());
			}
			
			Statistic.getInstance().setMaxPointer(1);
			isInit.set(true);
		}
	}
	
	public String getSchedulerConfig() {
		return schedulerConfigPath;
	}
	
	public void setSchedulerConfig(String schedulerConfig) {
		this.schedulerConfigPath = schedulerConfig;
	}
	
	public static final boolean isInit() {
		return isInit.get();
	}
}
