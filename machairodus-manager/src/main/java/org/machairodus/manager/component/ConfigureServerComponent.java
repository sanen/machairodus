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

import org.machairodus.manager.component.impl.ConfigureServerComponentImpl;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestParam;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(ConfigureServerComponentImpl.class)
@RequestMapping("/configure/server")
public interface ConfigureServerComponent {
	@RequestMapping("/find")
	Object find(@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "address", required = false) String address, 
			@RequestParam(name = "init", required = false) Boolean init, 
			@RequestParam(name = "sort", required = false) String sort, 
			@RequestParam(name = "order", required = false) String order, 
			@RequestParam(name = "offset") Integer offset, 
			@RequestParam(name = "limit") Integer limit);
}
