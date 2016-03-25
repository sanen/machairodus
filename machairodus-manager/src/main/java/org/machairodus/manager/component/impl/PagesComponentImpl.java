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
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.machairodus.manager.component.PagesComponent;
import org.machairodus.manager.service.LayoutService;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.manager.util.Definition;
import org.machairodus.manager.websocket.MonitorHandler;
import org.machairodus.mappers.domain.Func;
import org.machairodus.mappers.domain.User;
import org.nanoframework.extension.websocket.WebSocketFactory;
import org.nanoframework.extension.websocket.WebSocketServer;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

import com.google.inject.Inject;

public class PagesComponentImpl implements PagesComponent {

	@Inject
	private LayoutService layoutSerivce;
	
	@Inject
	private PermissionService permissionService;
	
	private final View INDEX = new ForwardView("/index.jsp");
	public static final String DEFINITION = "definition";

	@Override
	public View index(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.INDEX);
		return INDEX;
	}
	
	@Override
	public View permissionUser(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.PERMISSION_USER);
		return INDEX;
	}
	
	@Override
	public View permissionRole(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.PERMISSION_ROLE);
		return INDEX;
	}
	
	@Override
	public View permissionFunc(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.PERMISSION_FUNC);
		return INDEX;
	}
	
	@Override
	public View configureServer(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.CONFIGURE_SERVER);
		return INDEX;
	}
	
	@Override
	public View configureNode(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.CONFIGURE_NODE);
		return INDEX;
	}
	
	@Override
	public View configureService(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.CONFIGURE_SERVICE);
		return INDEX;
	}
	
	@Override
	public View scheduleBalancer(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.SCHEDULE_BALANCER);
		return INDEX;
	}
	
	@Override
	public View scheduleScheduler(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.SCHEDULE_SCHEDULER);
		return INDEX;
	}
	
	@Override
	public View scheduleTopology(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.SCHEDULE_TOPOLOGY);
		return INDEX;
	}
	
	@Override
	public View monitorLoad(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.MONITOR_LOAD);
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View monitorTps(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.MONITOR_TPS);
		return INDEX;
	}
	
	@Override
	public View statisticsScheduler(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute(DEFINITION, Definition.STATISTICS_SCHEDULER);
		return INDEX;
	}
	
	@Override
	public View websocket(String id, HttpServletRequest request, HttpServletResponse response, Model model) {
		WebSocketServer server = WebSocketFactory.get(MonitorHandler.class.getSimpleName());
		String schema = server.isSsl() ? "wss://" : "ws://";
		String host = request.getServerName();
		int port = server.getPort();
		String context = server.getLocation();
		
		User user = permissionService.findPrincipal();
		model.addAttribute("uid", user.getId());
		model.addAttribute("sid", SecurityUtils.getSubject().getSession().getId());
		model.addAttribute("url", schema + host + ":" + port + context);
		
		return new ForwardView("/pages/js/" + id + ".websocket.jsp", true);
	}
	
	private void build(HttpServletRequest request, Model model) {
		Map<Long, Func> funcMap = layoutSerivce.findSelectFunc(request.getRequestURI());
		layoutSerivce.buildSidebar(funcMap.keySet(), model);
		layoutSerivce.buildNavigate(funcMap.values(), model);
		layoutSerivce.buildTitle(funcMap.values(), request.getRequestURI(), model);
	}
}
