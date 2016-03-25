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
package org.machairodus.manager.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.machairodus.manager.auth.MachairodusAuthorizationInfo;
import org.machairodus.manager.service.LayoutService;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.mappers.domain.Func;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.Constants;
import org.nanoframework.web.server.mvc.Model;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class LayoutServiceImpl implements LayoutService {

	@Inject
	private PermissionService permissionService;
	
	private final String context = System.getProperty(Constants.CONTEXT_ROOT);
	
	@Override
	public void buildSidebar(Set<Long> itemIds, Model model) {
		MachairodusAuthorizationInfo info = (MachairodusAuthorizationInfo) permissionService.findPermissions();
		List<Func> items;
		if(info == null || CollectionUtils.isEmpty(items = info.getFuncs())) 
			return ;
		
		StringBuilder builder = new StringBuilder();
		int idx = 1;
		for(Func item : items) {
			if(itemIds.contains(item.getId()))
				builder.append("<li id='"+item.getCode()+"' class='open "+(idx == items.size() ? "last" : "")+"'>");
			else 
				builder.append("<li id='"+item.getCode()+"' class='"+(idx == items.size() ? "last" : "")+"'>");
			
			if(item.getUri() == null) 
				builder.append("<a href='javascript:;'>");
			else {
				if(item.getUri().startsWith("http://") || item.getUri().startsWith("https://"))
					builder.append("<a href='"+ item.getUri()+"'>");
				else
					builder.append("<a href='"+ context + "/" + item.getUri()+"'>");
			}
			
			if(item.getIcon() != null) 
				builder.append("<i class='icon-"+item.getIcon()+"'></i>");
			
			builder.append("<span class='title'>"+item.getName()+"</span>");
			if(!CollectionUtils.isEmpty(item.getChildren())) {
				if(itemIds.contains(item.getId()))
					builder.append("<span class='arrow open'></span>");
				else 
					builder.append("<span class='arrow'></span>");
			}
			
			builder.append("</a>");
			
			if(!CollectionUtils.isEmpty(item.getChildren())) { 
				if(itemIds.contains(item.getId()))
					builder.append("<ul class='sub-menu' style='display: block;'>");
				else 
					builder.append("<ul class='sub-menu' style='display: none;'>");
				
				for(Func child : item.getChildren()) builder.append(appendChildren(child, itemIds));
				builder.append("</ul>");
			}
			
			builder.append("</li>");
			idx ++;
		}
		
		model.addAttribute(SIDEBAR, builder.toString());
	}
	
	private String appendChildren(Func item, Set<Long> itemIds) {
		StringBuilder builder = new StringBuilder();
		if(itemIds.contains(item.getId()))
			builder.append("<li id='"+item.getCode()+"' class='open'>");
		else 
			builder.append("<li id='"+item.getCode()+"' class=''>");
		
		if(item.getUri() == null)
			builder.append("<a href='javascript:;'>");
		else {
			if(item.getUri().startsWith("http://") || item.getUri().startsWith("https://"))
				builder.append("<a href='"+ item.getUri()+"'>");
			else
				builder.append("<a href='"+ context + "/" + item.getUri()+"'>");
		}
		
		if(item.getIcon() != null) 
			builder.append("<i class='icon-"+item.getIcon()+"'></i>");
		
		builder.append("<span class='title'>"+item.getName()+"</span>");
		
		if(!CollectionUtils.isEmpty(item.getChildren())) {
			if(itemIds.contains(item.getId()))
				builder.append("<span class='arrow open'></span>");
			else 
				builder.append("<span class='arrow'></span>");
		}
		
		builder.append("</a>");
		if(!CollectionUtils.isEmpty(item.getChildren())) {
			if(itemIds.contains(item.getId()))
				builder.append("<ul class='sub-menu' style='display: block;'>");
			else 
				builder.append("<ul class='sub-menu' style='display: none;'>");
			
			for(Func child : item.getChildren()) {
				builder.append(appendChildren(child, itemIds));
			}
			
			builder.append("</ul>");
		}
		
		builder.append("</li>");
			
		return builder.toString();
	}
	
	@Override
	public Map<Long, Func> findSelectFunc(String uri) {
		MachairodusAuthorizationInfo info = (MachairodusAuthorizationInfo) permissionService.findPermissions();
		List<Func> funcs;
		if(info != null && !CollectionUtils.isEmpty(funcs = info.getFuncs())) {
			Map<Long, Func> funcMap = Maps.newLinkedHashMap();
			if(uri.startsWith(context))
				uri = uri.replace(context + "/", "");
			
			findChildren(funcs, funcMap, uri);
			return funcMap;
		}
		
		return Collections.emptyMap();
	}
	
	private boolean findChildren(List<Func> children, Map<Long, Func> funcMap, String uri) {
		if(!CollectionUtils.isEmpty(children)) {
			for(Func child : children) {
				if(!CollectionUtils.isEmpty(child.getChildren())) {
					boolean hasCode = findChildren(child.getChildren(), funcMap, uri);
					if(hasCode) {
						funcMap.put(child.getId(), child);
						return true;
					}
					
					continue ;
				}
					
				if(uri.equals(child.getUri())) {
					funcMap.put(child.getId(), child);
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void buildNavigate(Collection<Func> funcs, Model model) {
		if(CollectionUtils.isEmpty(funcs)) {
			model.addAttribute(NAVIGATE, "<h3 class='page-title'>首页 <small>Machairodus Scheduler</small></h3>");
			return ;
		}
		
		final StringBuilder builder = new StringBuilder("<ul class='breadcrumb'><li><i class='icon-home'></i><a href='"+context+"/index'>首页</a><i class='icon-angle-right'></i></li>");
		final AtomicInteger idx = new AtomicInteger(1);
		final StringBuilder pageTitle = new StringBuilder(); 
		funcs.stream().sorted((before, after) -> before.getHierarchy().compareTo(after.getHierarchy()))
		.forEach(func -> {
			builder.append("<li><a href='javascript:;'>").append(func.getName()).append("</a>");
			if(idx.get() < funcs.size())
				builder.append("<i class='icon-angle-right'></i>");
			else 
				pageTitle.append("<h3 class='page-title'>"+func.getName()+"</h3>");
			
			builder.append("</li>");
			idx.incrementAndGet();
		});
		
		builder.append("</ul>");
		model.addAttribute(NAVIGATE, pageTitle.toString() + builder.toString());
	}
	
	@Override
	public void buildTitle(Collection<Func> funcs, String uri, Model model) {
		if(CollectionUtils.isEmpty(funcs)) {
			model.addAttribute(TITLE, "首页");
			return ;
		}
		
		funcs.stream().filter(func -> uri.equals(context + "/" + func.getUri())).forEach(func -> model.addAttribute(TITLE, func.getName()));
	}
	
}
