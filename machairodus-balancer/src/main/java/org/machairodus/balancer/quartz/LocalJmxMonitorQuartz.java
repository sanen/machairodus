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
package org.machairodus.balancer.quartz;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.RedisKeys;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitor.MemoryUsage;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.Quartz;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.common.collect.Maps;

@Quartz(name = "LocalJmxMonitorQuartz", beforeAfterOnly = true, parallel = 1)
public class LocalJmxMonitorQuartz extends BaseQuartz {
	private Logger LOG = LoggerFactory.getLogger(LocalJmxMonitorQuartz.class);
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private static final String JMX_MONITOR = RedisKeys.JMX_MONITOR.value().getKey();
	private final String BALANCER = "BALANCER:";
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		JmxMonitor monitor = new JmxMonitor();
		try {
			/** Runtime */
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			monitor.setUptime(runtime.getUptime());
			monitor.setName(runtime.getName());
			monitor.setPid(monitor.getName().split("@")[0]);
			monitor.setStartTime(runtime.getStartTime());
			
			/** ClassLoading */
			ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
			monitor.setLoadedClassCount(classLoading.getLoadedClassCount());
			monitor.setUnloadedClassCount(classLoading.getUnloadedClassCount());
			monitor.setTotalLoadedClassCount(classLoading.getTotalLoadedClassCount());
			
			/** Memory */
			MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
			java.lang.management.MemoryUsage heapMemoryUsage = memory.getHeapMemoryUsage();
			Map<MemoryUsage, Long> heap = Maps.newHashMap();
			heap.put(MemoryUsage.INIT, heapMemoryUsage.getInit());
			heap.put(MemoryUsage.USED, heapMemoryUsage.getUsed());
			heap.put(MemoryUsage.COMMITTED, heapMemoryUsage.getCommitted());
			heap.put(MemoryUsage.MAX, heapMemoryUsage.getMax());
			monitor.setHeapMemoryUsage(heap);
			
			java.lang.management.MemoryUsage nonHeapMemoryUsage = memory.getNonHeapMemoryUsage();
			Map<MemoryUsage, Long> nonHeap = Maps.newHashMap();
			nonHeap.put(MemoryUsage.INIT, nonHeapMemoryUsage.getInit());
			nonHeap.put(MemoryUsage.USED, nonHeapMemoryUsage.getUsed());
			nonHeap.put(MemoryUsage.COMMITTED, nonHeapMemoryUsage.getCommitted());
			nonHeap.put(MemoryUsage.MAX, nonHeapMemoryUsage.getMax());
			monitor.setNonHeapMemoryUsage(nonHeap);
			
			/** Threading */
			ThreadMXBean thread = (ThreadMXBean) ManagementFactory.getThreadMXBean();
			monitor.setTotalStartedThreadCount(thread.getTotalStartedThreadCount());
			monitor.setThreadCount(thread.getThreadCount());
			monitor.setDaemonThreadCount(thread.getDaemonThreadCount());
			monitor.setPeakThreadCount(thread.getPeakThreadCount());
//			monitor.setAllThreadIds(thread.getAllThreadIds());
			
			/** OS */
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			monitor.setOs(os.getName());
//			monitor.setFreePhysicalMemorySize(os.getFreePhysicalMemorySize());
//			monitor.setFreeSwapSpaceSize(os.getFreeSwapSpaceSize());
//			monitor.setTotalPhysicalMemorySize(os.getTotalPhysicalMemorySize());
//			monitor.setTotalSwapSpaceSize(os.getTotalSwapSpaceSize());
//			monitor.setCommiteedVirtualMemorySize(os.getCommittedVirtualMemorySize());
			monitor.setAvailableProcessors(os.getAvailableProcessors());
//			monitor.setProcessCpuLoad(os.getProcessCpuLoad());
//			monitor.setSystemCpuLoad(os.getSystemCpuLoad());
			monitor.setSystemLoadAverage(os.getSystemLoadAverage());
//			monitor.setProcessCpuTime(os.getProcessCpuTime());
			monitor.setCpuRatio(cpuRatio(1000, true));
			
			redisClient.set(JMX_MONITOR + ":" + BALANCER + monitor.getName(), monitor);
			redisClient.expire(JMX_MONITOR + ":" + BALANCER + monitor.getName(), 10);
		} catch(Throwable e) {
			LOG.error("LocalJmxMonitor.execute error: " + e.getMessage());
			redisClient.del(JMX_MONITOR + ":" + BALANCER + monitor.getName());
			thisWait(1000);
		}
	}

	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {
		redisClient.del(JMX_MONITOR + ":" + BALANCER + ManagementFactory.getRuntimeMXBean().getName());
	}
	
	@SuppressWarnings("restriction")
	private double cpuRatio(long time, boolean ifAvaProc) {
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
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
