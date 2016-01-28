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

import static org.machairodus.topology.quartz.QuartzFactory.DEFAULT_QUARTZ_NAME_PREFIX;
import static org.machairodus.topology.quartz.QuartzFactory.threadFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.CronExpression;
import org.machairodus.topology.quartz.QuartzConfig;
import org.machairodus.topology.quartz.QuartzException;
import org.machairodus.topology.quartz.QuartzFactory;
import org.machairodus.topology.quartz.QuartzStatus;
import org.machairodus.topology.quartz.QuartzStatus.Status;
import org.machairodus.topology.util.Assert;
import org.machairodus.topology.util.CollectionUtils;
import org.machairodus.topology.util.CryptUtil;
import org.machairodus.topology.util.MD5Utils;
import org.machairodus.topology.util.StringUtils;
import org.nanoframework.extension.etcd.client.retry.RetryWithExponentialBackOff;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;

public class EtcdQuartz extends BaseQuartz implements EtcdQuartzOperate {

	private final Set<Class<?>> clsSet;
	
	public static final String SYSTEM_ID = MD5Utils.getMD5String(UUID.randomUUID().toString() + System.currentTimeMillis() + Math.random());
	public static final String ETCD_ENABLE = "context.quartz.etcd.enable";
	public static final String ETCD_URI = "context.quartz.etcd.uri";
	public static final String ETCD_USER = "context.quartz.etcd.username";
	public static final String ETCD_CLIENT_ID = "context.quartz.etcd.clientid";
	public static final String ETCD_APP_NAME = "context.quartz.app.name";
	public static final String ETCD_RESOURCE = "context.quartz.etcd.resource";
	
	public static final String ROOT_RESOURCE;
	public static final String DIR = (ROOT_RESOURCE = System.getProperty(ETCD_RESOURCE, "/machairodus/" + System.getProperty(ETCD_USER, ""))) + "/" + SYSTEM_ID;
	public static final String CLS_KEY = DIR + "/Quartz.class";
	public static final String INSTANCE_KEY = DIR + "/Quartz.list";
	public static final String INFO_KEY = DIR + "/App.info";
	private static String APP_NAME;
	
	private Map<Class<?>, Long> clsIndex = new HashMap<Class<?>, Long>();
	private Map<String, Long> indexMap = new HashMap<String, Long>();
	
	private boolean init = false;
	private final int timeout = 75;
	private EtcdClient etcd;
	
	public EtcdQuartz(Set<Class<?>> clsSet) {
		Assert.notNull(clsSet);
		
		this.clsSet = clsSet;
		
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
		
		initEtcdClient();
		if(etcd == null)
			throw new QuartzException("Can not init Etcd Client");
		
	}
	
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		syncBaseDirTTL();
		syncInfo();
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
			if(e.getMessage() != null && e.getMessage().indexOf("Key not found") > -1) {
				reSync();
				return ;
			}
			
			if(e.getMessage() != null && e.getMessage().indexOf("Key already exists") > -1) {
				init = true;
				syncBaseDirTTL();
				return ;
			}
			
