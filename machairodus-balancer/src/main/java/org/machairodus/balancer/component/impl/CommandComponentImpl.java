/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.machairodus.balancer.component.impl;

import org.machairodus.balancer.component.CommandComponent;
import org.machairodus.balancer.service.CommandService;
import org.machairodus.commons.token.Token;
import org.machairodus.commons.util.ResponseStatus;
import org.machairodus.mappers.domain.NodeConfig;
import org.nanoframework.core.status.ResultMap;

import com.google.inject.Inject;

public class CommandComponentImpl implements CommandComponent {

	@Inject
	private CommandService commandService;
	
	@Override
	public Object create(NodeConfig node, String token) {
		if(node.getId() == null) 
			return ResultMap.create(ResponseStatus.FAIL.getStatus(), "无效的节点配置对象: ID不能为空", ResponseStatus.FAIL.getInfo());
		
		if(!Token.decode(token)) 
			return ResultMap.create(ResponseStatus.FAIL.getStatus(), "无效的Token认证", ResponseStatus.FAIL.getInfo());
		
		
		return commandService.createScheduler(node);
	}

	@Override
	public Object destroy(Long id, String token) {
		if(!Token.decode(token)) 
			return ResultMap.create(ResponseStatus.FAIL.getStatus(), "无效的Token认证", ResponseStatus.FAIL.getInfo());
		
		return commandService.destroyScheduler(id);
	}

}
