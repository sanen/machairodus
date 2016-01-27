/**
 * Copyright 2015-2016 the original author or authors.
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
package org.machairodus.topology.etcd;

import org.junit.Ignore;
import org.junit.Test;
import org.machairodus.topology.quartz.defaults.etcd.EtcdAppInfo;
import org.machairodus.topology.quartz.defaults.etcd.EtcdOrder;
import org.machairodus.topology.quartz.defaults.etcd.EtcdOrder.OrderAction;
import org.machairodus.topology.util.CryptUtil;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class EtcdOrderCreatorTest {
	private Logger LOG = LoggerFactory.getLogger(EtcdOrderCreatorTest.class);
	
	@Test
	public void createOrder() {
		EtcdOrder order = new EtcdOrder();
		order.setAction(OrderAction.STOP);
		order.setGroup("TestWorkerQuartz2");
		order.setId("TestWorkerQuartz2-4");
		
		LOG.debug("STOP: " + CryptUtil.encrypt(order.toString(), "4b238cbb7565549833008d3f0ff2bbb6"));
		
		EtcdOrder stopGroup = new EtcdOrder();
		stopGroup.setAction(OrderAction.STOP_GROUP);
		stopGroup.setGroup("TestWorkerQuartz2");
		
		LOG.debug("STOP_GROUP: " + CryptUtil.encrypt(stopGroup.toString(), "4b238cbb7565549833008d3f0ff2bbb6"));
		
		EtcdOrder removeGroup = new EtcdOrder();
		removeGroup.setAction(OrderAction.REMOVE_GROUP);
		removeGroup.setGroup("TestWorkerQuartz2");
		
		LOG.debug("REMOVE_GROUP: " + CryptUtil.encrypt(removeGroup.toString(), "4b238cbb7565549833008d3f0ff2bbb6"));
		
		EtcdOrder stopAll = new EtcdOrder();
		stopAll.setAction(OrderAction.STOP_ALL);
		
		LOG.debug("STOP_ALL" + CryptUtil.encrypt(stopAll.toString(), "4b238cbb7565549833008d3f0ff2bbb6"));
		
		EtcdOrder startAll = new EtcdOrder();
		startAll.setAction(OrderAction.START_ALL);
		
		LOG.debug("START_ALL: " + CryptUtil.encrypt(startAll.toString(), "4b238cbb7565549833008d3f0ff2bbb6"));
	}
	
	@Ignore
	@Test
	public void decodeAppInfo() {
		String value = "Rjg3NTU3NDIwRTY3RjlFRTIzMDJFNDhENzdFOEZCMjJDMUU2OThGQjNFRTA1QUQzMjQyRjMxRTRDMERCMEQ2RUY2ODVDQjM3QzQyNEEzMjhBNjU2RUFBMTNFOEJGQUU3QkM0NDJCNzM1OEJGQjc3NzM4QkE5QUQxNDJBRTNGQkVGQkFEQ0FFNkZGRUU1Qzc1NjhBMUJCMkU1MERGODU0OEQxRkRDOEUzMTE4RjM3NzAwQzA0QTVCQjI2QTk3Nzk3";
		EtcdAppInfo info = JSON.parseObject(CryptUtil.decrypt(value, "4b238cbb7565549833008d3f0ff2bbb6"), new TypeReference<EtcdAppInfo>() { });
		LOG.debug(info.toString());
	}
}
