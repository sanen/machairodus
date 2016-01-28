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
package org.machairodus.topology.scheduler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.machairodus.topology.domain.Test;
import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.Quartz;
import org.machairodus.topology.quartz.QuartzException;
import org.machairodus.topology.quartz.defaults.monitor.Statistic;
import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.util.CollectionUtils;

@Quartz(name = "TestWorkerQuartz2", queueName = "Test2", closeTimeout = 180000, parallelProperty = "quartz.worker.test.parallel")
public class TestWorkerQuartz2 extends BaseQuartz {
	private List<Test> data;
	private Random random = new Random();
	
	@Override
	public void before() throws QuartzException {
		data = BlockingQueueFactory.getInstance().poll(Test.class.getSimpleName() + "2", 100, 1000, TimeUnit.MILLISECONDS);
		
	}

	@Override
	public void execute() throws QuartzException {
		if(!CollectionUtils.isEmpty(data)) {
			for(@SuppressWarnings("unused") Test test : data) {
				thisWait(random.nextInt(10));
				Statistic.getInstance().incrementAndGet(Test.class.getSimpleName() + "2");
			}
			
//			LOG.debug("消费数据2[" + getConfig().getTotal() + "-" + getConfig().getNum() + "]: " + data.size());
		}
	}

	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {

	}

}
