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

import org.machairodus.topology.quartz.QuartzFactory;
import org.machairodus.topology.quartz.defaults.Statistic;
import org.machairodus.topology.util.PropertiesLoader;
import org.machairodus.topology.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachairodusPortal {
	private Logger LOG = LoggerFactory.getLogger(MachairodusPortal.class);
	private String quartzConfigPath;
	private static AtomicBoolean isInit = new AtomicBoolean(false);
	private final Object LOCK = new Object();
	
	public MachairodusPortal() {
		
	}
	
	public MachairodusPortal(String quartzConfig) {
		this.quartzConfigPath = quartzConfig;
	}
	
	protected void init() {
		init(this.getClass().getResourceAsStream(quartzConfigPath));
	}
	
	protected void init(InputStream input) {
		if(isInit.get()) {
			LOG.warn("quartz已经初始化");
			return ;
		}
		
		synchronized (LOCK) {
			if(isInit.get()) {
				LOG.warn("quartz已经初始化");
				return ;
			}
			
			if(input == null) {
				try {
					File file = ResourceUtils.getFile(quartzConfigPath);
					if(file != null) {
						input = new FileInputStream(file);
						LOG.debug("quartz-config path: " + file.getAbsolutePath());
					}
				} catch(Exception e) {
					LOG.error("ResourceUtils.getFile load Error: " + e.getMessage());
				}
				
				if(input == null)
					input = ClassLoader.class.getResourceAsStream(quartzConfigPath);
				
				if(input == null)
					input = this.getClass().getResourceAsStream(quartzConfigPath);
			}
			
			if(input == null) {
				LOG.warn("未配置quartz-config或配置错误");
				return ;
			}
			
			Properties properties = null;
			try {
				properties = PropertiesLoader.load(quartzConfigPath, input);
			} catch(Exception e) {
				LOG.error("加载配置异常: " + e.getMessage());
			}
			
			if(properties == null || properties.isEmpty()) {
				LOG.warn("无法正确加载quartz-config属性文件");
				return ;
			}
			
			try {
				for(Entry<Object, Object> entry : properties.entrySet()) {
					String key, value;
					System.setProperty((key = (String) entry.getKey()), (value = (String) entry.getValue()));
					if(LOG.isDebugEnabled()) 
						LOG.debug("Put Property to System: ( " + key + ": " + value + " )");
				}
				
				QuartzFactory.load();
				boolean autoRun = Boolean.valueOf(properties.getProperty(QuartzFactory.AUTO_RUN, "true"));
				if(autoRun)
					QuartzFactory.getInstance().startAll();
				
			} catch(Exception e) {
				LOG.error("加载任务异常: " + e.getMessage());
			}
			
//			Statistic.setMaxPointer(Integer.parseInt(properties.getProperty(QuartzFactory.MAX_POINTER, "1200")));
			Statistic.getInstance().setMaxPointer(1);
			isInit.set(true);
		}
	}
	
	public String getQuartzConfig() {
		return quartzConfigPath;
	}
	
	public void setQuartzConfig(String quartzConfig) {
		this.quartzConfigPath = quartzConfig;
	}
	
	public static final boolean isInit() {
		return isInit.get();
	}
}
