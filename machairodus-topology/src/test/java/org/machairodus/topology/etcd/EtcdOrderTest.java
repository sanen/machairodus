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

import org.junit.Test;
import org.machairodus.topology.quartz.defaults.EtcdOrder;
import org.machairodus.topology.quartz.defaults.EtcdOrder.OrderAction;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

public class EtcdOrderTest {

	private Logger LOG = LoggerFactory.getLogger(EtcdOrderTest.class);
	
	@Test
	public void createOrder() {
		EtcdOrder order = new EtcdOrder();
		order.setAction(OrderAction.STOP);
		order.setGroup("TestWorkerQuartz2");
		order.setId("TestWorkerQuartz2-4");
		
		LOG.debug(order.toString());
	}
}
