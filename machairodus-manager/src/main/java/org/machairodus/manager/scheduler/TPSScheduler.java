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

import java.io.IOException;
import java.rmi.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.machairodus.manager.service.MonitorType;
import org.machairodus.manager.service.StatisticMXBean;
import org.machairodus.manager.service.impl.StatisticImpl;
import org.machairodus.manager.websocket.MonitorHandler;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.Scheduler;
import org.nanoframework.extension.websocket.ChannelGroupSupport;
import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.JmxClientManager;

import com.alibaba.fastjson.JSON;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Scheduler(group = TPSScheduler.class, beforeAfterOnly = true, cron = "* * * * * ?", parallel = 1)
public class TPSScheduler extends BaseScheduler {
	private Map<String, Map<String, Object>> serverMap = new HashMap<>();
	
	@Override
	public void before() {

	}

	@Override
	public void execute() {
		Map<String, String> servers = new HashMap<>();
		ChannelGroupSupport.GROUP.entrySet().parallelStream().forEach(entry -> {
			String key = entry.getKey();
			if(!key.endsWith(MonitorType.TPS.value()))
				return ;
			
			List<ChannelGroupSupport> supports = entry.getValue();
			if(!CollectionUtils.isEmpty(supports)) {
				Map<String, String> map = MonitorHandler.get(key);
				String server = map.get("server");
				String id = map.get("id");
				if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(server))
					servers.put(id, server);
			}
		});
		
		ConcurrentMap<String, Map<String, Object>> responses = new ConcurrentHashMap<>();
		servers.entrySet().parallelStream().forEach(entry -> {
			String id = entry.getKey();
			String server = entry.getValue();
			Map<String, Object> mxbeanMap;
			if((mxbeanMap = serverMap.get(server)) == null) {
				mxbeanMap = new HashMap<>();
				serverMap.put(server, mxbeanMap);
			}
			
			Map<String, Object> response = new HashMap<>();
			JmxClient client = null;
			try {
				client = JmxClientManager.get(server);
				StatisticMXBean statisticMXBean;
				if((statisticMXBean = (StatisticMXBean) mxbeanMap.get("StatisticMXBean")) == null) {
					statisticMXBean = new StatisticImpl(client);
					mxbeanMap.put("StatisticMXBean", statisticMXBean);
				}
				
				response.put("ID", "view_" + id);
				response.put("TIME", System.currentTimeMillis());
				response.put("TPS", statisticMXBean.getPointer());
			} catch(Exception e) {
				if(e.getCause() != null && e.getCause() instanceof IOException) {
					if(StringUtils.isNotBlank(server) && client != null) {
						try { client.reconnect(); } catch(ConnectException ex) { LOG.error("断线重连异常: " + e.getMessage()); }
						mxbeanMap.clear();
					}
				} else
					LOG.error("获取信息异常：" + e.getMessage());
				
				response.put("ID", "view_" + id);
				response.put("TIME", System.currentTimeMillis());
				response.put("TPS", Collections.emptyList());
			}
			
			responses.put(server, response);
		});
		
		ChannelGroupSupport.GROUP.entrySet().parallelStream().forEach(entry -> {
			String key = entry.getKey();
			if(!key.endsWith(MonitorType.TPS.value()))
				return ;
			
			List<ChannelGroupSupport> supports = entry.getValue();
			if(!CollectionUtils.isEmpty(supports)) {
				Map<String, String> map = MonitorHandler.get(key);
				String server = map.get("server");
				if(StringUtils.isNotBlank(server)) {
					Map<String, Object> response = responses.get(server);
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
