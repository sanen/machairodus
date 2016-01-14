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
package org.machairodus.manager.component.impl;

import static org.machairodus.commons.util.ResponseStatus.FAIL;
import static org.machairodus.commons.util.ResponseStatus.OK;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.RedisKeys;
import org.machairodus.manager.component.ConfigureNodeComponent;
import org.machairodus.manager.service.ConfigureNodeService;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.JmxMonitorStatus;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.NodeType;
import org.machairodus.mappers.domain.ServerConfig;
import org.machairodus.mappers.domain.User;
import org.machairodus.mappers.mapper.manager.ConfigureNodeMapper;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ObjectCompare;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.mybatis.MultiTransactional;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConfigureNodeComponentImpl implements ConfigureNodeComponent {

	private Logger LOG = LoggerFactory.getLogger(ConfigureNodeComponentImpl.class);
	
	@Inject
	private ConfigureNodeMapper configureNodeMapper;
	
	@Inject
	private PermissionService permissionService;
	
	@Inject
	private ConfigureNodeService nodeService;
	
	private RedisClient redisClient = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	private TypeReference<JmxMonitor> typeReference = new TypeReference<JmxMonitor>() { };
	
	@Override
	public Object find(Long[] server, String[] node, Integer[] type, Boolean init, String sort, String order, Integer offset, Integer limit) {
		try {
			if(init != null && init)
				return OK;
			
			List<NodeConfig> nodeConfigs = configureNodeMapper.find(server, node, type, sort, order, offset, limit);
			long total = configureNodeMapper.findTotal(server, node, type);
			List<Map<String, Object>> nodes = Lists.newArrayList();
			if(!CollectionUtils.isEmpty(nodeConfigs)) {
				nodeConfigs.forEach(config -> {
					String _type = NodeType.value(config.getType()).name();
					String address = redisClient.hget(RedisKeys.JMX_MONITOR_NODE.value(), String.valueOf(config.getId()));
					
					Map<String, Object> _node = config._getBeanToMap();
					if(StringUtils.isNotBlank(address)) {
						_node.put("monitored", true);
						
						JmxMonitor monitor = redisClient.hget(_type, String.valueOf(config.getId()), typeReference);
						if(monitor != null) {
							_node.put("pid", monitor.getPid());
							_node.put("status", monitor.getStatus());
							if(ObjectCompare.isInList(monitor.getStatus(), JmxMonitorStatus.DOWN, JmxMonitorStatus.TIMEOUT, JmxMonitorStatus.MONITOR_DOWN))
								_node.put("monitored", false);
							
						} else {
							_node.put("status", JmxMonitorStatus.DOWN);
						}
					} else {
						_node.put("status", JmxMonitorStatus.CLOSED);
						_node.put("monitored", false);
					}
					
					nodes.add(_node);
				});
			}
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", nodes);
			map.put("total", total);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureNode异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureNode异常");
			return map;
		}
	}
	
	@Override
	public Object findById(Long id) {
		try {
			if(id == null)
				return OK;
			
			NodeConfig nodeConfigs = configureNodeMapper.findById(id);
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", Lists.newArrayList(nodeConfigs));
			map.put("total", 1);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureNode异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureNode异常");
			return map;
		}
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object add(NodeConfig nodeConfig) {
		if(nodeConfig.getId() != null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的ConfigureNode新增对象");
			return map;
		}
		
		if(!nodeConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增对象数据内容无效，[服务器，名称，类型，端口]不能为空");
			return map;
		}
		
		Timestamp time = new Timestamp(System.currentTimeMillis());
		nodeConfig.setCreateTime(time);
		nodeConfig.setModifyTime(time);
		
		try {
			User user = permissionService.findPrincipal();
			if(user != null) {
				nodeConfig.setCreateUserId(user.getId());
				nodeConfig.setCreateUserName(user.getUsername());
				nodeConfig.setModifyUserId(user.getId());
				nodeConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			if(configureNodeMapper.insert(nodeConfig) > 0) {
				nodeConfig = configureNodeMapper.findById(nodeConfig.getId());
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", nodeConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("新增ConfigureNode对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增ConfigureNode对象异常");
			return map;
		}
		
		return FAIL;
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object update(NodeConfig nodeConfig) {
		if(nodeConfig.getId() == null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的ConfigureNode修改对象");
			return map;
		}
		
		if(!nodeConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "修改对象数据内容无效，[服务器，名称，类型，端口]不能为空");
			return map;
		}
		
		try {
			if(configureNodeMapper.findExistsById(nodeConfig.getId()) == 0) {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
			
			User user = permissionService.findPrincipal();
			if(user != null) {
				nodeConfig.setModifyUserId(user.getId());
				nodeConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			nodeConfig.setModifyTime(new Timestamp(System.currentTimeMillis()));
			if(configureNodeMapper.update(nodeConfig) > 0) {
				nodeConfig = configureNodeMapper.findById(nodeConfig.getId());
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", nodeConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("更新ConfigureNode对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "更新ConfigureNode对象异常");
			return map;
		}
		
		return FAIL;
	}

	@Override
	public Object delete(Long id) {
		try {
			User user = permissionService.findPrincipal();
			if(user == null) {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			if(configureNodeMapper.delete(id, user.getId()) > 0) {
				return OK;
			} else {
				Map<String, Object> map = OK._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
		} catch(Exception e) {
			LOG.error("删除NodeConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("foreign key constrain")) {
				map.put("message", "请确保不存在依赖后再进行删除操作");
			} else
				map.put("message", "删除ConfigureNode对象异常");
			
			return map;
		}
	}

	@Override
	public Object findSimple(String param, Integer[] type, Integer offset, Integer limit) {
		try {
			List<NodeConfig> serverConfigs = configureNodeMapper.findSimple(param, type, offset, limit);
			long total = configureNodeMapper.findSimpleTotal(param, type);
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", serverConfigs);
			map.put("total", total);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureNode异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureNode异常");
			return map;
		}
	}
	
	@Override
	public Object startMonitor(Long id) {
		try {
			NodeConfig node = configureNodeMapper.findById(id);
			if(node == null)
				return FAIL;
					
			if(nodeService.startMonitor(node)) {
				Map<String, Object> ok = OK._getBeanToMap();
				ok.put("jmxStatus", JmxMonitorStatus.PENDING);
				ok.put("monitored", true);
				return ok;
			} else 
				return FAIL;
		} catch(Exception e) {
			LOG.error("启动监控异常: " + e.getMessage());
			return FAIL;
		}
		
	}
	
	@Override
	public Object stopMonitor(Long id) {
		try {
			NodeConfig node = configureNodeMapper.findById(id);
			if(node == null)
				return FAIL;
			
			if(nodeService.stopMonitor(node)) {
				Map<String, Object> ok = OK._getBeanToMap();
				ok.put("jmxStatus", JmxMonitorStatus.PENDING);
				ok.put("monitored", false);
				return ok;
			} else 
				return FAIL;
		} catch(Exception e) {
			LOG.error("启动监控异常: " + e.getMessage());
			return FAIL;
		}
	}
}
