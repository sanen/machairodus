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

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.machairodus.commons.util.ResponseStatus;
import org.machairodus.manager.component.UsersComponent;
import org.machairodus.topology.util.ResultMap;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

import com.google.common.collect.Maps;

public class UsersComponentImpl implements UsersComponent {
	private Logger LOG = LoggerFactory.getLogger(UsersComponentImpl.class);
	
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

	@Override
	public Object remoteLogin(String username, String passwd, Boolean rememberMe, String host) {
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, passwd, rememberMe, host);
		try {
			subject.login(token);
			Map<String, Object> result = Maps.newHashMap();
			result.put("sid", subject.getSession().getId());
			return result;
		} catch(AuthenticationException e) {
			LOG.error("权限认证失败: {}", new Object[] { e.getMessage() });
			Map<String, Object> result = ResponseStatus.FAIL_MAP;
			result.put(ResultMap.MESSAGE, "权限认证失败");
			return result;
		}
	}
}
