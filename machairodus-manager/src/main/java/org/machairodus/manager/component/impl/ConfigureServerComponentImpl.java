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

import static org.machairodus.manager.util.ResponseStatus.OK;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.machairodus.manager.component.ConfigureServerComponent;
import org.machairodus.mappers.domain.ServerConfig;

import com.google.common.collect.Lists;

public class ConfigureServerComponentImpl implements ConfigureServerComponent {
	
	@Override
	public Object find(String name, String address, Boolean init, String sort, String order, Integer offset, Integer limit) {
		if(init != null && init) return OK;
		Map<String, Object> map = OK._getBeanToMap();
		map.put("total", 100);
		List<ServerConfig> serverConfigList = Lists.newArrayList();
		Random random = new Random();
		for(long idx = 1; idx <= limit; idx ++) {
			Timestamp now = new Timestamp(System.currentTimeMillis() + random.nextInt(1000000));
			ServerConfig config = new ServerConfig();
			config.setId(idx);
			config.setName("Server" + idx);
			config.setAddress("localhost");
			config.setUsername("root");
			config.setPasswd("******");
			config.setCreateTime(now);
			config.setCreateUserId(1L);
			config.setCreateUserName("admin");
			config.setModifyTime(now);
			config.setModifyUserId(1L);
			config.setModifyUserName("admin");
			config.setDeleted(0);
			serverConfigList.add(config);
		}
		
		if(StringUtils.isNotBlank(sort)) {
			Collections.sort(serverConfigList, (before, after) -> {
				if("desc".equals(order)) {
					return ObjectUtils.compare(after._getAttributeValue(sort), before._getAttributeValue(sort));
				} else 
					return ObjectUtils.compare(before._getAttributeValue(sort), after._getAttributeValue(sort));
			});
		}
		
 		map.put("rows", serverConfigList);
		return map;
	}

	
}
