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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.Quartz;
import org.machairodus.topology.quartz.QuartzException;
import org.machairodus.topology.quartz.defaults.Statistic;
import org.machairodus.topology.quartz.defaults.StatisticQuartz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

@Quartz(name = "StatisticEchoQuartz", interval = 1000, parallel = 1)
public class StatisticEchoQuartz extends BaseQuartz {
	private Logger LOG = LoggerFactory.getLogger(StatisticQuartz.class);
	private BlockingQueue<List<Map<String , Object>>> pointerQueue;
	
	@Override
	public void before() throws QuartzException {
		pointerQueue = Statistic.getPointerQueue();
	}

	@Override
	public void execute() throws QuartzException {
		if(pointerQueue != null) {
			while(pointerQueue.size() > 0) {
				try {
					List<Map<String , Object>> pointer = pointerQueue.poll(1000, TimeUnit.MILLISECONDS);
					String jsonPointer = JSON.toJSONString(pointer);
					LOG.info("Pointer: " + jsonPointer);
					
				} catch(InterruptedException e) {
					LOG.error(StatisticQuartz.class.getName() + " : " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void after() throws QuartzException {
		pointerQueue = null;
	}

	@Override
	public void destroy() throws QuartzException {
		LOG = null;
	}

}
