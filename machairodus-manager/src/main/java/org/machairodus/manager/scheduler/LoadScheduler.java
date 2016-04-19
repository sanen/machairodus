/**
 * Copyright 2015 the original author or authors.
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
package org.machairodus.manager.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.manager.service.MonitorType;
import org.machairodus.manager.websocket.MonitorHandler;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitor.MemoryUsage;
import org.machairodus.mappers.domain.NodeType;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.Scheduler;
import org.nanoframework.extension.websocket.ChannelGroupSupport;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Scheduler(beforeAfterOnly = true, cron = "* * * * * ?", parallel = 1)
public class LoadScheduler extends BaseScheduler {
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private TypeReference<JmxMonitor> typeReference = new TypeReference<JmxMonitor>() { };
	
	@Override
	public void before() {

	}

	@Override
	public void execute() {
		Map<String, Map<String, String>> servers = new HashMap<>();
		ChannelGroupSupport.GROUP.entrySet().parallelStream().forEach(entry -> {
			String key = entry.getKey();
			if(!key.endsWith(MonitorType.LOAD.value()))
				return ;
			
			List<ChannelGroupSupport> supports = entry.getValue();
			if(!CollectionUtils.isEmpty(supports)) {
				Map<String, String> map = MonitorHandler.get(key);
				String sid = map.get("sid");
				String id = map.get("id");
				if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(sid))
					servers.put(id, map);
			}
		});
		
		ConcurrentMap<String, Map<String, Object>> responses = new ConcurrentHashMap<>();
		servers.entrySet().parallelStream().forEach(server -> {
			String id = server.getKey();
			Map<String, String> map = server.getValue();
			Map<String, Object> response = new HashMap<>();
			try {
				JmxMonitor monitor = redisClient.hget(NodeType.value(Integer.parseInt(map.get("nodeType"))).name(), id, typeReference);
				response.put("TIME", System.currentTimeMillis());
				response.put("CPU", monitor.getCpuRatio());
				
				Map<String, Long> memory = new HashMap<>();
				Map<MemoryUsage, Long> heap = monitor.getHeapMemoryUsage();
				memory.put("COMMITED", heap.get(MemoryUsage.COMMITTED));
				memory.put("USED", heap.get(MemoryUsage.USED));
				response.put("MEMORY", memory);
				
				Map<String, Long> classload = new HashMap<>();
				classload.put("LOADED", monitor.getTotalLoadedClassCount());
				classload.put("UNLOADED", monitor.getUnloadedClassCount());
				response.put("CLASSLOAD", classload);
				
				Map<String, Integer> thread = new HashMap<>();
				thread.put("THREAD_COUNT", monitor.getThreadCount());
				thread.put("DAEMON_THREAD_COUNT", monitor.getDaemonThreadCount());
				response.put("THREAD", thread);
			} catch(Exception e) {
				LOG.error("获取信息异常：" + e.getMessage());
				
				response.put("TIME", System.currentTimeMillis());
				response.put("CPU", 0);
				
				Map<String, Long> _memory = new HashMap<>();
				_memory.put("COMMITED", 0L);
				_memory.put("USED", 0L);
				response.put("MEMORY", _memory);
				
				Map<String, Long> _classload = new HashMap<>();
				_classload.put("LOADED", 0L);
				_classload.put("UNLOADED", 0L);
				response.put("CLASSLOAD", _classload);
				
				Map<String, Integer> _thread = new HashMap<>();
				_thread.put("THREAD_COUNT", 0);
				_thread.put("DAEMON_THREAD_COUNT", 0);
				response.put("THREAD", _thread);
			}
			
			responses.put(server.getValue().get("sid"), response);
		});
		
		ChannelGroupSupport.GROUP.entrySet().parallelStream().forEach(entry -> {
			String key = entry.getKey();
			if(!key.endsWith(MonitorType.LOAD.value()))
				return ;
			
			List<ChannelGroupSupport> supports = entry.getValue();
			if(!CollectionUtils.isEmpty(supports)) {
				Map<String, String> map = MonitorHandler.get(key);
				String sid = map.get("sid");
				if(StringUtils.isNotBlank(sid)) {
					Map<String, Object> response = responses.get(sid);
					if(!CollectionUtils.isEmpty(response))
						supports.get(0).getGroup().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(response)));
				}
			}
		});
	}

	@Override
	public void after() {

	}

	@Override
	public void destroy() {

	}

}
