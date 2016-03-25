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

import javax.servlet.http.HttpServletRequest;

import org.machairodus.manager.component.impl.UsersComponentImpl;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
import org.nanoframework.core.component.stereotype.bind.RequestParam;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(UsersComponentImpl.class)
@RequestMapping("/permissions/users")
public interface UsersComponent {
	@RequestMapping(value = "/login")
	View login(HttpServletRequest request, Model model);

	@RequestMapping(value = "/login/remote", method = { RequestMethod.POST })
	Object remoteLogin(
			@RequestParam(name = "username") String username, 
			@RequestParam(name = "password") String passwd,
			@RequestParam(name = "rememberMe", required = false, defaultValue = "false") Boolean rememberMe,
			@RequestParam(name = "host", required = false) String host);
}
