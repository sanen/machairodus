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
package org.machairodus.manager;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.commons.lang3.ArrayUtils;
import org.machairodus.manager.service.Pointer;
import org.machairodus.manager.service.StatisticMXBean;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.JmxClientManager;

import com.alibaba.fastjson.JSON;

public class DefineMBeanTest {
	private static Logger LOG = LoggerFactory.getLogger(DefineMBeanTest.class);
	
	public static void main(String[] args) {
		try {
			JmxClient client = JmxClientManager.get("localhost:18088");
			MBeanServerConnection connection = client.getConnection();
			while(true) {
				CompositeData[] pointerComposites = (CompositeData[]) connection.getAttribute(new ObjectName(StatisticMXBean.STATISTIC_MXBEAN_NAME), "Pointer");
				List<Pointer> pointerList = null;
				if(!ArrayUtils.isEmpty(pointerComposites)) {
					pointerList = new ArrayList<>();
					for(CompositeData data : pointerComposites) {
						pointerList.add(Pointer.from(data));
					}
				}
				
				LOG.debug(JSON.toJSONString(pointerList));
				Thread.sleep(1000L);
			}
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
