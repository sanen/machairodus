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
import java.util.concurrent.TimeUnit;

import org.machairodus.topology.example.domain.Test;
import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.scheduler.BaseScheduler;
import org.machairodus.topology.scheduler.Scheduler;
import org.machairodus.topology.util.CollectionUtils;

@Scheduler(workerClassProperty = "scheduler.data-loader.test.worker.class", parallelProperty = "scheduler.data-loader.test.parallel")
public class TestDataLoaderScheduler extends BaseScheduler {
	private List<Test> data;
	
	static {
		BlockingQueueFactory.getInstance().initQueue(Test.class.getSimpleName(), 10000);
	}
	
	@Override
	public void before() {
		if(CollectionUtils.isEmpty(data) && BlockingQueueFactory.getInstance().getQueue(Test.class.getSimpleName()).size() < 100) {
			data = BlockingQueueFactory.getInstance().poll(Test.class.getName(), 1000, 1000, TimeUnit.MILLISECONDS);
//			LOG.debug("抓取数据[" + getConfig().getTotal() + "-" + getConfig().getNum() + "]: " + data.size());
		} else {
			thisWait(1000);
		}
	}

	@Override
	public void execute() {
		if(!CollectionUtils.isEmpty(data)) {
			for(Test item : data) {
				boolean offed = false;
				while(!offed) {
					try { 
						BlockingQueueFactory.getInstance().offer(Test.class.getSimpleName(), item, 1000, TimeUnit.MILLISECONDS);
						offed = true;
					} catch(InterruptedException e) { }
				}
			}
		}
	}

	@Override
	public void after() {
		if(data != null) {
			data.clear();
			data = null;
		}
	}

	@Override
	public void destroy() {

	}

}
