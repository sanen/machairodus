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
package org.machairodus.manager.realm;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.machairodus.manager.auth.MachairodusAuthenticationInfo;
import org.machairodus.manager.auth.MachairodusAuthorizationInfo;
import org.machairodus.mappers.domain.Func;
import org.machairodus.mappers.domain.Role;
import org.machairodus.mappers.domain.User;
import org.machairodus.mappers.mapper.manager.FuncMapper;
import org.machairodus.mappers.mapper.manager.RoleMapper;
import org.machairodus.mappers.mapper.manager.UserMapper;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.ext.shiro.util.ByteSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

public class MachairodusRealm extends JdbcRealm {
	private Logger LOG = LoggerFactory.getLogger(MachairodusRealm.class);
	
	private UserMapper userMapper;
	private RoleMapper roleMapper;
	private FuncMapper funcMapper;
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }
        
        MachairodusAuthenticationInfo info;
        try {
        	if(userMapper == null)
    			userMapper = Globals.get(Injector.class).getInstance(UserMapper.class);
        	
        	User user = userMapper.findUserByUsername(username);
        	if(user == null)
        		throw new UnknownAccountException("No account found for user [" + username + "]");
        	
        	info = new MachairodusAuthenticationInfo(username, user.getPassword().toCharArray(), getName(), user);
            
        	String salt = null;
	        switch (saltStyle) {
	            case NO_SALT:
	                break;
	            case CRYPT:
	                throw new ConfigurationException("Not implemented yet");
	            case COLUMN:
	                salt = user.getPasswordSalt();
	                break;
	            case EXTERNAL:
	                salt = getSaltForUser(username);
            }
	        
        	if (salt != null) {
                info.setCredentialsSalt(ByteSource.Util.bytes(salt));
            }
        	
        } catch(Exception e) {
        	if(e instanceof AuthenticationException)
        		throw e;
        	
        	final String message = "There was a SQL error while authenticating user [" + username + "]";
        	LOG.error(message, e);
            throw new AuthenticationException(message, e);
        }
        
        assertCredentialsMatch(upToken, info);
        
        return info;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        String username = (String) getAvailablePrincipal(principals);
        
        MachairodusAuthorizationInfo info = new MachairodusAuthorizationInfo();
        if(roleMapper == null)
        	roleMapper = Globals.get(Injector.class).getInstance(RoleMapper.class);
        
        Set<Role> roles = roleMapper.findRolesByUserId(username);
        info.setRoleSet(roles);
        if(permissionsLookupEnabled && !CollectionUtils.isEmpty(roles)) {
        	Set<Long> roleIds = Sets.newLinkedHashSet();
        	Set<String> _roleIds = Sets.newLinkedHashSet();
        	roles.forEach(role -> {
        		roleIds.add(role.getId());
        		_roleIds.add(String.valueOf(role.getId()));
        	});
        	
        	if(funcMapper == null)
        		funcMapper = Globals.get(Injector.class).getInstance(FuncMapper.class);
        	
        	Set<Func> funcs = funcMapper.findFuncByRoleIds(roleIds);
        	Set<String> permissions = Sets.newLinkedHashSet();
        	funcs.forEach(func -> permissions.add(func.getCode()));
        	
        	info.setRoles(_roleIds);
        	info.setFuncs(setRoots(funcs));
        	info.setStringPermissions(permissions);
        }
        
        return info;
	}
	
	private List<Func> setRoots(Set<Func> funcs) {
		List<Func> roots = Lists.newArrayList();
		funcs.forEach(func -> {
			if(func.getParentId() == null) {
				roots.add(func);
			}
		});
		
		Collections.sort(roots, (before, after) -> before.getHierarchy().compareTo(after.getHierarchy()));
		roots.forEach(func -> setChildren(func, funcs));
		return roots;
	}
	
	private void setChildren(Func func, Set<Func> funcs) {
		List<Func> children = Collections.synchronizedList(Lists.newArrayList());
		funcs.parallelStream().filter(item -> item.getParentId() != null).forEach(item -> {
			if(func.getId().equals(item.getParentId())) {
				setChildren(item, funcs);
				children.add(item);
			}
		});
		
		Collections.sort(children, (before, after) -> before.getHierarchy().compareTo(after.getHierarchy()));
		func.setChildren(children);
	}
	
	public void setSaltStyle(String saltStyle) {
		this.saltStyle = SaltStyle.valueOf(saltStyle);
	}
	
	@Override
	public String getSaltForUser(String username) {
		return "Nano Framework Extension Shiro Salt for user: [admin]";
	}
}
