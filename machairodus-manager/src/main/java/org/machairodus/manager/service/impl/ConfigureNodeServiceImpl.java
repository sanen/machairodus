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
package org.machairodus.manager.service.impl;

import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.machairodus.commons.token.Token;
import org.machairodus.commons.util.HttpClientUtil;
import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.RedisKeys;
import org.machairodus.commons.util.ResponseStatus;
import org.machairodus.manager.service.ConfigureNodeService;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.NodeType;
import org.machairodus.mappers.mapper.manager.ConfigureNodeMapper;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.MD5Utils;
import org.nanoframework.core.status.ResultMap;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ConfigureNodeServiceImpl implements ConfigureNodeService {

	private Logger LOG = LoggerFactory.getLogger(ConfigureNodeServiceImpl.class);
			
	@Inject
	private ConfigureNodeMapper nodeMapper;
	
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	
	@Override
	public boolean startMonitor(NodeConfig node) {
		if(node.getType().intValue() == NodeType.BALANCER.value()) {
			return startMonitor0(node, node.getServerAddress() + ":" + node.getPort());
			
		} else {
			List<NodeConfig> nodes = nodeMapper.findAllBalancer();
			List<Long> balancers = redisClient.hget(RedisKeys.JMX_MONITOR.value(), NodeType.BALANCER.name(), new TypeReference<List<Long>>() { });
			if(!CollectionUtils.isEmpty(balancers)) {
				for(long id : balancers) {
					for(NodeConfig _node : nodes) {
						if(_node.getId().longValue() == id) {
							Socket socket;
							try {
								socket = new Socket(_node.getServerAddress(), _node.getPort());
								if(socket.isConnected()) 
									socket.close();
								
								return startMonitor0(node, _node.getServerAddress() + ":" + _node.getPort());
							} catch(Exception e) {
								break;
								
							} finally {
								socket = null;
							}
						}
					}
				}
				
				// Redis中的Balancer全部无法连接时
				for(NodeConfig _node : nodes) {
					Socket socket;
					try {
						socket = new Socket(_node.getServerAddress(), _node.getPort());
						if(socket.isConnected()) 
							socket.close();
						
						return startMonitor0(node, _node.getServerAddress() + ":" + _node.getPort());
					} catch(Exception e) {
						continue ;
					} finally {
						socket = null;
					}
				}
				
			} else {
				for(NodeConfig _node : nodes) {
					Socket socket;
					try {
						socket = new Socket(_node.getServerAddress(), _node.getPort());
						if(socket.isConnected()) 
							socket.close();
						
						return startMonitor0(node, _node.getServerAddress() + ":" + _node.getPort());
					} catch(Exception e) {
						continue ;
					} finally {
						socket = null;
					}
				}
			}
		}
		
		return false;
	}
	
	private synchronized boolean startMonitor0(NodeConfig node, String address) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("nodeConfig", node);
		params.put("token", Token.encode(MD5Utils.getMD5String(node.toString())));
		try {
			Map<String, Object> map = HttpClientUtil.post("http://" + address + "/balancer/cmd/create?", params, new TypeReference<Map<String, Object>>() { });
			if(map.get(ResultMap.STATUS).equals(ResponseStatus.OK.getStatus())) {
				redisClient.hset(RedisKeys.JMX_MONITOR_NODE.value(), String.valueOf(node.getId()), address);
				return true;
			}
		} catch(Exception e) {
			LOG.error("远程服务调用异常: " + e.getMessage());
		}
		
		return false;
	}

	@Override
	public boolean stopMonitor(NodeConfig node) {
		return stopMonitor0(String.valueOf(node.getId()));
	}

	private synchronized boolean stopMonitor0(String id) {
		String address = redisClient.hget(RedisKeys.JMX_MONITOR_NODE.value(), id);
		if(StringUtils.isBlank(address))
			return false;
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("token", Token.encode(MD5Utils.getMD5String(id)));
		try {
			Map<String, Object> map = HttpClientUtil.post("http://" + address + "/balancer/cmd/destroy/" + id, params, new TypeReference<Map<String, Object>>() { });
			if(map.get(ResultMap.STATUS).equals(ResponseStatus.OK.getStatus())) {
				redisClient.hdel(RedisKeys.JMX_MONITOR_NODE.value(), id);
				return true;
			}
		} catch(Exception e) {
			LOG.error("远程服务调用异常: " + e.getMessage());
		}
		
		return false;
	}
}
