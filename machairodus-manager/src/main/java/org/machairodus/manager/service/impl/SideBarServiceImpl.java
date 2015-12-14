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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.machairodus.manager.auth.MachairodusAuthorizationInfo;
import org.machairodus.manager.service.PermissionService;
import org.machairodus.manager.service.SideBarService;
import org.machairodus.mappers.domain.Func;
import org.nanoframework.commons.util.CollectionUtils;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class SideBarServiceImpl implements SideBarService {

	@Inject
	private PermissionService permissionService;
	
	@Override
	public String build(Long id) {
		MachairodusAuthorizationInfo info = (MachairodusAuthorizationInfo) permissionService.findPermissions();
		List<Func> items;
		if(info == null || CollectionUtils.isEmpty(items = info.getFuncs())) 
			return null;
		
		Set<Long> itemIds = Collections.emptySet();
		if(id != null) 
			itemIds = findSelectItem(id);
		
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
				String uri = item.getUri();
				if(item.getUri().contains("?"))
					uri += "&_id=" + item.getId();
				else 
					uri += "?_id=" + item.getId();
				
				builder.append("<a href='"+uri+"'>");
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
				
				for(Func child : item.getChildren()) builder.append(appendChildren(child, id, itemIds));
				builder.append("</ul>");
			}
			
			builder.append("</li>");
			idx ++;
		}
		
		return builder.toString();
	}
	
	private String appendChildren(Func item, Long id, Set<Long> itemIds) {
		StringBuilder builder = new StringBuilder();
		if(itemIds.contains(item.getId()))
			builder.append("<li id='"+item.getCode()+"' class='open'>");
		else 
			builder.append("<li id='"+item.getCode()+"' class=''>");
		
		if(item.getUri() == null)
			builder.append("<a href='javascript:;'>");
		else {
			String uri = item.getUri();
			if(item.getUri().contains("?"))
				uri += "&_id=" + item.getId();
			else 
				uri += "?_id=" + item.getId();
			
			builder.append("<a href='"+uri+"'>");
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
				builder.append(appendChildren(child, id, itemIds));
			}
			
			builder.append("</ul>");
		}
		
		builder.append("</li>");
			
		return builder.toString();
	}
	
	@Override
	public Set<Long> findSelectItem(Long id) {
		MachairodusAuthorizationInfo info = (MachairodusAuthorizationInfo) permissionService.findPermissions();
		List<Func> funcs;
		if(info != null && !CollectionUtils.isEmpty(funcs = info.getFuncs())) {
			Set<Long> itemIds = Sets.newHashSet();
			findChildren(funcs, itemIds, id);
			return itemIds;
		}
		
		return Collections.emptySet();
	}
	
	private boolean findChildren(List<Func> children, Set<Long> itemIds, Long id) {
		if(!CollectionUtils.isEmpty(children)) {
			for(Func child : children) {
				if(!CollectionUtils.isEmpty(child.getChildren())) {
					boolean hasCode = findChildren(child.getChildren(), itemIds, id);
					if(hasCode) {
						itemIds.add(child.getId());
						return true;
					}
					
					continue ;
				}
					
				if(child.getId().equals(id)) {
					itemIds.add(child.getId());
					return true;
				}
			}
		}
		
		return false;
	}

}
