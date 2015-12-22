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
package org.machairodus.topology.quartz;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
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
public class QuartzFactory {
	private static Logger LOG = LoggerFactory.getLogger(QuartzFactory.class);
	private static QuartzFactory FACTORY;
	private static final Object LOCK = new Object();
	private AtomicInteger startedQuartzSize = new AtomicInteger(0);
	private final ConcurrentMap<String , BaseQuartz> startedQuartz = new ConcurrentHashMap<String , BaseQuartz>();
	private final ConcurrentMap<String , BaseQuartz> stoppingQuartz = new ConcurrentHashMap<String , BaseQuartz>();
	private final ConcurrentMap<String , BaseQuartz> stoppedQuartz = new ConcurrentHashMap<String , BaseQuartz>();
	private final ConcurrentMap<String, Set<BaseQuartz>> group = new ConcurrentHashMap<String, Set<BaseQuartz>>();
	private static final QuartzThreadFactory threadFactory = new QuartzThreadFactory();
	private static final ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
	private static final ThreadPoolExecutor closeQuartzService = (ThreadPoolExecutor) new ThreadPoolExecutor(0, RuntimeUtil.AVAILABLE_PROCESSORS, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	private static final ThreadPoolExecutor closeQuartzCallableService = (ThreadPoolExecutor) new ThreadPoolExecutor(0, RuntimeUtil.AVAILABLE_PROCESSORS, 5L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	
	private static boolean isLoaded = false;
	
	public static final String BASE_PACKAGE = "context.quartz-scan.base-package";
	public static final String AUTO_RUN = "context.quartz.run.auto";
	public static final String MAX_POINTER = "context.statistic.max.pointer";
	public static final String INCLUDES = "context.quartz.group.includes";
	public static final String EXCLUSIONS = "context.quartz.group.exclusions";
	
	private QuartzFactory() {
		
	}
	
	public static final QuartzFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null) {
					FACTORY = new QuartzFactory();
					StatusMonitorQuartz statusMonitor = FACTORY.new StatusMonitorQuartz();
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
	 * @param quartz 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz bind(BaseQuartz quartz) {
		try {
			quartz.setClose(false);
			startedQuartz.put(quartz.getConfig().getId(), quartz);
			startedQuartzSize.incrementAndGet();
			return quartz;
		} finally {
			if(LOG.isInfoEnabled())
				LOG.info("绑定任务: 任务号[ " + quartz.getConfig().getId() + " ]");
			
		}
	}
	
	/**
	 * 解绑任务
	 * 
	 * @param quartz 任务
	 * @return 返回当前任务
	 */
	public BaseQuartz unbind(BaseQuartz quartz) {
		startedQuartz.remove(quartz.getConfig().getId());
		startedQuartzSize.decrementAndGet();
		
		if(LOG.isDebugEnabled())
			LOG.debug("解绑任务 : 任务号[ " + quartz.getConfig().getId() + " ], 现存任务数: " + startedQuartzSize.get());
		
		return quartz;
	}
	
	/**
	 * 获取现在正在执行的任务数
	 * @return 任务数
	 */
	public int getStartedQuartzSize() {
		return startedQuartzSize.get();
		
	}
	
	/**
	 * 返回所有任务
	 * @return 任务集合
	 */
	public Collection<BaseQuartz> getStartedQuartz() {
		return startedQuartz.values();
	}
	
	public int getStoppingQuartzSize() {
		return stoppingQuartz.size();
	}
	
	public Collection<BaseQuartz> getStoppingQuartz() {
		return stoppingQuartz.values();
	}
	
	public int getStopedQuartzSize() {
		return stoppedQuartz.size();
	}
	
	public Collection<BaseQuartz> getStopedQuratz() {
		return stoppedQuartz.values();
	}
	
	/**
	 * 关闭任务
	 * @param id 任务号
	 */
	public void close(final String id) {
		try {
			final BaseQuartz quartz = startedQuartz.get(id);
			close(quartz);
		} finally {
			if(LOG.isDebugEnabled())
				LOG.debug("关闭任务: 任务号[ " + id + " ]");
			
		}
	}
	
	public void close(final BaseQuartz quartz) {
		if(quartz != null && !quartz.isClose()) {
			if(quartz.getConfig().getWorkerClass() != BaseQuartz.class) {
				quartz.setClose(true);
				stoppingQuartz.put(quartz.getConfig().getId(), quartz);
				startedQuartz.remove(quartz.getConfig().getId(), quartz);
			} else {
				int size = 0;
				Set<BaseQuartz> dataLoaders = new LinkedHashSet<BaseQuartz>();
				for(BaseQuartz _quartz : startedQuartz.values()) {
					if(_quartz.getClass() == quartz.getClass()) {
						size ++;
					}
					
					if(_quartz.getConfig().getWorkerClass() == quartz.getClass()) {
						dataLoaders.add(_quartz);
					}
				}
				
				if(size > 1) {
					quartz.setClose(true);
					stoppingQuartz.put(quartz.getConfig().getId(), quartz);
					startedQuartz.remove(quartz.getConfig().getId(), quartz);
				} else if(size == 1) {
					for(BaseQuartz _quartz : dataLoaders) {
						_quartz.setClose(true);
						stoppingQuartz.put(_quartz.getConfig().getId(), _quartz);
						startedQuartz.remove(_quartz.getConfig().getId(), _quartz);
					}
					
					closeByQueue(quartz, quartz.getConfig().getQueueName());
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
		Set<BaseQuartz> dataLoaders = new LinkedHashSet<BaseQuartz>();
		for(BaseQuartz quartz : startedQuartz.values()) {
			if(quartz.getConfig().getGroup().equals(groupName)) {
				for(BaseQuartz _quartz : startedQuartz.values()) {
					if(_quartz.getConfig().getWorkerClass() == quartz.getClass()) {
						dataLoaders.add(_quartz);
					}
				}
				
				break;
			}
		}
		
		for(BaseQuartz quartz : dataLoaders) {
			quartz.setClose(true);
			stoppingQuartz.put(quartz.getConfig().getId(), quartz);
			startedQuartz.remove(quartz.getConfig().getId(), quartz);
		}
		
		for(BaseQuartz quartz : startedQuartz.values()) {
			if(quartz.getConfig().getGroup().equals(groupName)) {
				if(!"".equals(quartz.getConfig().getQueueName()))
					closeByQueue(quartz, quartz.getConfig().getQueueName());
				else {
					quartz.setClose(true);
					stoppingQuartz.put(quartz.getConfig().getId(), quartz);
					startedQuartz.remove(quartz.getConfig().getId(), quartz);
				}
			}
		}
	}
	
	/**
	 * 关闭所有任务
	 */
	public synchronized void closeAll() {
		if(startedQuartz.size() > 0) {
			LOG.warn("现在关闭所有的任务");
			Set<String> groupNames = new LinkedHashSet<String>();
			for(BaseQuartz quartz : startedQuartz.values()) {
				groupNames.add(quartz.getConfig().getGroup());
			}
			
			for(String groupName : groupNames) {
				closeGroup(groupName);
			}
		}
	}
	
	private void closeByQueue(final BaseQuartz quartz, final String queueName) {
		if(quartz.isClose())
			return ;
		
		if(StringUtils.isEmpty(queueName)) {
			quartz.setClose(true);
			stoppingQuartz.put(quartz.getConfig().getId(), quartz);
			startedQuartz.remove(quartz.getConfig().getId(), quartz);
			return ;
		}
		
		threadFactory.setBaseQuartz(null);
		closeQuartzService.execute(new Runnable() {
			@Override
			public void run() {
				Future<Boolean> future = closeQuartzCallableService.submit(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						while(BlockingQueueFactory.getInstance().getQueue(queueName).size() > 0) Thread.sleep(100L);
						return true;
					}
				});
				
				try {
					long timeout = quartz.getConfig().getTimeout();
					if(timeout <= 0) {
						LOG.warn("现在开始无限等待队列数据消费，请注意线程阻塞: " + quartz.getConfig().getId());
						future.get();
						LOG.info("队列数据消费结束: " + quartz.getConfig().getId());
						quartz.setClose(true);
						stoppingQuartz.put(quartz.getConfig().getId(), quartz);
						startedQuartz.remove(quartz.getConfig().getId(), quartz);
					} else {
						future.get(timeout, TimeUnit.MILLISECONDS);
						quartz.setClose(true);
						stoppingQuartz.put(quartz.getConfig().getId(), quartz);
						startedQuartz.remove(quartz.getConfig().getId(), quartz);
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
		if(stoppedQuartz.size() > 0) {
			for(Entry<String, BaseQuartz> entry : stoppedQuartz.entrySet()) {
				String name = entry.getKey();
				BaseQuartz quartz = entry.getValue();
				if(LOG.isInfoEnabled())
					LOG.info("Start quartz [ " + name + " ], class with [ " + quartz.getClass().getName() + " ]");
				
				getInstance().bind(quartz);
				threadFactory.setBaseQuartz(quartz);
				service.execute(quartz);
			}
			
			stoppedQuartz.clear();
		}
	}
	
	public final void startGroup(String groupName) {
		if(stoppedQuartz.size() > 0) {
			Set<String> keys = new HashSet<String>();
			for(Entry<String, BaseQuartz> entry : stoppedQuartz.entrySet()) {
				String id = entry.getKey();
				BaseQuartz quartz = entry.getValue();
				if(groupName.equals(quartz.getConfig().getGroup())) {
					if(quartz.isClose()) {
						if(LOG.isInfoEnabled())
							LOG.info("Start quartz [ " + id + " ], class with [ " + quartz.getClass().getName() + " ]");
						
						getInstance().bind(quartz);
						threadFactory.setBaseQuartz(quartz);
						service.execute(quartz);
						keys.add(id);
					}
				}
			}
			
			for(String key : keys) {
				stoppedQuartz.remove(key);
			}
		}
	}
	
	public final void start(String id) {
		BaseQuartz quartz = stoppedQuartz.get(id);
		if(quartz != null && quartz.isClose()) {
			if(LOG.isInfoEnabled())
				LOG.info("Start quartz [ " + id + " ], class with [ " + quartz.getClass().getName() + " ]");
			
			getInstance().bind(quartz);
			threadFactory.setBaseQuartz(quartz);
			service.execute(quartz);
			stoppedQuartz.remove(id);
		}
	}
	
	public final boolean closed(String id) {
		return stoppedQuartz.containsKey(id);
	}
	
	public final boolean started(String id) {
		return startedQuartz.containsKey(id);
	}
	
	public final boolean hasClosedGroup(String group) {
		if(stoppedQuartz.size() > 0) {
			for(BaseQuartz quartz : stoppedQuartz.values()) {
				if(quartz.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public final boolean hasStartedGroup(String group) {
		if(startedQuartz.size() > 0) {
			for(BaseQuartz quartz : startedQuartz.values()) {
				if(quartz.getConfig().getGroup().equals(group))
					return true;
			}
		}
		
		return false;
	}
	
	public final void addQuartz(BaseQuartz quartz) {
		Set<BaseQuartz> groupQuartz = group.get(quartz.getConfig().getGroup());
		if(groupQuartz == null) groupQuartz = new LinkedHashSet<BaseQuartz>();
		groupQuartz.add(quartz);
		group.put(quartz.getConfig().getGroup(), groupQuartz);
		
		if(stoppedQuartz.containsKey(quartz.getConfig().getId()) || startedQuartz.containsKey(quartz.getConfig().getId()))
			throw new QuartzException("exists quartz in memory");
		
		stoppedQuartz.put(quartz.getConfig().getId(), quartz);
		rebalance(quartz.getConfig().getGroup());
	}
	
	public final void removeQuartz(BaseQuartz quartz) {
		Set<BaseQuartz> groupQuartz = group.get(quartz.getConfig().getGroup());
		getInstance().close(quartz.getConfig().getId());
		
		if(groupQuartz.size() > 1) {
			groupQuartz.remove(quartz);
			stoppedQuartz.remove(quartz.getConfig().getId(), quartz);
		}
		
		rebalance(quartz.getConfig().getGroup());
	}
	
	public final void removeQuartz(String groupName) {
		BaseQuartz quartz = findLast(groupName);
		if(quartz != null) {
			removeQuartz(quartz);
		}
	}
	
	public final int getGroupSize(String groupName) {
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz))
			return groupQuartz.size();
		
		return 0;
	}
	
	public final BaseQuartz findLast(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz)) {
			for(BaseQuartz quartz : groupQuartz) {
				if(quartz.getConfig().getNum() + 1 == quartz.getConfig().getTotal())
					return quartz;
			}
		}
		
		return null;
	}
	
	public final void rebalance(String groupName) {
		Assert.hasLength(groupName);
		Set<BaseQuartz> groupQuartz = group.get(groupName);
		if(!CollectionUtils.isEmpty(groupQuartz)) {
			for(BaseQuartz quartz : groupQuartz) quartz.getConfig().setTotal(groupQuartz.size());
		}
	}
	
	/**
	 * 加载任务调度
	 * @param injector Guice Injector
	 * @throws IllegalArgumentException 非法的参数列表
	 * @throws IllegalAccessException ?
	 */
	@SuppressWarnings("unchecked")
	public static final void load(Properties properties) throws IllegalArgumentException, IllegalAccessException {
		if(isLoaded) 
			throw new QuartzException("Quartz已经加载，这里不再进行重复的加载，如需重新加载请调用reload方法");

		String _package = properties.getProperty(BASE_PACKAGE);
		if(_package == null || _package.isEmpty())
			throw new QuartzException("Property '" + BASE_PACKAGE + "' must not be null.");
		
		String[] packages = _package.split(",");
		for(String pkg : packages) {
			if(!pkg.isEmpty()) {
				ComponentScan.scan(pkg);
			}
		}
		
		Set<Class<?>> componentClasses = ComponentScan.filter(Quartz.class);
		if(LOG.isInfoEnabled())
			LOG.info("Quartz size: " + componentClasses.size());
		
		if(componentClasses.size() > 0) {
			String[] includes = properties.getProperty(INCLUDES, ".").split(",");
			String[] exclusions;
			if(!StringUtils.isEmpty(properties.getProperty(EXCLUSIONS)))
				exclusions = properties.getProperty(EXCLUSIONS).split(",");
			else 
				exclusions = new String[0];
			
			for(Class<?> clz : componentClasses) {
				if(BaseQuartz.class.isAssignableFrom(clz)) {
					if(LOG.isInfoEnabled())
						LOG.info("Inject Quartz Class: " + clz.getName());
					
					Quartz quartz = clz.getAnnotation(Quartz.class);
					if(quartz.name() == null && quartz.name().isEmpty()) 
						throw new QuartzException("任务名不能为空, 类名 [ " + clz.getName()+ " ]");
					
					if(!ObjectCompare.isInListByRegEx(quartz.name(), includes) || ObjectCompare.isInListByRegEx(quartz.name(), exclusions)) {
						LOG.warn("过滤任务组: " + quartz.name() + ", 类名 [ " + clz.getName()+ " ]");
						continue ;
					}
					
					String parallelProperty = quartz.parallelProperty();
					int parallel = 0;
					String cron = "";
					String value;
					if((value = properties.getProperty(parallelProperty)) != null && !value.isEmpty()) {
						/** 采用最后设置的属性作为最终结果 */
						try {
							parallel = Integer.parseInt(value);
						} catch(NumberFormatException e) { 
							throw new QuartzException("并行度属性设置错误, 属性名: [ " + parallelProperty + " ], 属性值: [ " + value + " ]");
						}
					}
					
					if((value = properties.getProperty(quartz.cronProperty())) != null && !value.isEmpty())
						cron = value;
					
					parallel = quartz.coreParallel() ? RuntimeUtil.AVAILABLE_PROCESSORS : parallel > 0 ? parallel : quartz.parallel();
					if(parallel < 0)
						parallel = 0;
					
					if(StringUtils.isEmpty(cron))
						cron = quartz.cron();
					
					try {
						for(int p = 0; p < parallel; p ++) {
							BaseQuartz baseQuartz = (BaseQuartz) clz.newInstance();
							QuartzConfig config = new QuartzConfig();
							config.setId(quartz.name() + "-" + p);
							config.setName("Quartz-Thread-Pool: " + quartz.name() + "-" + p);
							config.setGroup(quartz.name());
							config.setService(service);
							config.setBeforeAfterOnly(quartz.beforeAfterOnly());
							config.setRunNumberOfTimes(quartz.runNumberOfTimes());
							config.setInterval(quartz.interval());
							config.setNum(p);
							config.setTotal(parallel);
							if(!StringUtils.isEmpty(cron))
								try { config.setCron(new CronExpression(cron)); } catch(ParseException e) { throw new QuartzException(e.getMessage(), e); }
						
							config.setLazy(quartz.lazy());
							config.setDaemon(quartz.daemon());
							
							/** -------------------             set Machairodus private proerty   START       ------------- **/
							if(!StringUtils.isEmpty(quartz.workerClassProperty().trim())) {
								try {
									String className = properties.getProperty(quartz.workerClassProperty().trim());
									if(!StringUtils.isEmpty(className)) {
										Class<?> cls = Class.forName(className);
										if(BaseQuartz.class.isAssignableFrom(cls))
											config.setWorkerClass((Class<? extends BaseQuartz>) cls);
										else 
											throw new QuartzException("无效的workClass属性配置，workClass必须继承BaseQuartz");
										
									} else 
										config.setWorkerClass(quartz.workerClass());
									
								} catch(Exception e) {
									if(!(e instanceof ClassNotFoundException))
										throw new QuartzException(e.getMessage(), e);
								}
							} else 
								config.setWorkerClass(quartz.workerClass());
							
							if(!StringUtils.isEmpty(quartz.queueNameProperty().trim())) {
								String queueName = properties.getProperty(quartz.queueNameProperty().trim());
								if(!StringUtils.isEmpty(queueName)) {
									config.setQueueName(queueName);
								} else 
									config.setQueueName(quartz.queueName());
								
							} else 
								config.setQueueName(quartz.queueName());
							
							if(!StringUtils.isEmpty(quartz.closeTimeoutProperty())) {
								try {
									long timeout = Long.parseLong(properties.getProperty(quartz.closeTimeoutProperty()));
									config.setTimeout(timeout);
								} catch(Exception e) { 
									config.setTimeout(quartz.closeTimeout());
								}
							} else 
								config.setTimeout(quartz.closeTimeout());
							
							/** -------------------             set Machairodus private proerty   END          ------------- **/
							
							baseQuartz.setConfig(config);
							
							if(getInstance().stoppedQuartz.containsKey(quartz.name() + "-" + p)) {
								throw new QuartzException("\n\t任务调度重复: " + quartz.name() + "-" + p + ", 组件类: {'" + clz.getName() + "', '" + getInstance().stoppedQuartz.get(quartz.name() + "-" + p).getClass().getName() +"'}");
							}
							
							getInstance().stoppedQuartz.put(config.getId(), baseQuartz);
							
							Set<BaseQuartz> groupQuartz = getInstance().group.get(baseQuartz.getConfig().getGroup());
							if(groupQuartz == null) groupQuartz = new LinkedHashSet<BaseQuartz>();
							groupQuartz.add(baseQuartz);
							getInstance().group.put(config.getGroup(), groupQuartz);
						}
					} catch(Exception e) {
						throw new QuartzException("创建调度任务异常: " + e.getMessage());
					}
				} else 
					throw new QuartzException("必须继承: [ "+BaseQuartz.class.getName()+" ]");
				
			}
			
		}
		
		isLoaded = true;
	}
	
	/**
	 * 重新加载调度任务
	 * @param injector Guice Injector
	 */
	public static final void reload(final Properties properties) {
		getInstance().stoppedQuartz.clear();
		getInstance().closeAll();
		service.execute(new Runnable() {
			
			@Override
			public void run() {
				try { while(QuartzFactory.getInstance().getStartedQuartzSize() > 0) Thread.sleep(100L); } catch(InterruptedException e) { }
				if(LOG.isInfoEnabled())
					LOG.info("所有任务已经全部关闭");
				
				try {
					load(properties);
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				
			}
		});
		
	}
	
	private class StatusMonitorQuartz extends BaseQuartz {
		private final ConcurrentMap<String, BaseQuartz> closed;
		
		public StatusMonitorQuartz() {
			QuartzConfig config = new QuartzConfig();
			config.setId("StatusMonitorQuartz-0");
			config.setName("StatusMonitorQuartz");
			config.setGroup("StatusMonitorQuartz");
			threadFactory.setBaseQuartz(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			try { config.setCron(new CronExpression("* * * * * ?")); } catch(ParseException e) {}
			config.setTotal(1);
			config.setDaemon(true);
			setConfig(config);
			setClose(false);
			closed = new ConcurrentHashMap<String, BaseQuartz>();
		}
		
		@Override
		public void before() throws QuartzException {
			for(Entry<String, BaseQuartz> entry : stoppingQuartz.entrySet()) {
				if(entry.getValue().isClosed()) {
					closed.put(entry.getKey(), entry.getValue());
				}
			}
		}

		@Override
		public void execute() throws QuartzException {
			for(Entry<String, BaseQuartz> entry : closed.entrySet()) {
				String id = entry.getKey();
				BaseQuartz quartz = entry.getValue();
				stoppedQuartz.put(id, quartz);
				stoppingQuartz.remove(id, quartz);
			}
		}

		@Override
		public void after() throws QuartzException {
			closed.clear();
		}

		@Override
		public void destroy() throws QuartzException {
			
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
			Collection<BaseQuartz> quartzs = FACTORY.getStoppingQuartz();
			for(BaseQuartz quartz : quartzs) {
				quartz.thisNotify();
			}
			
			while((FACTORY.getStartedQuartzSize() > 0 || FACTORY.getStoppingQuartzSize() > 0) && System.currentTimeMillis() - time < 300000L) 
				try { Thread.sleep(10L); } catch(InterruptedException e) { }
			
 			LOG.info("停止任务调度完成, 耗时: " + (System.currentTimeMillis() - time) + "ms");
		}
		
	}
}
