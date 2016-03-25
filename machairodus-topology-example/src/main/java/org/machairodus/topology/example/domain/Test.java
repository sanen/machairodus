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
package org.machairodus.topology.example.domain;

import org.machairodus.topology.entity.BaseEntity;

public class Test extends BaseEntity {
	private static final long serialVersionUID = 7325047933690548752L;
	
	private Long id;
	private String name;

	public Test() { }
	
	public Test(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static Test create(Long id, String name) {
		return new Test(id, name);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
