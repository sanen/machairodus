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

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.machairodus.manager.component.UsersComponent;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

public class UsersComponentImpl implements UsersComponent {

	@Override
	public View login(HttpServletRequest request, Model model) {
		String errorClassName = (String) request.getAttribute("shiroLoginFailure");

        if(UnknownAccountException.class.getName().equals(errorClassName)) {
            model.addAttribute("error", "用户不存在");
        } else if(IncorrectCredentialsException.class.getName().equals(errorClassName)) {
        	model.addAttribute("error", "密码错误");
        } else if(errorClassName != null) {
        	model.addAttribute("error", "未知错误：" + errorClassName);
        }
		
		return new ForwardView("/pages/login.jsp", true);
	}

}
