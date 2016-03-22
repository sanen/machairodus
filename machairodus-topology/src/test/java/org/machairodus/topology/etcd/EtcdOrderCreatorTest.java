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
import org.machairodus.topology.scheduler.defaults.etcd.EtcdAppInfo;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdOrder;
import org.machairodus.topology.scheduler.defaults.etcd.EtcdOrder.OrderAction;
import org.machairodus.topology.util.CryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class EtcdOrderCreatorTest {
	private Logger LOG = LoggerFactory.getLogger(EtcdOrderCreatorTest.class);
	
	@Test
	public void createOrder() {
		EtcdOrder order = new EtcdOrder();
		order.setAction(OrderAction.STOP);
		order.setGroup("TestWorkerScheduler2");
		order.setId("TestWorkerScheduler2-4");
		
		LOG.debug("STOP: " + CryptUtil.encrypt(order.toString(), "3a5e93c00786099381534c93af339ac8"));
		
		EtcdOrder stopGroup = new EtcdOrder();
		stopGroup.setAction(OrderAction.STOP_GROUP);
		stopGroup.setGroup("TestWorkerScheduler2");
		
		LOG.debug("STOP_GROUP: " + CryptUtil.encrypt(stopGroup.toString(), "3a5e93c00786099381534c93af339ac8"));
		
		EtcdOrder removeGroup = new EtcdOrder();
		removeGroup.setAction(OrderAction.REMOVE_GROUP);
		removeGroup.setGroup("TestWorkerScheduler2");
		
		LOG.debug("REMOVE_GROUP: " + CryptUtil.encrypt(removeGroup.toString(), "3a5e93c00786099381534c93af339ac8"));
		
		EtcdOrder stopAll = new EtcdOrder();
		stopAll.setAction(OrderAction.STOP_ALL);
		
		LOG.debug("STOP_ALL" + CryptUtil.encrypt(stopAll.toString(), "3a5e93c00786099381534c93af339ac8"));
		
		EtcdOrder startAll = new EtcdOrder();
		startAll.setAction(OrderAction.START_ALL);
		
		LOG.debug("START_ALL: " + CryptUtil.encrypt(startAll.toString(), "3a5e93c00786099381534c93af339ac8"));
	}
	
	@Ignore
	@Test
	public void decodeAppInfo() {
		String value = "Rjg3NTU3NDIwRTY3RjlFRTIzMDJFNDhENzdFOEZCMjJDMUU2OThGQjNFRTA1QUQzMjQyRjMxRTRDMERCMEQ2RUY2ODVDQjM3QzQyNEEzMjhBNjU2RUFBMTNFOEJGQUU3QkM0NDJCNzM1OEJGQjc3NzM4QkE5QUQxNDJBRTNGQkVGQkFEQ0FFNkZGRUU1Qzc1NjhBMUJCMkU1MERGODU0OEQxRkRDOEUzMTE4RjM3NzAwQzA0QTVCQjI2QTk3Nzk3";
		EtcdAppInfo info = JSON.parseObject(CryptUtil.decrypt(value, "3a5e93c00786099381534c93af339ac8"), new TypeReference<EtcdAppInfo>() { });
		LOG.debug(info.toString());
	}
}
