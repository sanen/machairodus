/**
 * Copyright 2015- the original author or authors.
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
package org.machairodus.topology.scheduler;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.scan.ComponentScan;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdOrderWatcherScheduler;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdScheduler;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdSchedulerOperate;
import org.machairodus.topology.scheduler.defaults.monitor.LocalJmxMonitorScheduler;
import org.machairodus.topology.util.Assert;
import org.machairodus.topology.util.CollectionUtils;
import org.machairodus.topology.util.ObjectCompare;
import org.machairodus.topology.util.RuntimeUtil;
import org.machairodus.topology.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务工厂
 * @author yanghe
 * @date 2015年6月8日 下午5:24:13 
 */
public class SchedulerFactory {
	private static Logger LOG = LoggerFactory.getLogger(SchedulerFactory.class);
	private static SchedulerFactory FACTORY;
	private static final Object LOCK = new Object();
	private AtomicInteger startedSchedulerSize = new AtomicInteger(0);
	private final ConcurrentMap<String , BaseScheduler> startedScheduler = new ConcurrentHashMap<String , BaseScheduler>();
	private final ConcurrentMap<String , BaseScheduler> stoppingScheduler = new ConcurrentHashMap<String , BaseScheduler>();
	private final ConcurrentMap<String , BaseScheduler> stoppedScheduler = new ConcurrentHashMap<String , BaseScheduler>();
	private final ConcurrentMap<String, Set<BaseScheduler>> group = new ConcurrentHashMap<String, Set<BaseScheduler>>();
	public static final SchedulerThreadFactory threadFactory = new SchedulerThreadFactory();
	private static final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
	private static final ThreadPoolExecutor closeSchedulerService = (ThreadPoolExecutor) new ThreadPoolExecutor(0, RuntimeUtil.AVAILABLE_PROCESSORS, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	private static final ThreadPoolExecutor closeSchedulerCallableService = (ThreadPoolExecutor) new ThreadPoolExecutor(0, RuntimeUtil.AVAILABLE_PROCESSORS, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	private static boolean isLoaded = false;
	
	public static final String BASE_PACKAGE = "context.scheduler-scan.base-package";
	public static final String AUTO_RUN = "context.scheduler.run.auto";
	public static final String INCLUDES = "context.scheduler.group.includes";
	public static final String EXCLUSIONS = "context.scheduler.group.exclusions";
	public static final String DEFAULT_SCHEDULER_NAME_PREFIX = "Scheduler-Thread-Pool: ";
	
	private static EtcdSchedulerOperate etcdScheduler;
	
	private SchedulerFactory() {
		
	}
	
	public static final SchedulerFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null) {
					FACTORY = new SchedulerFactory();
					StatusMonitorScheduler statusMonitor = FACTORY.new StatusMonitorScheduler();
					statusMonitor.getConfig().getService().execute(statusMonitor);
					Runtime.getRuntime().addShutdownHook(new Thread(FACTORY.new ShutdownHook()));
				}
			}
		}
		
		return FACTORY;
		
	}
	
	/**
	 * 绑定任务
	 * 
	 * @param scheduler 任务
	 * @return 返回当前任务
	 */
	public BaseScheduler bind(BaseScheduler scheduler) {
		try {
			scheduler.setClose(false);
			startedScheduler.put(scheduler.getConfig().getId(), scheduler);
			startedSchedulerSize.incrementAndGet();
			return scheduler;
		} finally {
			if(LOG.isInfoEnabled())
				LOG.info("绑定任务: 任务号[ " + scheduler.getConfig().getId() + " ]");
			
		}
	}
	
	/**
	 * 解绑任务
	 * 
	 * @param scheduler 任务
	 * @return 返回当前任务
	 */
	public BaseScheduler unbind(BaseScheduler scheduler) {
		BaseScheduler removed = startedScheduler.remove(scheduler.getConfig().getId());
		if(removed != null) {
			startedSchedulerSize.decrementAndGet();
			if(LOG.isDebugEnabled())
				LOG.debug("解绑任务 : 任务号[ " + scheduler.getConfig().getId() + " ], 现存任务数: " + startedSchedulerSize.get());
		}
		
		return scheduler;
	}
	
	/**
	 * 获取现在正在执行的任务数
	 * @return 任务数
	 */
	public int getStartedSchedulerSize() {
		return startedSchedulerSize.get();
		
	}
	
	/**
	 * 返回所有任务
	 * @return 任务集合
	 */
	public Collection<BaseScheduler> getStartedScheduler() {
		return startedScheduler.values();
	}
	
	public int getStoppingSchedulerSize() {
		return stoppingScheduler.size();
	}
	
	public Collection<BaseScheduler> getStoppingScheduler() {
		return stoppingScheduler.values();
	}
	
	public int getStopedSchedulerSize() {
		return stoppedScheduler.size();
	}
	
	public Collection<BaseScheduler> getStoppedQuratz() {
		return stoppedScheduler.values();
	}
	
	/**
	 * 关闭任务
	 * @param id 任务号
	 */
	public void close(final String id) {
		BaseScheduler scheduler = null;
		try {
			close(scheduler = startedScheduler.get(id));
		} finally {
			if(scheduler != null && LOG.isDebugEnabled())
				LOG.debug("关闭任务: 任务号[ " + id + " ]");
		}
	}
	
	public void close(final BaseScheduler scheduler) {
		if(scheduler != null && !scheduler.isClose()) {
			if(scheduler.getConfig().getWorkerClass() != BaseScheduler.class) {
				/** Sync to Etcd by stop method */
				etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
				
				scheduler.setClose(true);
				stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
				startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
				
			} else {
				int size = 0;
				Set<BaseScheduler> dataLoaders = new LinkedHashSet<BaseScheduler>();
				for(BaseScheduler _scheduler : startedScheduler.values()) {
					if(_scheduler.getClass() == scheduler.getClass()) {
						size ++;
					}
					
					if(_scheduler.getConfig().getWorkerClass() == scheduler.getClass()) {
						dataLoaders.add(_scheduler);
					}
				}
				
				if(size > 1) {
					/** Sync to Etcd by stop method */
					etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
					
					scheduler.setClose(true);
					stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
					startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
				} else if(size == 1) {
					for(BaseScheduler _scheduler : dataLoaders) {
						/** Sync to Etcd by stop method */
						etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
						
						_scheduler.setClose(true);
						stoppingScheduler.put(_scheduler.getConfig().getId(), _scheduler);
						startedScheduler.remove(_scheduler.getConfig().getId(), _scheduler);
					}
					
					closeByQueue(scheduler, scheduler.getConfig().getQueueName());
				}
			}
		}
	}
	
	/**
	 * 关闭整组任务
	 * @param groupName
	 */
	public void closeGroup(String groupName) {
		Assert.hasLength(groupName, "groupName must not be null");
		Set<BaseScheduler> dataLoaders = new LinkedHashSet<BaseScheduler>();
		for(BaseScheduler scheduler : startedScheduler.values()) {
			if(scheduler.getConfig().getGroup().equals(groupName)) {
				for(BaseScheduler _scheduler : startedScheduler.values()) {
					if(_scheduler.getConfig().getWorkerClass() == scheduler.getClass()) {
						dataLoaders.add(_scheduler);
					}
				}
				
				break;
			}
		}
		
		for(BaseScheduler scheduler : dataLoaders) {
			/** Sync to Etcd by stop method */
			etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
			
			scheduler.setClose(true);
			stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
			startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
		}
		
		for(BaseScheduler scheduler : startedScheduler.values()) {
			if(scheduler.getConfig().getGroup().equals(groupName)) {
				if(!"".equals(scheduler.getConfig().getQueueName()))
					closeByQueue(scheduler, scheduler.getConfig().getQueueName());
				else {
					/** Sync to Etcd by stop method */
					etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
					
					scheduler.setClose(true);
					stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
					startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
				}
			}
		}
	}
	
	/**
	 * 关闭所有任务
	 */
	public synchronized void closeAll() {
		if(startedScheduler.size() > 0) {
			LOG.warn("现在关闭所有的任务");
			Set<String> groupNames = new LinkedHashSet<String>();
			for(BaseScheduler scheduler : startedScheduler.values()) {
				groupNames.add(scheduler.getConfig().getGroup());
			}
			
			for(String groupName : groupNames) {
				closeGroup(groupName);
			}
		}
	}
	
	private void closeByQueue(final BaseScheduler scheduler, final String queueName) {
		if(scheduler.isClose())
			return ;
		
		if(StringUtils.isEmpty(queueName)) {
			/** Sync to Etcd by stop method */
			etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
			
			scheduler.setClose(true);
			stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
			startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
			
			return ;
		}
		
		threadFactory.setBaseScheduler(null);
		closeSchedulerService.execute(new Runnable() {
			@Override
			public void run() {
				Future<Boolean> future = closeSchedulerCallableService.submit(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						while(BlockingQueueFactory.getInstance().getQueue(queueName).size() > 0) Thread.sleep(100L);
						return true;
					}
				});
				
				try {
					long timeout = scheduler.getConfig().getTimeout();
					if(timeout <= 0) {
						LOG.warn("现在开始无限等待队列数据消费，请注意线程阻塞: " + scheduler.getConfig().getId());
						future.get();
						LOG.info("队列数据消费结束: " + scheduler.getConfig().getId());
						
						/** Sync to Etcd by stop method */
						etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
						
						scheduler.setClose(true);
						stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
						startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
					} else {
						future.get(timeout, TimeUnit.MILLISECONDS);
						
						/** Sync to Etcd by stop method */
						etcdScheduler.stopping(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
						
						scheduler.setClose(true);
						stoppingScheduler.put(scheduler.getConfig().getId(), scheduler);
						startedScheduler.remove(scheduler.getConfig().getId(), scheduler);
					}
				} catch(Exception e) {
					LOG.error("等待队列数据消费超时: " + e.getMessage());
				} 
			}
		});
	}
	
	/**
	 * 启动所有缓冲区中的任务并清理任务缓冲区
	 */
	public final void startAll() {
		if(stoppedScheduler.size() > 0) {
			for(Entry<String, BaseScheduler> entry : stoppedScheduler.entrySet()) {
				String name = entry.getKey();
				BaseScheduler scheduler = entry.getValue();
				if(LOG.isInfoEnabled())
					LOG.info("Start scheduler [ " + name + " ], class with [ " + scheduler.getClass().getName() + " ]");
				
				getInstance().bind(scheduler);
				threadFactory.setBaseScheduler(scheduler);
				service.execute(scheduler);
				
				/** Sync to Etcd by start method */
				etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
			}
			
			stoppedScheduler.clear();
		}
	}
	
	public final void startGroup(String groupName) {
		if(stoppedScheduler.size() > 0) {
			Set<String> keys = new HashSet<String>();
			for(Entry<String, BaseScheduler> entry : stoppedScheduler.entrySet()) {
				String id = entry.getKey();
				BaseScheduler scheduler = entry.getValue();
				if(groupName.equals(scheduler.getConfig().getGroup())) {
					if(scheduler.isClose()) {
						if(LOG.isInfoEnabled())
							LOG.info("Start scheduler [ " + id + " ], class with [ " + scheduler.getClass().getName() + " ]");
						
						getInstance().bind(scheduler);
						threadFactory.setBaseScheduler(scheduler);
						service.execute(scheduler);
						keys.add(id);
						
						/** Sync to Etcd by start method */
						etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
					}
				}
			}
			
			for(String key : keys) {
				stoppedScheduler.remove(key);
			}
		}
	}
	
	public final void start(String id) {
		BaseScheduler scheduler = stoppedScheduler.get(id);
		if(scheduler != null && scheduler.isClose()) {
			if(LOG.isInfoEnabled())
				LOG.info("Start scheduler [ " + id + " ], class with [ " + scheduler.getClass().getName() + " ]");
			
			getInstance().bind(scheduler);
			threadFactory.setBaseScheduler(scheduler);
			service.execute(scheduler);
			stoppedScheduler.remove(id);
			
			/** Sync to Etcd by start method */
			etcdScheduler.start(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), scheduler.getAnalysis());
		}
	}
	
	public final void append(String groupName, int size, boolean autoStart) {
		BaseScheduler scheduler = findLast(groupName);
		if(scheduler == null) 
			return ;
		
		for(int idx = 0; idx < size; idx ++) {
			SchedulerConfig config = (SchedulerConfig) scheduler.getConfig().clone();
			int total = config.getTotal();
			config.setTotal(total + 1);
			config.setNum(total);
			config.setId(groupName + "-" + scheduler.getIndex(groupName));
			config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + config.getId());
			BaseScheduler _new = scheduler.clone();
			_new.setClose(true);
			_new.setClosed(true);
			_new.setRemove(false);
			_new.setConfig(config);
			addScheduler(_new);
			if(autoStart)
				start(config.getId());
			else {
				etcdScheduler.stopped(_new.getConfig().getGroup(), _new.getConfig().getId(), false, scheduler.getAnalysis());
			}
			
		}
	}
	
	public final boolean closed(String id) {
		return stoppedScheduler.containsKey(id);
	}
	
	public final boolean started(String id) {
		return startedScheduler.containsKey(id);
	}
	
	public final boolean hasClosedGroup(String group) {
		if(stoppedScheduler.size() > 0) {
			for(BaseScheduler scheduler : stoppedScheduler.values()) {
				if(scheduler.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public final boolean hasStartedGroup(String group) {
		if(startedScheduler.size() > 0) {
			for(BaseScheduler scheduler : startedScheduler.values()) {
				if(scheduler.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public final void addScheduler(BaseScheduler scheduler) {
		Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
		if(groupScheduler == null) groupScheduler = new LinkedHashSet<BaseScheduler>();
		groupScheduler.add(scheduler);
		group.put(scheduler.getConfig().getGroup(), groupScheduler);
		
		if(stoppedScheduler.containsKey(scheduler.getConfig().getId()) || startedScheduler.containsKey(scheduler.getConfig().getId()))
			throw new SchedulerException("exists scheduler in memory");
		
		stoppedScheduler.put(scheduler.getConfig().getId(), scheduler);
		rebalance(scheduler.getConfig().getGroup());
	}
	
	public final int removeScheduler(BaseScheduler scheduler) {
		Set<BaseScheduler> groupScheduler = group.get(scheduler.getConfig().getGroup());
		boolean remove = false;
		if(groupScheduler.size() > 1) {
			groupScheduler.remove(scheduler);
			scheduler.setRemove(remove = true);
		}
		
		if(scheduler.isClosed()) {
			/** Sync to Etcd by start method */
			etcdScheduler.stopped(scheduler.getConfig().getGroup(), scheduler.getConfig().getId(), remove, scheduler.getAnalysis());
		} else 
			close(scheduler);
		
		rebalance(scheduler.getConfig().getGroup());
		
		return groupScheduler.size();
	}
	
	public final int removeScheduler(String groupName) {
		BaseScheduler scheduler = findLast(groupName);
		if(scheduler != null) {
			return removeScheduler(scheduler);
		}
		
		return 0;
	}
	
	public final void removeGroup(String groupName) {
		while(removeScheduler(groupName) > 1) ;
		closeGroup(groupName);
		
	}
	
	public final int getGroupSize(String groupName) {
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler))
			return groupScheduler.size();
		
		return 0;
	}
	
	public Set<BaseScheduler> getGroupScheduler(String groupName) {
		return group.get(groupName);
	}
	
	public final BaseScheduler find(String id) {
		Assert.hasLength(id, "id must be not empty.");
		String groupName = id.substring(0, id.lastIndexOf("-"));
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getId().equals(id))
					return scheduler;
			}
		}
		
		return null;
	}
	
	public final BaseScheduler findLast(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			int max = -1;
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getNum() > max)
					max = scheduler.getConfig().getNum();
			}
			
			for(BaseScheduler scheduler : groupScheduler) {
				if(scheduler.getConfig().getNum() == max) {
					return scheduler;
				}
			}
		}
		
		return null;
	}
	
	public final void rebalance(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseScheduler> groupScheduler = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupScheduler)) {
			AtomicInteger idx = new AtomicInteger(0);
			for(BaseScheduler scheduler : groupScheduler) {
				scheduler.getConfig().setNum(idx.getAndIncrement());
				scheduler.getConfig().setTotal(groupScheduler.size());
			}
		}
	}
	
	/**
	 * 加载任务调度
	 * @param injector Guice Injector
	 * @throws IllegalArgumentException 非法的参数列表
	 * @throws IllegalAccessException ?
	 */
	@SuppressWarnings("unchecked")
	public static final void load() throws IllegalArgumentException, IllegalAccessException {
		if(isLoaded) 
			throw new SchedulerException("Scheduler已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");

		String _package = System.getProperty(BASE_PACKAGE);
		if(_package == null || _package.isEmpty())
			throw new SchedulerException("Property '" + BASE_PACKAGE + "' must not be null.");
		
		String[] packages = _package.split(",");
		for(String pkg : packages) {
			String _pkg;
			if(pkg != null && !(_pkg = pkg.trim()).isEmpty()) {
				ComponentScan.scan(_pkg);
			}
		}
		
		Set<Class<?>> componentClasses = ComponentScan.filter(Scheduler.class);
		if(LOG.isInfoEnabled())
			LOG.info("Scheduler size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			String[] includes = System.getProperty(INCLUDES, ".").split(",");
			String[] exclusions;
			if(!StringUtils.isEmpty(System.getProperty(EXCLUSIONS)))
				exclusions = System.getProperty(EXCLUSIONS).split(",");
			else 
				exclusions = new String[0];
			
			for(Class<?> clz : componentClasses) {
				if(BaseScheduler.class.isAssignableFrom(clz)) {
					if(LOG.isInfoEnabled())
						LOG.info("Inject Scheduler Class: " + clz.getName());
					
					Scheduler scheduler = clz.getAnnotation(Scheduler.class);
					if(!ObjectCompare.isInListByRegEx(clz.getSimpleName(), includes) || ObjectCompare.isInListByRegEx(clz.getSimpleName(), exclusions)) {
						LOG.warn("过滤任务组: " + clz.getSimpleName() + ", 类名 [ " + clz.getName()+ " ]");
						continue ;
					}
					
					String parallelProperty = scheduler.parallelProperty();
					int parallel = 0;
					String cron = "";
					String value;
					if(!StringUtils.isEmpty(parallelProperty) && (value = System.getProperty(parallelProperty)) != null && !value.isEmpty()) {
						/** 采用最后设置的属性作为最终结果 */
						try {
							parallel = Integer.parseInt(value);
						} catch(NumberFormatException e) { 
							throw new SchedulerException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
						}
					}
					
					if(!StringUtils.isEmpty(scheduler.cronProperty()) && (value = System.getProperty(scheduler.cronProperty())) != null && !value.isEmpty())
						cron = value;
					
					parallel = scheduler.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : scheduler.parallel();
					if(parallel < 0)
						parallel = 0;
					
					if(StringUtils.isEmpty(cron))
						cron = scheduler.cron();
					
					try {
						for(int p = 0; p < parallel; p ++) {
							BaseScheduler baseScheduler = (BaseScheduler) clz.newInstance();
							SchedulerConfig config = new SchedulerConfig();
							config.setId(clz.getSimpleName() + "-" + baseScheduler.getIndex(clz.getSimpleName()));
							config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + config.getId());
							config.setGroup(clz.getSimpleName());
							config.setService(service);
							config.setBeforeAfterOnly(scheduler.beforeAfterOnly());
							config.setRunNumberOfTimes(scheduler.runNumberOfTimes());
							config.setInterval(scheduler.interval());
							config.setNum(p);
							config.setTotal(parallel);
							if(!StringUtils.isEmpty(cron))
								try { config.setCron(new CronExpression(cron)); } catch(ParseException e) { throw new SchedulerException(e.getMessage(), e); }
						
							config.setLazy(scheduler.lazy());
							config.setDaemon(scheduler.daemon());
							config.setDefined(scheduler.defined());
							
							/** set Machairodus private proerty   START */
							if(!StringUtils.isEmpty(scheduler.workerClassProperty().trim())) {
								try {
									String className = System.getProperty(scheduler.workerClassProperty().trim());
									if(!StringUtils.isEmpty(className)) {
										Class<?> cls = Class.forName(className);
										if(BaseScheduler.class.isAssignableFrom(cls))
											config.setWorkerClass((Class<? extends BaseScheduler>) cls);
										else 
											throw new SchedulerException("无效的workClass属性配置，workClass必须继承BaseScheduler");
										
									} else 
										config.setWorkerClass(scheduler.workerClass());
									
								} catch(Exception e) {
									if(!(e instanceof ClassNotFoundException))
										throw new SchedulerException(e.getMessage(), e);
								}
							} else 
								config.setWorkerClass(scheduler.workerClass());
							
							if(!StringUtils.isEmpty(scheduler.queueNameProperty().trim())) {
								String queueName = System.getProperty(scheduler.queueNameProperty().trim());
								if(!StringUtils.isEmpty(queueName)) {
									config.setQueueName(queueName);
								} else 
									config.setQueueName(scheduler.queueName());
								
							} else 
								config.setQueueName(scheduler.queueName());
							
							if(!StringUtils.isEmpty(scheduler.closeTimeoutProperty())) {
								try {
									long timeout = Long.parseLong(System.getProperty(scheduler.closeTimeoutProperty()));
									config.setTimeout(timeout);
								} catch(Exception e) { 
									config.setTimeout(scheduler.closeTimeout());
								}
							} else 
								config.setTimeout(scheduler.closeTimeout());
							
							/** set Machairodus private proerty   END */
							
							baseScheduler.setConfig(config);
							
							if(getInstance().stoppedScheduler.containsKey(config.getId())) {
								throw new SchedulerException("\n\t任务调度重复: " + config.getId() + ", 组件类: {'" + clz.getName() + "', '" + getInstance().stoppedScheduler.get(config.getId()).getClass().getName() +"'}");
							}
							
							getInstance().stoppedScheduler.put(config.getId(), baseScheduler);
							
							Set<BaseScheduler> groupScheduler = getInstance().group.get(baseScheduler.getConfig().getGroup());
							if(groupScheduler == null) groupScheduler = new LinkedHashSet<BaseScheduler>();
							groupScheduler.add(baseScheduler);
							getInstance().group.put(config.getGroup(), groupScheduler);
						}
					} catch(Exception e) {
						throw new SchedulerException("创建调度任务异常: " + e.getMessage());
					}
				} else 
					throw new SchedulerException("必须继承: [ "+BaseScheduler.class.getName()+" ]");
				
			}
			
			/** Create and start Etcd Scheduler */
			createEtcdScheduler(componentClasses);
		}
		
		isLoaded = true;
	}
	
	private static final void createEtcdScheduler(Set<Class<?>> componentClasses) {
		try {
			boolean enable = Boolean.parseBoolean(System.getProperty(EtcdScheduler.ETCD_ENABLE, "false"));
			if(enable) {
				EtcdScheduler scheduler = new EtcdScheduler(componentClasses);
				etcdScheduler = scheduler;
				scheduler.getConfig().getService().execute(scheduler);
				scheduler.syncBaseDirTTL();
				scheduler.syncInfo();
				scheduler.syncClass();
				
				/** Start Order Scheduler */
				EtcdOrderWatcherScheduler etcdOrderScheduler = new EtcdOrderWatcherScheduler(scheduler.getEtcd());
				etcdOrderScheduler.getConfig().getService().execute(etcdOrderScheduler);
				
				if(LocalJmxMonitorScheduler.JMX_ENABLE) {
					LocalJmxMonitorScheduler jmxScheduler = new LocalJmxMonitorScheduler(scheduler.getEtcd());
					jmxScheduler.getConfig().getService().execute(jmxScheduler);
				}
			} else 
				etcdScheduler = EtcdSchedulerOperate.EMPTY;
			
		} catch(SchedulerException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 重新加载调度任务
	 * @param injector Guice Injector
	 */
	public static final void reload() {
		getInstance().stoppedScheduler.clear();
		getInstance().closeAll();
		service.execute(new Runnable() {
			
			@Override
			public void run() {
				try { while(SchedulerFactory.getInstance().getStartedSchedulerSize() > 0) Thread.sleep(100L); } catch(InterruptedException e) { }
				if(LOG.isInfoEnabled())
					LOG.info("所有任务已经全部关闭");
				
				try {
					load();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				
			}
		});
		
	}
	
	public class StatusMonitorScheduler extends BaseScheduler {
		private final ConcurrentMap<String, BaseScheduler> closed;
		
		public StatusMonitorScheduler() {
			SchedulerConfig config = new SchedulerConfig();
			config.setId("StatusMonitorScheduler-0");
			config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "StatusMonitorScheduler-0");
			config.setGroup("StatusMonitorScheduler");
			threadFactory.setBaseScheduler(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			try { config.setCron(new CronExpression("* * * * * ?")); } catch(ParseException e) {}
			config.setTotal(1);
			config.setDaemon(true);
			setConfig(config);
			setClose(false);
			closed = new ConcurrentHashMap<String, BaseScheduler>();
		}
		
		@Override
		public void before() throws SchedulerException {
			for(Entry<String, BaseScheduler> entry : stoppingScheduler.entrySet()) {
				if(entry.getValue().isClosed()) {
					closed.put(entry.getKey(), entry.getValue());
				}
			}
		}

		@Override
		public void execute() throws SchedulerException {
			for(Entry<String, BaseScheduler> entry : closed.entrySet()) {
				String id = entry.getKey();
				BaseScheduler scheduler = entry.getValue();
				if(!scheduler.isRemove())
					stoppedScheduler.put(id, scheduler);
				
				stoppingScheduler.remove(id, scheduler);
				
				/** Sync to Etcd by stopped method */
				etcdScheduler.stopped(scheduler.getConfig().getGroup(), id, scheduler.isRemove(), scheduler.getAnalysis());
			}
			
			/** 删除在停止列表中被标记为remove的任务 */
			for(Iterator<Entry<String, BaseScheduler>> iter = stoppedScheduler.entrySet().iterator(); iter.hasNext(); ) {
				if(iter.next().getValue().isRemove()) {
					iter.remove();
				}
			}
		}

		@Override
		public void after() throws SchedulerException {
			closed.clear();
		}

		@Override
		public void destroy() throws SchedulerException {
			
		}
		
	}
	
	private class ShutdownHook implements Runnable {
		@Override
		public void run() {
			LOG.info("等待队列中的所有元素被执行完成后停止系统");
			while((int) BlockingQueueFactory.howManyElementInQueues() > 0) 
				try { Thread.sleep(10L); } catch(InterruptedException e) { }
			
			LOG.info("队列中的所有元素已被执行完成");
			
			long time = System.currentTimeMillis();
			LOG.info("开始停止任务调度");
			FACTORY.closeAll();
			Collection<BaseScheduler> schedulers = FACTORY.getStoppingScheduler();
			for(BaseScheduler scheduler : schedulers) {
				scheduler.thisNotify();
			}
			
			while((FACTORY.getStartedSchedulerSize() > 0 || FACTORY.getStoppingSchedulerSize() > 0) && System.currentTimeMillis() - time < 300000L) 
				try { Thread.sleep(10L); } catch(InterruptedException e) { }
			
 			LOG.info("停止任务调度完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		}
		
	}
	
}
