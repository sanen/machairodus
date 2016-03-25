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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.lang3.ArrayUtils;
import org.machairodus.manager.service.Pointer;
import org.machairodus.manager.service.StatisticMXBean;
import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;

public class StatisticImpl extends AbstractMXBean implements StatisticMXBean {
	public static final String POINTER = "Pointer";
	
	public StatisticImpl(JmxClient client) {
		init(client, StatisticMXBean.STATISTIC_MXBEAN_NAME);
	}
	
	public StatisticImpl(JmxClient client, ObjectName objectName) {
		this.client = client;
		this.connection = client.getConnection();
		this.objectName = objectName;
	}
	
	@Override
	public List<Pointer> getPointer() {
		CompositeData[] datas = getAttribute(POINTER);
		if(!ArrayUtils.isEmpty(datas)) {
			List<Pointer> pointers = new ArrayList<>();
			for(CompositeData data : datas) {
				pointers.add(Pointer.from(data));
			}
			
			return pointers;
		}
		
		return Collections.emptyList();
	}

}
