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

import static org.machairodus.manager.util.ResponseStatus.FAIL;
import static org.machairodus.manager.util.ResponseStatus.OK;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.machairodus.manager.component.ConfigureNodeComponent;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.User;
import org.machairodus.mappers.mapper.manager.ConfigureNodeMapper;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.mybatis.MultiTransactional;

import com.google.inject.Inject;

public class ConfigureNodeComponentImpl implements ConfigureNodeComponent {

	private Logger LOG = LoggerFactory.getLogger(ConfigureNodeComponentImpl.class);
	
	@Inject
	private ConfigureNodeMapper configureNodeMapper;
	
	@Inject
	private PermissionService permissionService;
	
	@Override
	public Object find(Long[] server, String[] node, Integer[] type, Boolean init, String sort, String order, Integer offset, Integer limit) {
		try {
			if(init != null && init)
				return OK;
			
			List<NodeConfig> nodeConfigs = configureNodeMapper.find(server, node, type, sort, order, offset, limit);
			long total = configureNodeMapper.findTotal(server, node, type);
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", nodeConfigs);
			map.put("total", total);
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
		nodeConfig.setDeleted(0);
		
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
			map.put("message", "删除ConfigureNode对象异常");
			return map;
		}
	}

}
