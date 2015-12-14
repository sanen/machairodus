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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.machairodus.manager.service.PermissionService;

public class PermissionServiceImpl implements PermissionService {
	
	@Override
	public AuthorizationInfo findPermissions() {
		Collection<Realm> realms = ((DefaultWebSecurityManager) SecurityUtils.getSecurityManager()).getRealms();
		Cache<Object, AuthorizationInfo> cache = ((AuthorizingRealm) realms.iterator().next()).getAuthorizationCache();
		AuthorizationInfo info = cache.get(SecurityUtils.getSubject().getPrincipals());
		if(info == null) {
			SecurityUtils.getSubject().isPermitted("*");
			info = cache.get(SecurityUtils.getSubject().getPrincipals());
		}
		
		return info;
	}
	
}
