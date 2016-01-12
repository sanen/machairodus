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
package org.machairodus.balancer.component;

import org.machairodus.balancer.component.impl.CommandComponentImpl;
import org.machairodus.mappers.domain.NodeConfig;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(CommandComponentImpl.class)
@RequestMapping("/cmd")
public interface CommandComponent {
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	Object create(@RequestParam(name = "nodeConfig") NodeConfig node, @RequestParam(name = "token") String token);
	
	@RequestMapping("/destroy/{id}")
	Object destroy(@PathVariable("id") Long id, @RequestParam(name = "token") String token);
	
}
