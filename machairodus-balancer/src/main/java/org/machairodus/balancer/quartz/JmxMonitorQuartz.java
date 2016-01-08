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

import java.rmi.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.RedisKeys;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitor.MemoryUsage;
import org.machairodus.mappers.domain.NodeConfig;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.QuartzConfig;
import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.JmxClientManager;
import org.nanoframework.jmx.client.management.ClassLoadingMXBean;
import org.nanoframework.jmx.client.management.MemoryMXBean;
import org.nanoframework.jmx.client.management.OperatingSystemMXBean;
import org.nanoframework.jmx.client.management.RuntimeMXBean;
import org.nanoframework.jmx.client.management.ThreadMXBean;
import org.nanoframework.jmx.client.management.impl.ClassLoadingImpl;
import org.nanoframework.jmx.client.management.impl.MemoryImpl;
import org.nanoframework.jmx.client.management.impl.OperatingSystemImpl;
import org.nanoframework.jmx.client.management.impl.RuntimeImpl;
import org.nanoframework.jmx.client.management.impl.ThreadImpl;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.google.common.collect.Maps;

/**
 * 使用组件进行初始化，而非启动时初始化
 * 
 * @author yanghe
 * @date 2016年1月8日 上午9:46:03
 */
public class JmxMonitorQuartz extends BaseQuartz {
	private static final ConcurrentMap<Long, NodeConfig> nodeMap = new ConcurrentHashMap<>();
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private static final String JMX_MONITOR = RedisKeys.JMX_MONITOR.value().getKey();
	private final NodeConfig nodeConfig;
	private JmxClient jmxClient;
	private String address;
	
	public JmxMonitorQuartz(QuartzConfig config, NodeConfig nodeConfig) {
		Assert.notNull(config, "QuartzConfig must be not null.");
		Assert.notNull(nodeConfig, "NodeConfig must be not null.");
		Assert.hasLength(nodeConfig.getServerAddress(), "ServerAddress must be not empty.");
		Assert.notNull(nodeConfig.getJmxPort(), "JmxPort must be not null.");
		
		if(nodeMap.get(nodeConfig.getId()) != null)
			throw new QuartzException("Exists NodeConfig JmxMonitor");
		
		setConfig(config);
		this.nodeConfig = nodeConfig;
		
		jmxClient = JmxClientManager.get((address = nodeConfig.getServerAddress() + ":" + nodeConfig.getJmxPort()));
		nodeMap.put(nodeConfig.getId(), nodeConfig);
	}
	
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		JmxMonitor monitor = new JmxMonitor();
		try {
			/** Runtime */
			RuntimeMXBean runtime = new RuntimeImpl(jmxClient);
			monitor.setUptime(runtime.getUptime());
			monitor.setName(runtime.getName());
			monitor.setPid(monitor.getName().split("@")[0]);
			monitor.setStartTime(runtime.getStartTime());
			
			/** ClassLoading */
			ClassLoadingMXBean classLoading = new ClassLoadingImpl(jmxClient);
			monitor.setLoadedClassCount(classLoading.getLoadedClassCount());
			monitor.setUnloadedClassCount(classLoading.getUnloadedClassCount());
			monitor.setTotalLoadedClassCount(classLoading.getTotalLoadedClassCount());
			
			/** Memory */
			MemoryMXBean memory = new MemoryImpl(jmxClient);
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
			ThreadMXBean thread = new ThreadImpl(jmxClient);
			monitor.setTotalStartedThreadCount(thread.getTotalStartedThreadCount());
			monitor.setThreadCount(thread.getThreadCount());
			monitor.setDaemonThreadCount(thread.getDaemonThreadCount());
			monitor.setPeakThreadCount(thread.getPeakThreadCount());
//			monitor.setAllThreadIds(thread.getAllThreadIds());
			
			/** OS */
			OperatingSystemMXBean os = new OperatingSystemImpl(jmxClient);
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
			monitor.setCpuRatio(os.cpuRatio());
			redisClient.set(JMX_MONITOR + ":" + address, monitor);
			redisClient.expire(JMX_MONITOR + ":" + address, 10);
		} catch(Throwable e) {
			if(e.getCause() != null && e.getCause() instanceof ConnectException) {
				try { 
					jmxClient.reconnect(); 
				} catch(ConnectException ex) { 
					LOG.error("Reconnect Error: " + ex.getMessage());
					redisClient.del(JMX_MONITOR + ":" + address);
					thisWait(1000);
				}
			}
		}
		
	}

	@Override
	public void after() throws QuartzException {
		
	}

	@Override
	public void destroy() throws QuartzException {
		redisClient.del(JMX_MONITOR + ":" + address);
		jmxClient.close();
		JmxClientManager.remove(address, jmxClient);
		nodeMap.remove(nodeConfig.getId(), nodeConfig);
	}

	public NodeConfig getNodeConfig() {
		return nodeConfig;
	}
	
	public Long getNodeConfigId() {
		return nodeConfig.getId();
	}
}
