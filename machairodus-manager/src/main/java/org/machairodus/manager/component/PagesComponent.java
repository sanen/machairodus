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
import javax.servlet.http.HttpServletResponse;

import org.machairodus.manager.component.impl.PagesComponentImpl;
import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;

import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(PagesComponentImpl.class)
public interface PagesComponent {
	@RequestMapping("/index")
	View index(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/permission/user")
	View permissionUser(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/permission/role")
	View permissionRole(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/permission/func")
	View permissionFunc(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/configure/server")
	View configureServer(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/configure/node")
	View configureNode(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/configure/service")
	View configureService(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/schedule/balancer")
	View scheduleBalancer(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/schedule/scheduler")
	View scheduleScheduler(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/monitor/load")
	View monitorLoad(HttpServletRequest request, HttpServletResponse response, Model model);
	
	@RequestMapping("/statistics/scheduler")
	View statisticsScheduler(HttpServletRequest request, HttpServletResponse response, Model model);
}
