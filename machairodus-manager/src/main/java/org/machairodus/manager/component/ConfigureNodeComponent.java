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
package org.machairodus.manager.component;

import org.machairodus.manager.component.impl.ConfigureNodeComponentImpl;
import org.machairodus.mappers.domain.NodeConfig;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestParam;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(ConfigureNodeComponentImpl.class)
@RequestMapping("/configure/node")
public interface ConfigureNodeComponent {
	@RequestMapping("/find")
	Object find(@RequestParam(name = "server[]", required = false) Long[] server,
			@RequestParam(name = "node[]", required = false) String[] node,
			@RequestParam(name = "type[]", required = false) Integer[] type, 
			@RequestParam(name = "init", required = false) Boolean init, 
			@RequestParam(name = "sort", required = false) String sort, 
			@RequestParam(name = "order", required = false) String order, 
			@RequestParam(name = "offset") Integer offset, 
			@RequestParam(name = "limit") Integer limit);
	
	@RequestMapping("/find/id")
	Object findById(@RequestParam(name = "id", required = false) Long id);
	
	@RequestMapping("/add")
	Object add(@RequestParam(name = "nodeConfig") NodeConfig nodeConfig);
	
	@RequestMapping("/update")
	Object update(@RequestParam(name = "nodeConfig") NodeConfig nodeConfig);
	
	@RequestMapping("/delete")
	Object delete(@RequestParam(name = "id") Long id);
	
	@RequestMapping("/find/simple")
	Object findSimple(@RequestParam(name = "param", required = false) String param, @RequestParam(name = "type[]", required = false) Integer[] type, @RequestParam(name = "offset") Integer offset, 
			@RequestParam(name = "limit") Integer limit);
	
	@RequestMapping("/monitor/start/{id}")
	Object startMonitor(@PathVariable("id") Long id);
	
	@RequestMapping("/monitor/stop/{id}")
	Object stopMonitor(@PathVariable("id") Long id);
	
}
