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
package org.machairodus.topology.scheduler.defaults.monitor;

import static org.machairodus.topology.scheduler.SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX;
import static org.machairodus.topology.scheduler.SchedulerFactory.threadFactory;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.machairodus.topology.scheduler.BaseScheduler;
import org.machairodus.topology.scheduler.SchedulerConfig;
import org.machairodus.topology.scheduler.SchedulerException;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdScheduler;
import org.machairodus.topology.scheduler.defaults.monitor.JmxMonitor.MemoryUsage;
import org.machairodus.topology.util.CryptUtil;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;


/**
 * 使用组件进行初始化，而非启动时初始化
 * 
 * @author yanghe
 * @date 2016年1月8日 上午9:46:03
 */
public class LocalJmxMonitorScheduler extends BaseScheduler {
	private final EtcdClient etcd;
	
	public static final String JMX_KEY = EtcdScheduler.DIR + "/Jmx.store";
	public static final int JMX_RATE = Integer.parseInt(System.getProperty("context.scheduler.app.jmx.rate", "5"));
	public static final boolean JMX_ENABLE = Boolean.parseBoolean(System.getProperty("context.scheduler.app.jmx.enable", "false"));
	
	public LocalJmxMonitorScheduler(EtcdClient etcd) {
		this.etcd = etcd;
		
		SchedulerConfig config = new SchedulerConfig();
		config.setId("LocalJmxMonitorScheduler-0");
		config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "LocalJmxMonitorScheduler-0");
		config.setGroup("LocalJmxMonitorScheduler");
		threadFactory.setBaseScheduler(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
		config.setTotal(1);
		config.setDaemon(true);
		config.setBeforeAfterOnly(true);
		setConfig(config);
		setClose(false);
	}
	
	@Override
	public void before() throws SchedulerException {
		
	}

	@SuppressWarnings("restriction")
	@Override
	public void execute() throws SchedulerException {
		try {
			JmxMonitor monitor = new JmxMonitor();
			
			/** ClassLoading */
			ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
			monitor.setLoadedClassCount(classLoading.getLoadedClassCount());
			monitor.setUnloadedClassCount(classLoading.getUnloadedClassCount());
			monitor.setTotalLoadedClassCount(classLoading.getTotalLoadedClassCount());
			
			/** Memory */
			MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
			java.lang.management.MemoryUsage heapMemoryUsage = memory.getHeapMemoryUsage();
			Map<MemoryUsage, Long> heap = new HashMap<MemoryUsage, Long>();
			long used, max;
			heap.put(MemoryUsage.INIT, heapMemoryUsage.getInit() / 1000000);
			heap.put(MemoryUsage.USED, used = heapMemoryUsage.getUsed() / 1000000);
			heap.put(MemoryUsage.COMMITTED, heapMemoryUsage.getCommitted() / 1000000);
			heap.put(MemoryUsage.MAX, max = heapMemoryUsage.getMax() / 1000000);
			heap.put(MemoryUsage.FREE, max - used);
			monitor.setHeapMemoryUsage(heap);
			
			/** Threading */
			ThreadMXBean thread = ManagementFactory.getThreadMXBean();
			monitor.setTotalStartedThreadCount(thread.getTotalStartedThreadCount());
			monitor.setThreadCount(thread.getThreadCount());
			monitor.setDaemonThreadCount(thread.getDaemonThreadCount());
			monitor.setPeakThreadCount(thread.getPeakThreadCount());
			
			/** OS */
			com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			monitor.setCpuRatio(cpuRatio(JMX_RATE * 1000, true, os));
			
			/** User defined */
			monitor.setTps(Statistic.getInstance().setAndGetPointer(JMX_RATE));
			
			etcd.put(JMX_KEY, CryptUtil.encrypt(monitor.toString(), EtcdScheduler.SYSTEM_ID)).send().get();
		} catch(Throwable e) {
			LOG.error(e.getMessage(), e);
			thisWait(1000);
		}
		
	}

	@Override
	public void after() throws SchedulerException {
		
	}

	@Override
	public void destroy() throws SchedulerException {
		
	}

	@SuppressWarnings("restriction")
	public double cpuRatio(long time, boolean ifAvaProc, com.sun.management.OperatingSystemMXBean os) {
		Long start = System.currentTimeMillis();  
        long startT = os.getProcessCpuTime();  
        try { Thread.sleep(time); } catch (InterruptedException e) { }
        Long end = System.currentTimeMillis();  
        long endT = os.getProcessCpuTime();  
        double ratio = (endT - startT) / 1000000.0 / (end - start);
        if(ifAvaProc)
        	ratio /= os.getAvailableProcessors();
        
        BigDecimal decimal = new BigDecimal(ratio * 100);
        return decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        
	}
	
}
