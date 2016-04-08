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
package org.machairodus.topology.scheduler.defaults.etcd;

import static org.machairodus.topology.scheduler.SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX;
import static org.machairodus.topology.scheduler.SchedulerFactory.threadFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.URI;
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

import org.machairodus.topology.scheduler.BaseScheduler;
import org.machairodus.topology.scheduler.SchedulerAnalysis;
import org.machairodus.topology.scheduler.SchedulerConfig;
import org.machairodus.topology.scheduler.SchedulerException;
import org.machairodus.topology.scheduler.SchedulerFactory;
import org.machairodus.topology.scheduler.SchedulerStatus;
import org.machairodus.topology.scheduler.SchedulerStatus.Status;
import org.machairodus.topology.scheduler.defaults.monitor.LocalJmxMonitorScheduler;
import org.machairodus.topology.util.Assert;
import org.machairodus.topology.util.CollectionUtils;
import org.machairodus.topology.util.CryptUtil;
import org.machairodus.topology.util.MD5Utils;
import org.machairodus.topology.util.StringUtils;
import org.nanoframework.extension.etcd.client.retry.RetryWithExponentialBackOff;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;

public class EtcdScheduler extends BaseScheduler implements EtcdSchedulerOperate {

	private final Set<Class<?>> clsSet;
	
	public static final String SYSTEM_ID = MD5Utils.getMD5String(UUID.randomUUID().toString() + System.currentTimeMillis() + Math.random());
	public static final String ETCD_ENABLE = "context.scheduler.etcd.enable";
	public static final String ETCD_URI = "context.scheduler.etcd.uri";
	public static final String ETCD_USER = "context.scheduler.etcd.username";
	public static final String ETCD_CLIENT_ID = "context.scheduler.etcd.clientid";
	public static final String ETCD_APP_NAME = "context.scheduler.app.name";
	public static final String ETCD_MAX_RETRY_COUNT = "context.scheduler.etcd.max.retry.count";
	public static final String ETCD_SCHEDULER_ANALYSIS = "context.scheduler.analysis.enable";
	
	public static final String ROOT_RESOURCE = "/machairodus/" + System.getProperty(ETCD_USER, "");
	public static final String DIR = ROOT_RESOURCE + "/" + SYSTEM_ID;
	public static final String CLS_KEY = DIR + "/Scheduler.class";
	public static final String INSTANCE_KEY = DIR + "/Scheduler.list";
	public static final String INFO_KEY = DIR + "/App.info";
	private final int maxRetryCount = Integer.parseInt(System.getProperty(ETCD_MAX_RETRY_COUNT, "1"));
	public static final boolean SCHEDULER_ANALYSIS_ENABLE = Boolean.parseBoolean(System.getProperty(ETCD_SCHEDULER_ANALYSIS, "false"));
	
	private Map<Class<?>, String> clsIndex = new HashMap<Class<?>, String>();
	private Map<String, String> indexMap = new HashMap<String, String>();

	private static String APP_NAME;
	private boolean init = false;
	private final int timeout = 300;
	private EtcdClient etcd;
	
	public EtcdScheduler(Set<Class<?>> clsSet) {
		Assert.notNull(clsSet);
		
		this.clsSet = clsSet;
		
		SchedulerConfig config = new SchedulerConfig();
		config.setId("EtcdScheduler-0");
		config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "EtcdScheduler-0");
		config.setGroup("EtcdScheduler");
		threadFactory.setBaseScheduler(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
//		try { config.setCron(new CronExpression("0 */1 * * * ?")); } catch(ParseException e) {}
		config.setInterval(60000L);
		config.setTotal(1);
		config.setDaemon(true);
		config.setBeforeAfterOnly(true);
		config.setLazy(true);
		setConfig(config);
		setClose(false);
		
		initEtcdClient();
		if(etcd == null)
			throw new SchedulerException("Can not init Etcd Client");
		
	}
	
