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

import org.machairodus.manager.component.PagesComponent;
import org.machairodus.manager.service.LayoutService;
import org.machairodus.mappers.domain.Func;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

import com.google.inject.Inject;

public class PagesComponentImpl implements PagesComponent {

	@Inject
	private LayoutService layoutSerivce;

	@Override
	public View index(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "index");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View permissionUser(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "permission.user");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View permissionRole(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "permission.role");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View permissionFunc(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "permission.func");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View configureServer(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "configure.server");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View configureNode(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "configure.node");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View configureService(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "configure.service");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View scheduleBalancer(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "schedule.balancer");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View scheduleScheduler(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "schedule.scheduler");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View monitorLoad(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "monitor.load");
		return new ForwardView("/index.jsp");
	}
	
	@Override
	public View statisticsScheduler(HttpServletRequest request, HttpServletResponse response, Model model) {
		build(request, model);
		model.addAttribute("definition", "statistics.scheduler");
		return new ForwardView("/index.jsp");
	}
	
	private void build(HttpServletRequest request, Model model) {
		Map<Long, Func> funcMap = layoutSerivce.findSelectFunc(request.getRequestURI());
		layoutSerivce.buildSidebar(funcMap.keySet(), model);
		layoutSerivce.buildNavigate(funcMap.values(), model);
		layoutSerivce.buildTitle(funcMap.values(), request.getRequestURI(), model);
	}
}
