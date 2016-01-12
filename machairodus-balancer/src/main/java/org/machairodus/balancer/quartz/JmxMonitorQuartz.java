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
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitor.MemoryUsage;
import org.machairodus.mappers.domain.JmxMonitorStatus;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.NodeType;
import org.machairodus.mappers.mapper.balancer.ConfigureNodeMapper;
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
import com.google.inject.Inject;

/**
 * 使用组件进行初始化，而非启动时初始化
 * 
 * @author yanghe
 * @date 2016年1月8日 上午9:46:03
 */
public class JmxMonitorQuartz extends BaseQuartz {
	private static final ConcurrentMap<Long, NodeConfig> nodeMap = new ConcurrentHashMap<>();
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private NodeConfig node;
	private JmxClient jmxClient;
	private String address;
	
	@Inject
	private ConfigureNodeMapper nodeMapper;
	
	public JmxMonitorQuartz() {
		
	}
	
	public JmxMonitorQuartz(QuartzConfig config, NodeConfig nodeConfig) {
		Assert.notNull(config, "QuartzConfig must be not null.");
		setConfig(config);
		setNode(nodeConfig);
		connect(nodeConfig);
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
			String[] rt = monitor.getName().split("@");
			monitor.setHostName(rt[1]);
			monitor.setPid(rt[0]);
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
			long used, max;
			heap.put(MemoryUsage.INIT, heapMemoryUsage.getInit() / 1000000);
			heap.put(MemoryUsage.USED, used = heapMemoryUsage.getUsed() / 1000000);
			heap.put(MemoryUsage.COMMITTED, heapMemoryUsage.getCommitted() / 1000000);
			heap.put(MemoryUsage.MAX, max = heapMemoryUsage.getMax() / 1000000);
			heap.put(MemoryUsage.FREE, max - used);
			monitor.setHeapMemoryUsage(heap);
			
			java.lang.management.MemoryUsage nonHeapMemoryUsage = memory.getNonHeapMemoryUsage();
			Map<MemoryUsage, Long> nonHeap = Maps.newHashMap();
			nonHeap.put(MemoryUsage.INIT, nonHeapMemoryUsage.getInit() / 1000000);
			nonHeap.put(MemoryUsage.USED, used = nonHeapMemoryUsage.getUsed() / 1000000);
			nonHeap.put(MemoryUsage.COMMITTED, nonHeapMemoryUsage.getCommitted() / 1000000);
			nonHeap.put(MemoryUsage.MAX, max = nonHeapMemoryUsage.getMax() / 1000000);
			nonHeap.put(MemoryUsage.FREE, max - used);
			monitor.setNonHeapMemoryUsage(nonHeap);
			
			/** Threading */
			ThreadMXBean thread = new ThreadImpl(jmxClient);
			monitor.setTotalStartedThreadCount(thread.getTotalStartedThreadCount());
			monitor.setThreadCount(thread.getThreadCount());
			monitor.setDaemonThreadCount(thread.getDaemonThreadCount());
			monitor.setPeakThreadCount(thread.getPeakThreadCount());
			
			/** OS */
			OperatingSystemMXBean os = new OperatingSystemImpl(jmxClient);
			monitor.setOs(os.getName());
			monitor.setAvailableProcessors(os.getAvailableProcessors());
			monitor.setCpuRatio(os.cpuRatio(2000));
			
			/** Set update time */
			monitor.setUpdateTime(System.currentTimeMillis());
			monitor.setStatus(JmxMonitorStatus.RUNNING);
			monitor.setNodeName(address);
			monitor.setId(node.getId());
			String[] hosts = address.split(":");
			monitor.setHost(hosts[0]);
			monitor.setPort(Integer.parseInt(hosts[1]));
			
			redisClient.hset(NodeType.value(node.getType()).name(), node.getId().toString(), monitor);
			
			String pid;
			if((pid = redisClient.hget("PID", node.getId().toString())) != null) {
				if(!pid.equals(monitor.getPid())) {
					nodeMapper.updatePID(Integer.parseInt(monitor.getPid()), node.getId());
					redisClient.hset("PID", node.getId().toString(), monitor.getPid());
				}
			} else {
				nodeMapper.updatePID(Integer.parseInt(monitor.getPid()), node.getId());
				redisClient.hset("PID", node.getId().toString(), monitor.getPid());
			}
		} catch(Throwable e) {
			if(e.getCause() != null && e.getCause() instanceof ConnectException) {
				try { 
					jmxClient.reconnect(); 
				} catch(ConnectException ex) { 
					LOG.error("Reconnect Error: " + ex.getMessage());
					redisClient.hdel(NodeType.value(node.getType()).name(), address);
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
		try {
			redisClient.hdel(NodeType.value(node.getType()).name(), address);
			jmxClient.close();
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		JmxClientManager.remove(address, jmxClient);
		nodeMap.remove(node.getId(), node);
	}

	public NodeConfig getNodeConfig() {
		return node;
	}
	
	public Long getNodeConfigId() {
		return node.getId();
	}
	
	public void setNode(NodeConfig node) {
		Assert.notNull(node, "NodeConfig must be not null.");
		Assert.notNull(node.getId(), "NodeConfig ID must be not null.");
		Assert.hasLength(node.getServerAddress(), "ServerAddress must be not empty.");
		Assert.notNull(node.getJmxPort(), "JmxPort must be not null.");
		this.node = node;
		
		if(nodeMap.get(node.getId()) != null)
			throw new QuartzException("Exists NodeConfig JmxMonitor");
	}
	
	public void connect(NodeConfig node) {
		jmxClient = JmxClientManager.get((address = node.getServerAddress() + ":" + node.getJmxPort()));
		nodeMap.put(node.getId(), node);
	}
}
