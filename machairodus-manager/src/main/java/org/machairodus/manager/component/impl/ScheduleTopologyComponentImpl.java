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
package org.machairodus.manager.component.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.machairodus.commons.token.Token;
import org.machairodus.commons.util.HttpClientUtil;
import org.machairodus.commons.util.ResponseStatus;
import org.machairodus.manager.component.ScheduleTopologyComponent;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.NodeType;
import org.machairodus.mappers.domain.ScheduleTopology;
import org.machairodus.mappers.mapper.manager.ConfigureNodeMapper;
import org.machairodus.topology.cmd.Command;
import org.machairodus.topology.cmd.Executor;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.status.ResultMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ScheduleTopologyComponentImpl implements ScheduleTopologyComponent {

	private Logger LOG = LoggerFactory.getLogger(ScheduleTopologyComponentImpl.class);
	public static final String DEFAULT_TOPOLOGY_URI = "/machairodus?";
	private TypeReference<Map<String, String>> mapType = new TypeReference<Map<String, String>>() { };
	
	@Inject
	private ConfigureNodeMapper nodeMapper;
	
	
	
	@Override
	public Object find(NodeConfig[] nodes, String[] group, String[] quartzId, String[] status, Boolean init, final String sort, final String order, Integer offset, Integer limit) {
		if(init != null && init)
			return Collections.emptyList();
		
		if(ArrayUtils.isEmpty(nodes)) {
			List<NodeConfig> _nodes = nodeMapper.findByType(NodeType.SERVICE_NODE.value());
			if(!CollectionUtils.isEmpty(_nodes))
				nodes = _nodes.toArray(new NodeConfig[_nodes.size()]);
		}
		
		if(ArrayUtils.isEmpty(nodes))
			return Collections.emptyList();
		
		List<ScheduleTopology> topologys = Lists.newArrayList();
		for(NodeConfig node : nodes) {
			try {
				Map<String, Object> map = Maps.newHashMap();
				map.put(Executor.COMMAND, Command.QUARTZ.value());
				map.put("key", Token.encode("cmd=quartz:" + String.valueOf(Math.random())));
				String nodeAddress;
				String name;
				Map<String, String> ret = HttpClientUtil.post("http://" + (nodeAddress = node.getServerAddress() + ":" + node.getPort()) + "/" + (name = node.getName()) + DEFAULT_TOPOLOGY_URI, map, mapType);
				if(!CollectionUtils.isEmpty(ret)) {
					String[] starteds = JSON.parseObject(ret.get(Executor.STARTED), String[].class);
					String[] stoppings = JSON.parseObject(ret.get(Executor.STOPPING), String[].class);
					String[] stoppeds = JSON.parseObject(ret.get(Executor.STOPPED), String[].class);
					
					if(ArrayUtils.isEmpty(status)) {
						put(nodeAddress, name, starteds, Executor.STARTED, topologys, group, quartzId);
						put(nodeAddress, name, stoppings, Executor.STOPPING, topologys, group, quartzId);
						put(nodeAddress, name, stoppeds, Executor.STOPPED, topologys, group, quartzId);
						
					} else {
						if(ArrayUtils.contains(status, Executor.STARTED))
							put(nodeAddress, name, starteds, Executor.STARTED, topologys, group, quartzId);
						
						if(ArrayUtils.contains(status, Executor.STOPPING))
							put(nodeAddress, name, stoppings, Executor.STOPPING, topologys, group, quartzId);
						
						if(ArrayUtils.contains(status, Executor.STOPPED));
							put(nodeAddress, name, stoppeds, Executor.STOPPED, topologys, group, quartzId);
							
					}
				}
				
				if(StringUtils.isNotBlank(sort) && StringUtils.isNotBlank(order)) {
					Collections.sort(topologys, (before, after) -> {
						String beforeSort = before._getAttributeValue(sort, "");
						String afterSort = after._getAttributeValue(sort, "");
						
						if("asc".equals(order.toLowerCase())) {
							return beforeSort.compareTo(afterSort);
							
						} else if("desc".equals(order.toLowerCase())) {
							return afterSort.compareTo(beforeSort);
							
						}
						
						return 0;
					});
				}
			} catch(Exception e) {
				LOG.error("获取任务列表异常: " + e.getMessage());
			}
		}
		
		return topologys;
	}
	
	private void put(String nodeAddress, String name, String[] arrays, String status, List<ScheduleTopology> topologys, String[] groups, String[] quartzIds) {
		if(!ArrayUtils.isEmpty(arrays)) {
			for(String item : arrays) {
				String group = item.substring(0, item.lastIndexOf("-"));
				if(!ArrayUtils.isEmpty(groups)) {
					boolean hasGroup = false;
					for(String _group : groups) {
						if(group.contains(_group)) {
							hasGroup = true;
							break;
						}
					}
					
					if(!hasGroup)
						continue ;
				}
				
				if(!ArrayUtils.isEmpty(quartzIds)) {
					boolean hasQuartzId = false;
					for(String quartzId : quartzIds) {
						if(item.contains(quartzId)) {
							hasQuartzId = true;
							break;
						}
					}
					
					if(!hasQuartzId)
						continue ;
				}
				
				ScheduleTopology topology = new ScheduleTopology();
				topology.setId(nodeAddress + "-" + name + "-" + item);
				topology.setNodeAddress(nodeAddress);
				topology.setNodeName(name);
				topology.setGroup(group);
				topology.setQuartzId(item);
				topology.setStatus(status.toUpperCase());
				topologys.add(topology);
			}
		}
	}
	
	@Override
	public Object start(ScheduleTopology st) {
		Map<String, Object> map = Maps.newHashMap();
		map.put(Executor.COMMAND, Command.START.value());
		map.put(Executor.ID, st.getQuartzId());
		map.put(Executor.KEY, Token.encode(JSON.toJSONString(st)));
		Map<String, String> ret = HttpClientUtil.post("http://" + st.getNodeAddress() + "/" + st.getNodeName() + DEFAULT_TOPOLOGY_URI, map, mapType);
		if(ret.get(ResultMap.STATUS) != null &&  Integer.parseInt(ret.get(ResultMap.STATUS)) == 200) {
			Map<String, Object> item = ResponseStatus.OK._getBeanToMap();
			st.setStatus(Executor.STARTED.toUpperCase());
			item.put("item", st);
			return item;
		}
		
		Map<String, Object> fail = ResponseStatus.FAIL._getBeanToMap();
		fail.put("item", st);
		return fail;
	}
	
	@Override
	public Object stop(ScheduleTopology st) {
		Map<String, Object> map = Maps.newHashMap();
		map.put(Executor.COMMAND, Command.STOP.value());
		map.put(Executor.ID, st.getQuartzId());
		map.put(Executor.KEY, Token.encode(JSON.toJSONString(st)));
		Map<String, String> ret = HttpClientUtil.post("http://" + st.getNodeAddress() + "/" + st.getNodeName() + DEFAULT_TOPOLOGY_URI, map, mapType);
		if(ret.get(ResultMap.STATUS) != null &&  Integer.parseInt(ret.get(ResultMap.STATUS)) == 200) {
			Map<String, Object> item = ResponseStatus.OK._getBeanToMap();
			st.setStatus(Executor.STOPPING.toUpperCase());
			item.put("item", st);
			return item;
		}
		
		Map<String, Object> fail = ResponseStatus.FAIL._getBeanToMap();
		fail.put("item", st);
		return fail;
	}
	
	@Override
	public Object remove(ScheduleTopology st) {
		Map<String, Object> map = Maps.newHashMap();
		map.put(Executor.COMMAND, Command.REMOVE.value());
		map.put(Executor.ID, st.getQuartzId());
		map.put(Executor.KEY, Token.encode(JSON.toJSONString(st)));
		Map<String, String> ret = HttpClientUtil.post("http://" + st.getNodeAddress() + "/" + st.getNodeName() + DEFAULT_TOPOLOGY_URI, map, mapType);
		if(ret.get(ResultMap.STATUS) != null &&  Integer.parseInt(ret.get(ResultMap.STATUS)) == 200) {
			Map<String, Object> item = ResponseStatus.OK._getBeanToMap();
			st.setStatus(Executor.STOPPING.toUpperCase());
			item.put("item", st);
			return item;
		}
		
		Map<String, Object> fail = ResponseStatus.FAIL._getBeanToMap();
		fail.put("item", st);
		return fail;
	}
}
