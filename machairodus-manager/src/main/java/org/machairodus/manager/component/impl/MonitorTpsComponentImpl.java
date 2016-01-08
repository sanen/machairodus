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

import java.util.Map;

import org.apache.shiro.util.CollectionUtils;
import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.ResponseStatus;
import org.machairodus.manager.component.MonitorTpsComponent;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.mappers.domain.User;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.web.server.mvc.Model;

import com.alibaba.fastjson.TypeReference;
import com.google.inject.Inject;

public class MonitorTpsComponentImpl implements MonitorTpsComponent {
	private Logger LOG = LoggerFactory.getLogger(MonitorTpsComponentImpl.class);
	
	@Inject
	private PermissionService permissionService;
	
	private RedisClient MANAGER = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	
	@Override
	public Object init(String monitorType, Model model) {
		try {
			User user = permissionService.findPrincipal();
			Map<String, Map<String, String>> maps = MANAGER.hgetAll(user.getId() + "_" + monitorType, new TypeReference<Map<String, String>>() { });
			Map<String, Object> result = ResponseStatus.OK._getBeanToMap();
			if(!CollectionUtils.isEmpty(maps))
				result.put("items", maps.values());
			
			return result;
		} catch(Exception e) {
			LOG.error("读取监控列表异常: " + e.getMessage());
			Map<String, Object> result =  ResponseStatus.FAIL._getBeanToMap();
			result.put("message", "读取监控列表异常");
			return result;
		}
	}
}
