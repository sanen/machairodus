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
package org.machairodus.manager.auth;

import org.apache.shiro.subject.PrincipalCollection;
import org.machairodus.mappers.domain.User;
import org.nanoframework.ext.shiro.authc.SimpleAuthenticationInfo;
import org.nanoframework.ext.shiro.util.ByteSource;

public class MachairodusAuthenticationInfo extends SimpleAuthenticationInfo {
	private static final long serialVersionUID = -205944346354257424L;
	
	private User user;

	public MachairodusAuthenticationInfo() {

	}

	public MachairodusAuthenticationInfo(Object principal, Object credentials, String realmName, User user) {
		super(principal, credentials, realmName);
		this.user = user;
	}

	public MachairodusAuthenticationInfo(Object principal, Object hashedCredentials, ByteSource credentialsSalt, String realmName, User user) {
		super(principal, hashedCredentials, credentialsSalt, realmName);
		this.user = user;
	}

	public MachairodusAuthenticationInfo(PrincipalCollection principals, Object credentials, User user) {
		super(principals, credentials);
		this.user = user;
	}

	public MachairodusAuthenticationInfo(PrincipalCollection principals, Object hashedCredentials, ByteSource credentialsSalt, User user) {
		super(principals, hashedCredentials, credentialsSalt);
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
