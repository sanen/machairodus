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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.machairodus.topology.example.domain.Test;
import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.scheduler.BaseScheduler;
import org.machairodus.topology.scheduler.Scheduler;
import org.machairodus.topology.scheduler.SchedulerException;
import org.nanoframework.commons.util.UUIDUtils;

@Scheduler(beforeAfterOnly = true, parallel = 1)
public class DataCreatorScheduler extends BaseScheduler {
	private AtomicLong id = new AtomicLong(0);
	private Test test;
	
	static {
		BlockingQueueFactory.getInstance().initQueue(Test.class.getName(), 10000);
	}
	
	@Override
	public void before() throws SchedulerException {
		
	}

	@Override
	public void execute() throws SchedulerException {
		if(test == null)
			test = Test.create(id.incrementAndGet(), UUIDUtils.create());
		
		try { 
			BlockingQueueFactory.getInstance().offer(Test.class.getName(), test, 1000L, TimeUnit.MILLISECONDS);
			test = null;
		} catch(InterruptedException e) { }
	}

	@Override
	public void after() throws SchedulerException {

	}

	@Override
	public void destroy() throws SchedulerException {

	}

}