			// 异常2秒重试
			thisWait(2000);
			syncBaseDirTTL();
		}
	}
	
	private void reSync() {
		init = false;
		clsIndex.clear();
		indexMap.clear();
		
		syncBaseDirTTL();
		syncInfo();
		syncClass();
		syncInstance();
	}
	
	public void syncInfo() {
		EtcdAppInfo info = new EtcdAppInfo();
		info.setSystemId(SYSTEM_ID);
		info.setAppName(APP_NAME);
		
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		info.setStartTime(runtime.getStartTime());
		info.setUptime(runtime.getUptime());
		String[] rt = runtime.getName().split("@");
		info.setHostName(rt[1]);
		info.setPid(rt[0]);
		
		info.setAvailableProcessors(ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
		
		try {
			info.setIp(Inet4Address.getLocalHost().getHostAddress());
			String value = CryptUtil.encrypt(info.toString(), SYSTEM_ID);
			etcd.put(INFO_KEY, value).send().get();
		} catch (Exception e) {
			LOG.error("Send App info error: " + e.getMessage());
			
			// 异常2秒重试
			thisWait(2000);
			syncBaseDirTTL();
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
	
	public void syncInstance() {
		Collection<BaseQuartz> started = QuartzFactory.getInstance().getStartedQuartz();
		Collection<BaseQuartz> stopping = QuartzFactory.getInstance().getStoppingQuartz();
		Collection<BaseQuartz> stopped = QuartzFactory.getInstance().getStoppedQuratz();
		
		if(!CollectionUtils.isEmpty(started)) {
			for(BaseQuartz quartz : started) 
				start(quartz.getConfig().getGroup(), quartz.getConfig().getId());
		}
		
		if(!CollectionUtils.isEmpty(stopping)) {
			for(BaseQuartz quartz : stopping) 
				stopping(quartz.getConfig().getGroup(), quartz.getConfig().getId());
		}
		
		if(!CollectionUtils.isEmpty(stopped)) {
			for(BaseQuartz quartz : stopped) 
				stopped(quartz.getConfig().getGroup(), quartz.getConfig().getId(), false);
		}
	}
	
	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {

	}
	
	private final void initEtcdClient() {
		/** create ETCD client instance */
		String username = System.getProperty(ETCD_USER, "");
		String clientId = CryptUtil.decrypt(System.getProperty(ETCD_CLIENT_ID, ""));
		APP_NAME = System.getProperty(ETCD_APP_NAME, "");
		String[] uris = System.getProperty(ETCD_URI, "").split(",");
		if(!StringUtils.isEmpty(username.trim()) && !StringUtils.isEmpty(clientId.trim()) && !StringUtils.isEmpty(APP_NAME.trim()) && uris.length > 0) {
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
				etcd = new EtcdClient(username, clientId, uriList.toArray(new URI[uriList.size()]));
				etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
			}
		}
	}
	
	private EtcdKeysResponse put(String key, QuartzStatus status) {
		try {
			Long index;
			EtcdKeysResponse response;
			String value = CryptUtil.encrypt(status.toString(), SYSTEM_ID);
			if((index = indexMap.get(status.getId())) != null) {
				response = etcd.put(key + "/" + index, value).prevExist(true).send().get();
			} else {
				response = etcd.post(key, value).send().get();
				if(response.node != null && (index = response.node.createdIndex) != null) {
					indexMap.put(status.getId(), index);
				}
			}
			
			return response;
		} catch (Exception e) {
			LOG.error("Put to etcd error: " + e.getMessage());
		}
		
		return null;
	}
	
	private EtcdKeysResponse delete(String key, QuartzStatus status) {
		try {
			Long index;
			EtcdKeysResponse response = null;
			if((index = indexMap.get(status.getId())) != null) {
				response = etcd.delete(key + "/" + index).send().get();
				indexMap.remove(status.getId());
			} 
			
			return response;
		} catch (Exception e) {
			LOG.error("Delete etcd file error: " + e.getMessage());
		}
		
		return null;
	}
	
	public void start(String group, String id) {
		put(INSTANCE_KEY, new QuartzStatus(group, id, Status.STARTED));
	}
	
	public void stopping(String group, String id) {
		put(INSTANCE_KEY, new QuartzStatus(group, id, Status.STOPPING));
	}
	
	public void stopped(String group, String id, boolean isRemove) {
		QuartzStatus status = new QuartzStatus(group, id, Status.STOPPED);
		if(!isRemove)
			put(INSTANCE_KEY, status);
		else 
			delete(INSTANCE_KEY, status);
		
	}
	
	public EtcdClient getEtcd() {
		return etcd;
	}
	
	public static String getAppName() {
		return APP_NAME;
	}
}