	@Override
	public void before() throws SchedulerException {
		
	}

	@Override
	public void execute() throws SchedulerException {
		syncBaseDirTTL();
		syncInfo();
		
		if(SCHEDULER_ANALYSIS_ENABLE)
			syncInstance();
	}
	
	public void syncBaseDirTTL() {
		try {
			if(!init) {
				etcd.putDir(DIR).ttl(timeout).prevExist(false).send().get();
				init = true;
			} else 
				etcd.putDir(DIR).ttl(timeout).prevExist(true).send().get();
			
		} catch(Exception e) {
			LOG.error("Put base dir error: " + e.getMessage(), e);
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
		info.setJmxEnable(LocalJmxMonitorScheduler.JMX_ENABLE);
		info.setJmxRate(LocalJmxMonitorScheduler.JMX_RATE);
		
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
					String index;
					EtcdKeysResponse response;
					if((index = clsIndex.get(cls)) != null) {
						response = etcd.put(CLS_KEY + "/" + index, cls.getName()).prevExist(true).send().get();
					} else {
						response = etcd.post(CLS_KEY, cls.getName()).send().get();
						if(response.node != null) {
							if((index = response.node.key.substring(response.node.key.lastIndexOf("/"))) != null)
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
		Collection<BaseScheduler> started = SchedulerFactory.getInstance().getStartedScheduler();
		Collection<BaseScheduler> stopping = SchedulerFactory.getInstance().getStoppingScheduler();
		Collection<BaseScheduler> stopped = SchedulerFactory.getInstance().getStoppedQuratz();
		
		if(!CollectionUtils.isEmpty(started)) {
			for(BaseScheduler scheduler : started) 
				start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
		}
		
		if(!CollectionUtils.isEmpty(stopping)) {
			for(BaseScheduler scheduler : stopping) 
				stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
		}
		
		if(!CollectionUtils.isEmpty(stopped)) {
			for(BaseScheduler scheduler : stopped) 
				stopped(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), false, scheduler.getAnalysis());
		}
	}
	
	@Override
	public void after() throws SchedulerException {

	}

	@Override
	public void destroy() throws SchedulerException {

	}
	
	private final void initEtcdClient() {
		/** create ETCD client instance */
		String username = System.getProperty(ETCD_USER, "");
		String clientId = CryptUtil.decrypt(System.getProperty(ETCD_CLIENT_ID, ""), username);
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
				etcd.setRetryHandler(new RetryWithExponentialBackOff(20, maxRetryCount, -1));
			}
		}
	}
	
	private EtcdKeysResponse put(String key, SchedulerStatus status) {
		try {
			String index;
			EtcdKeysResponse response;
			String value = CryptUtil.encrypt(status.toString(), SYSTEM_ID);
			if((index = indexMap.get(status.getId())) != null) {
				response = etcd.put(key + "/" + index, value).prevExist(true).send().get();
			} else {
				response = etcd.post(key, value).send().get();
				if(response.node != null) {
					if((index = response.node.key.substring(response.node.key.lastIndexOf("/"))) != null)
						indexMap.put(status.getId(), index);
				}
			}
			
			return response;
		} catch (Exception e) {
			LOG.error("Put to etcd error: " + e.getMessage());
		}
		
		return null;
	}
	
	private EtcdKeysResponse delete(String key, SchedulerStatus status) {
		try {
			String index;
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
	
	public void start(String group, String id, SchedulerAnalysis analysis) {
		put(INSTANCE_KEY, new SchedulerStatus(group, id, Status.STARTED, analysis));
	}
	
	public void stopping(String group, String id, SchedulerAnalysis analysis) {
		put(INSTANCE_KEY, new SchedulerStatus(group, id, Status.STOPPING, analysis));
	}
	
	public void stopped(String group, String id, boolean isRemove, SchedulerAnalysis analysis) {
		SchedulerStatus status = new SchedulerStatus(group, id, Status.STOPPED, analysis);
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
