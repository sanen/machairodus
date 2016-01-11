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
package org.machairodus.controller.service.impl;

import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.RedisKeys;
import org.machairodus.controller.service.LoadBalanceService;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitor.MemoryUsage;
import org.machairodus.mappers.domain.JmxMonitorStatus;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

public class LoadBalanceServiceImpl implements LoadBalanceService {
	private Logger LOG = LoggerFactory.getLogger(LoadBalanceServiceImpl.class);
	
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private long timeout = Long.parseLong(System.getProperty("machairodus.controller.load-balancer.monitor.timeout", "10000"));
	private Map<String, Double> cpuRatios;
	private Comparator<JmxMonitor> comparator = (before, after) -> {
		int totalCpu = 0;
		if(!before.getHost().equals(after.getHost())) 
			totalCpu = cpuRatios.get(before.getHost()).compareTo(cpuRatios.get(after.getHost()));
		
		int cpu = before.getCpuRatio().compareTo(after.getCpuRatio());
		int thread = before.getThreadCount().compareTo(after.getThreadCount());
		int memory = before.getHeapMemoryUsage().get(MemoryUsage.FREE).compareTo(after.getHeapMemoryUsage().get(MemoryUsage.FREE));
		
		if(thread == 0) {
			if(totalCpu == 0) {
				if(cpu == 0) {
					return memory;
				
				} else 
					return cpu;
				
			} else 
				return totalCpu;
		
		} else 
			return thread;
		
	};
	
	@Override
	public Map<String, JmxMonitor> load(String key, TypeReference<JmxMonitor> type) {
		return redisClient.hgetAll(key, type);
	}
	
	@Override
	public void executeTimeout(String nodeType, Map<String, JmxMonitor> map) {
		if(!CollectionUtils.isEmpty(map)) {
			Iterator<Entry<String, JmxMonitor>> iterator = map.entrySet().iterator();
			iterator.forEachRemaining(entry -> {
				JmxMonitor monitor = entry.getValue();
				if(System.currentTimeMillis() - timeout > monitor.getUpdateTime()) {
					Socket socket = null;
					try {
						socket = new Socket(monitor.getHost(), monitor.getPort());
						if(socket.isConnected())
							socket.close();
						
						monitor.setStatus(JmxMonitorStatus.TIMEOUT);
					} catch (Exception e) {
						monitor.setStatus(JmxMonitorStatus.DOWN);
						
					} finally {
						socket = null;
					}
					
					redisClient.hset(nodeType, entry.getKey(), monitor);
					iterator.remove();
				}
			});
		}
	}

	@Override
	public void loadBalance(String nodeType, Map<String, JmxMonitor> map, Map<String, Double> cpuRatios) {
		if(!CollectionUtils.isEmpty(map)) {
			map.values().forEach(monitor -> {
				Double cpuRatio;
				if((cpuRatio = cpuRatios.get(monitor.getHost())) != null) {
					cpuRatio += monitor.getCpuRatio();
				} else 
					cpuRatio = monitor.getCpuRatio();
				
				cpuRatios.put(monitor.getHost(), cpuRatio);
			});
		}
	}
	
	@Override
	public void loadBalance0(String nodeType, Map<String, JmxMonitor> map, Map<String, Double> cpuRatios) {
		if(!CollectionUtils.isEmpty(map)) {
			this.cpuRatios = cpuRatios;
			List<JmxMonitor> monitors = Lists.newLinkedList(map.values());
			Collections.sort(monitors, comparator);
			List<Long> nodeList = Lists.newLinkedList();
			LOG.debug("\n");
			monitors.forEach(monitor -> {
				monitor.setTotalCpuRatio(cpuRatios.get(monitor.getHost()));
				LOG.debug(nodeType + ": " + monitor);
				nodeList.add(monitor.getId());
			});
			
			redisClient.hset(RedisKeys.JMX_MONITOR.value(), nodeType, nodeList);
		} else {
			redisClient.hdel(RedisKeys.JMX_MONITOR.value(), nodeType);
		}
	}
	
}
