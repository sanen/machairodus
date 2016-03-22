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
package org.machairodus.topology.example.scheduler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.machairodus.topology.example.domain.Test;
import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.scheduler.BaseScheduler;
import org.machairodus.topology.scheduler.Scheduler;
import org.machairodus.topology.scheduler.SchedulerException;
import org.machairodus.topology.scheduler.defaults.monitor.Statistic;
import org.machairodus.topology.util.CollectionUtils;

@Scheduler(queueName = "Test", closeTimeout = 180000, parallelProperty = "scheduler.worker.test.parallel")
public class TestWorkerScheduler extends BaseScheduler {
	private List<Test> data;
	private Random random = new Random();
	
	@Override
	public void before() throws SchedulerException {
		data = BlockingQueueFactory.getInstance().poll(Test.class.getSimpleName(), 100, 1000, TimeUnit.MILLISECONDS);
		
	}

	@Override
	public void execute() throws SchedulerException {
		if(!CollectionUtils.isEmpty(data)) {
			for(@SuppressWarnings("unused") Test test : data) {
				thisWait(random.nextInt(10));
				Statistic.getInstance().incrementAndGet(Test.class.getSimpleName());
			}
			
//			LOG.debug("消费数据[" + getConfig().getTotal() + "-" + getConfig().getNum() + "]: " + data.size());
		}
	}

	@Override
	public void after() throws SchedulerException {

	}

	@Override
	public void destroy() throws SchedulerException {

	}

}
