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

import org.machairodus.manager.component.ConfigureServerComponent;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.manager.util.MachairodusConstants;
import org.machairodus.mappers.domain.ServerConfig;
import org.machairodus.mappers.domain.User;
import org.machairodus.mappers.mapper.manager.ConfigureServerMapper;
import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.orm.mybatis.MultiTransactional;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConfigureServerComponentImpl implements ConfigureServerComponent {
	
	private Logger LOG = LoggerFactory.getLogger(ConfigureServerComponentImpl.class);
	
	@Inject
	private ConfigureServerMapper configureServerMapper;
	
	@Inject
	private PermissionService permissionService;
	
	@Override
	public Object find(String name[], String address[], Boolean init, String sort, String order, Integer offset, Integer limit) {
		try {
			if(init != null && init)
				return OK;
			
			List<ServerConfig> serverConfigs = configureServerMapper.find(name, address, sort, order, offset, limit);
			long total = configureServerMapper.findTotal(name, address);
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", serverConfigs);
			map.put("total", total);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureServer异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureServer异常");
			return map;
		}
	}
	
	@Override
	public Object findById(Long id) {
		try {
			if(id == null)
				return OK;
			
			ServerConfig serverConfigs = configureServerMapper.findById(id);
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", Lists.newArrayList(serverConfigs));
			map.put("total", 1);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureServer异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureServer异常");
			return map;
		}
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object add(ServerConfig serverConfig) {
		if(serverConfig.getId() != null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的ServerConfig新增对象");
			return map;
		}
		
		if(!serverConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增对象数据内容无效，[名称，地址，用户名，密码]不能为空");
			return map;
		}
		
		serverConfig.setPasswd(CryptUtil.encrypt(serverConfig.getPasswd()));
		
		Timestamp time = new Timestamp(System.currentTimeMillis());
		serverConfig.setCreateTime(time);
		serverConfig.setModifyTime(time);
		
		try {
			User user = permissionService.findPrincipal();
			if(user != null) {
				serverConfig.setCreateUserId(user.getId());
				serverConfig.setCreateUserName(user.getUsername());
				serverConfig.setModifyUserId(user.getId());
				serverConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			if(configureServerMapper.insert(serverConfig) > 0) {
				serverConfig = configureServerMapper.findById(serverConfig.getId());
				serverConfig.setPasswd(MachairodusConstants.PASSWD_VIEW);
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", serverConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("新增ServerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "新增ServerConfig对象异常");
			return map;
		}
		
		return FAIL;
	}

	@MultiTransactional(envId = "machairodus")
	@Override
	public Object update(ServerConfig serverConfig) {
		if(serverConfig.getId() == null) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "无效的ServerConfig修改对象");
			return map;
		}
		
		if(!serverConfig.validate()) {
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "修改对象数据内容无效，[名称，地址，用户名，密码]不能为空");
			return map;
		}
		
		try {
			if(configureServerMapper.findExistsById(serverConfig.getId()) == 0) {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
			
			if(!MachairodusConstants.PASSWD_VIEW.equals(serverConfig.getPasswd()))
				serverConfig.setPasswd(CryptUtil.encrypt(serverConfig.getPasswd()));
			
			User user = permissionService.findPrincipal();
			if(user != null) {
				serverConfig.setModifyUserId(user.getId());
				serverConfig.setModifyUserName(user.getUsername());
			} else {
				Map<String, Object> map = FAIL._getBeanToMap();
				map.put("message", "无效的登陆用户信息");
				return map;
			}
			
			serverConfig.setModifyTime(new Timestamp(System.currentTimeMillis()));
			if(configureServerMapper.update(serverConfig) > 0) {
				serverConfig = configureServerMapper.findById(serverConfig.getId());
				serverConfig.setPasswd(MachairodusConstants.PASSWD_VIEW);
				Map<String, Object> map = OK._getBeanToMap();
				map.put("item", serverConfig);
				return map;
			}
		} catch(Exception e) {
			LOG.error("更新ServerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "更新ServerConfig对象异常");
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
			
			if(configureServerMapper.delete(id, user.getId()) > 0) {
				return OK;
			} else {
				Map<String, Object> map = OK._getBeanToMap();
				map.put("message", "当前更新的对象已不存在");
				return map;
			}
		} catch(Exception e) {
			LOG.error("删除ServerConfig对象异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("foreign key constrain")) {
				map.put("message", "请确保不存在依赖后再进行删除操作");
			} else
				map.put("message", "删除ServerConfig对象异常");
			
			return map;
		}
	}
	
	@Override
	public Object findSimple(String param, Integer offset, Integer limit) {
		try {
			List<ServerConfig> serverConfigs = configureServerMapper.findSimple(param, offset, limit);
			long total = configureServerMapper.findSimpleTotal(param);
			
			Map<String, Object> map = OK._getBeanToMap();
			map.put("rows", serverConfigs);
			map.put("total", total);
			return map;
		} catch(Exception e) {
			LOG.error("查询ConfigureServer异常: " + e.getMessage());
			Map<String, Object> map = FAIL._getBeanToMap();
			map.put("message", "查询ConfigureServer异常");
			return map;
		}
	}
}
