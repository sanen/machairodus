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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.machairodus.manager.component.ConfigureServiceComponent;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.mappers.domain.NodeConfig;
import org.machairodus.mappers.domain.SchedulerConfig;
import org.machairodus.mappers.domain.User;
import org.machairodus.mappers.mapper.manager.ConfigureNodeMapper;
import org.machairodus.mappers.mapper.manager.ConfigureServiceMapper;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.mybatis.MultiTransactional;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConfigureServiceComponentImpl implements ConfigureServiceComponent {
private Logger LOG = LoggerFactory.getLogger(ConfigureServerComponentImpl.class);
	
	@Inject
	private ConfigureServiceMapper configureServiceMapper;
	
	@Inject
	private ConfigureNodeMapper configureNodeMapper;
	
	@Inject
	private PermissionService permissionService;
	
	@Override
	public Object find(String name[], String uri[], Boolean init, String sort, String order, Integer offset, Integer limit) {
		try {
			if(init != null && init)
				return OK;
			
			List<SchedulerConfig> schedulerConfigs = configureServiceMapper.find(name, uri, sort, order, offset, limit);
			long total = configureServiceMapper.findTotal(name, uri);
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", schedulerConfigs);
			map.put("total", total);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureService异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureService异常");
			return map;
		}
	}
	
	@Override
	public Object findById(Long id) {
		try {
			if(id == null)
				return OK;
			
			SchedulerConfig schedulerConfigs = configureServiceMapper.findById(id);
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", Lists.newArrayList(schedulerConfigs));
			map.put("total", 1);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureService异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureService异常");
			return map;
		}
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object add(SchedulerConfig schedulerConfig) {
		if(schedulerConfig.getId() != null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的ServerConfig新增对象");
			return map;
		}
		
		if(!schedulerConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增对象数据内容无效，[名称，地址，用户名，密码]不能为空");
			return map;
		}
		
		Timestamp time = new Timestamp(System.currentTimeMillis());
		schedulerConfig.setCreateTime(time);
		schedulerConfig.setModifyTime(time);
		
		try {
			User user = permissionService.findPrincipal();
			if(user != null) {
				schedulerConfig.setCreateUserId(user.getId());
				schedulerConfig.setCreateUserName(user.getUsername());
				schedulerConfig.setModifyUserId(user.getId());
				schedulerConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			if(configureServiceMapper.insert(schedulerConfig) > 0) {
				schedulerConfig = configureServiceMapper.findById(schedulerConfig.getId());
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", schedulerConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("新增SchedulerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增SchedulerConfig对象异常");
			return map;
		}
		
		return FAIL;
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object update(SchedulerConfig schedulerConfig) {
		if(schedulerConfig.getId() == null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的SchedulerConfig修改对象");
			return map;
		}
		
		if(!schedulerConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "修改对象数据内容无效，[名称，服务URI，类型]不能为空");
			return map;
		}
		
		try {
			if(configureServiceMapper.findExistsById(schedulerConfig.getId()) == 0) {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
			
			User user = permissionService.findPrincipal();
			if(user != null) {
				schedulerConfig.setModifyUserId(user.getId());
				schedulerConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			schedulerConfig.setModifyTime(new Timestamp(System.currentTimeMillis()));
			if(configureServiceMapper.update(schedulerConfig) > 0) {
				schedulerConfig = configureServiceMapper.findById(schedulerConfig.getId());
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", schedulerConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("更新SchedulerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "更新SchedulerConfig对象异常");
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
			
			if(configureServiceMapper.delete(id, user.getId()) > 0) {
				return OK;
			} else {
				Map<String, Object> map = OK._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
		} catch(Exception e) {
			LOG.error("删除SchedulerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("foreign key constrain")) {
				map.put("message", "请确保不存在依赖后再进行删除操作");
			} else
				map.put("message", "删除SchedulerConfig对象异常");
			
			return map;
		}
	}
	
	@Override
	public View assign(Long schedulerId, Model model) {
		List<NodeConfig> unselect = configureNodeMapper.findUnAssign(schedulerId);
		Collections.sort(unselect, (before, after) -> before.getName().compareTo(after.getName()));
		model.addAttribute("unselect", unselect);
		
		List<NodeConfig> select = configureNodeMapper.findAssign(schedulerId);
		Collections.sort(select, (before, after) -> before.getName().compareTo(after.getName()));
		model.addAttribute("select", select);
		
		return new ForwardView("/pages/dialog/configure.service.assign.jsp", true);
	}
	
	@Override
	public Object assigning(Long schedulerId, Long nodeId, String type) {
		try {
			User user = permissionService.findPrincipal();
			if(user == null) {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			if("select".equals(type)) {
				configureServiceMapper.insertSchedulerInfo(schedulerId, nodeId, user.getId());
			} else if("deselect".equals(type)) {
				configureServiceMapper.deleteSchedulerInfo(schedulerId, nodeId);
			}
			
			return OK;
		} catch(Exception e) {
			LOG.error("删除SchedulerInfo对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "删除SchedulerInfo对象异常");
			return map;
		}
	}
}
