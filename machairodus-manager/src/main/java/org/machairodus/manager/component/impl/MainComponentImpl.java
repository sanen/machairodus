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
import javax.servlet.http.HttpServletResponse;

import org.machairodus.manager.component.MainComponent;
import org.machairodus.manager.service.SideBarService;
import org.nanoframework.web.server.mvc.Model;
import org.nanoframework.web.server.mvc.View;
import org.nanoframework.web.server.mvc.support.ForwardView;

import com.google.inject.Inject;

public class MainComponentImpl implements MainComponent {

	@Inject
	private SideBarService sidebarSerivce;

	@Override
	public View main(Long id, HttpServletRequest request, HttpServletResponse response, Model model) {
		String sidebarContent = sidebarSerivce.build(id);
		if(sidebarContent != null) {
			model.addAttribute("sidebar", sidebarContent);
		}
		
		return new ForwardView("/index.jsp");
	}
	
}
