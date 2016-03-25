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

import java.util.List;
import java.util.Set;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.machairodus.mappers.domain.Func;
import org.machairodus.mappers.domain.Role;

public class MachairodusAuthorizationInfo extends SimpleAuthorizationInfo {
	private static final long serialVersionUID = -9092266191737548610L;

	private Set<Role> roleSet;
	private List<Func> funcs;
	
    public MachairodusAuthorizationInfo() {
    }

    public MachairodusAuthorizationInfo(Set<String> roles) {
        this.roles = roles;
    }

	public Set<Role> getRoleSet() {
		return roleSet;
	}

	public void setRoleSet(Set<Role> roleSet) {
		this.roleSet = roleSet;
	}

	public List<Func> getFuncs() {
		return funcs;
	}
	
	public void setFuncs(List<Func> funcs) {
		this.funcs = funcs;
	}

}
