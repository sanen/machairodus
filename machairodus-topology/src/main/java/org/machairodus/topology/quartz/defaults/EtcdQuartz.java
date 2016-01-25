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
package org.machairodus.topology.quartz.defaults;

import static org.machairodus.topology.quartz.QuartzFactory.DEFAULT_QUARTZ_NAME_PREFIX;
import static org.machairodus.topology.quartz.QuartzFactory.threadFactory;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.CronExpression;
import org.machairodus.topology.quartz.QuartzConfig;
import org.machairodus.topology.quartz.QuartzException;
import org.machairodus.topology.util.Assert;
import org.machairodus.topology.util.MD5Utils;
import org.machairodus.topology.util.StringUtils;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.extension.etcd.client.retry.RetryWithExponentialBackOff;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;

public class EtcdQuartz extends BaseQuartz {

	private final Set<Class<?>> clsSet;
	
	@SuppressWarnings("unused")
	private final Properties properties;
	
	public static final String SYSTEM_ID = MD5Utils.getMD5String(UUID.randomUUID().toString() + System.currentTimeMillis() + Math.random());
	public static final String ETCD_URI = "context.quartz.etcd.uri";
	public static final String ETCD_USER = "context.quartz.etcd.username";
	public static final String ETCD_PASSWD = "context.quartz.etcd.passwd";
	public static final String ETCD_RESOURCE = "/machairodus";
	
	public static final String DIR = ETCD_RESOURCE + "/" + SYSTEM_ID;
	public static final String CLS_KEY = DIR + "/Quartz.class";
	public static final String STARTED_KEY = DIR + "/Started";
	public static final String STOPPING_KEY = DIR + "/Stopping";
	public static final String STOPPED_KEY = DIR + "/Stopped";
	
	private Map<Class<?>, Long> clsIndex = new HashMap<Class<?>, Long>();
	private Map<String, Map<String, Long>> indexMap = new HashMap<String, Map<String, Long>>();
	
	private boolean init = false;
	private final int timeout = 75;
	private EtcdClient etcd;
	
	public EtcdQuartz(Set<Class<?>> clsSet, Properties properties) {
		Assert.notNull(properties);
		Assert.notNull(clsSet);
		
		this.clsSet = clsSet;
		this.properties = properties;
		
		QuartzConfig config = new QuartzConfig();
		config.setId("EtcdQuartz-0");
		config.setName(DEFAULT_QUARTZ_NAME_PREFIX + "EtcdQuartz-0");
		config.setGroup("EtcdQuartz");
		threadFactory.setBaseQuartz(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
		try { config.setCron(new CronExpression("0 */1 * * * ?")); } catch(ParseException e) {}
		config.setTotal(1);
		config.setDaemon(true);
		config.setBeforeAfterOnly(true);
		config.setLazy(true);
		setConfig(config);
		setClose(false);
		
		initEtcdClient(properties);
		if(etcd == null)
			throw new QuartzException("Can not init Etcd Client");
		
	}
	
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		syncBaseDirTTL();
	}
	
	public void syncBaseDirTTL() {
		try {
			if(!init) {
				etcd.putDir(DIR).ttl(timeout).prevExist(false).send().get();
				init = true;
			} else 
				etcd.putDir(DIR).ttl(timeout).prevExist(true).send().get();
			
		} catch(Exception e) {
			LOG.error("Put base dir error: " + e.getMessage());
		}
	}
	
	public void syncClass() {
		if(!CollectionUtils.isEmpty(clsSet)) {
			Iterator<Class<?>> iter = clsSet.iterator();
			while(iter.hasNext()) {
				try {
					Class<?> cls = iter.next();
					Long index;
					EtcdKeysResponse response;
					if((index = clsIndex.get(cls)) != null) {
						response = etcd.put(CLS_KEY + "/" + index, cls.getName()).prevExist(true).send().get();
					} else {
						response = etcd.post(CLS_KEY, cls.getName()).send().get();
						if(response.node != null && (index = response.node.createdIndex) != null) {
							clsIndex.put(cls, index);
						}
					}
					
					LOG.debug("Class Sync: " + cls.getName());
				} catch (Exception e) {
					LOG.error("Send to Etcd error: " + e.getMessage());
				} 
			}
		}
	}
	
	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {

	}
	
	private final void initEtcdClient(Properties properties) {
		Assert.notNull(properties);
		Assert.notEmpty(properties);
		
		/** create ETCD client instance */
		String username = properties.getProperty(ETCD_USER, "");
		String password = properties.getProperty(ETCD_PASSWD, "");
		String[] uris = properties.getProperty(ETCD_URI, "").split(",");
		if(!StringUtils.isEmpty(username.trim()) && !StringUtils.isEmpty(password.trim()) && uris.length > 0) {
			List<URI> uriList = new ArrayList<URI>();
			for(String uri : uris) {
				if(StringUtils.isEmpty(uri))
					continue ;
				
				try {
					uriList.add(URI.create(uri));
				} catch(Throwable e) {
					LOG.error("Etcd URI Error: " + e.getMessage());
				}
			}
			
			if(uriList.size() > 0) {
				etcd = new EtcdClient(username, password, uriList.toArray(new URI[uriList.size()]));
				etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
			}
		}
	}
	
	private EtcdKeysResponse put(String key, String field, String value) {
		try {
			Long index;
			Map<String, Long> map;
			if((map = indexMap.get(key)) == null)
				indexMap.put(key, map = new HashMap<String, Long>());
			
			EtcdKeysResponse response;
			if((index = map.get(value)) != null) {
				response = etcd.put(field + "/" + index, value).prevExist(true).send().get();
			} else {
				response = etcd.post(field, value).send().get();
				if(response.node != null && (index = response.node.createdIndex) != null) {
					map.put(value, index);
				}
			}
			
			return response;
		} catch (Exception e) {
			LOG.error("Put to etcd error: " + e.getMessage());
		}
		
		return null;
	}
	
	private EtcdKeysResponse delete(String key, String field) {
		try {
			Long index;
			Map<String, Long> map;
			if((map = indexMap.get(key)) == null)
				indexMap.put(key, map = new HashMap<String, Long>());
			
			EtcdKeysResponse response = null;
			if((index = map.get(field)) != null) {
				response = etcd.delete(field + "/" + index).send().get();
				map.remove(field);
			} 
			
			return response;
		} catch (Exception e) {
			LOG.error("Delete etcd file error: " + e.getMessage());
		}
		
		return null;
	}
	
	public void start(String group, String id) {
		put(STARTED_KEY, STARTED_KEY + "/" + group, id);
		delete(STOPPED_KEY, STOPPED_KEY + "/" + group);
	}
	
	public void stopping(String group, String id) {
		put(STOPPING_KEY, STOPPING_KEY + "/" + group, id);
		delete(STARTED_KEY, STARTED_KEY + "/" + group);
	}
	
	public void stopped(String group, String id, boolean isRemove) {
		if(!isRemove)
			put(STOPPED_KEY, STOPPED_KEY + "/" + group, id);
		
		delete(STOPPING_KEY, STOPPING_KEY + "/" + group);
	}
	
	public EtcdClient getEtcd() {
		return etcd;
	}
}
