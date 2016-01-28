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

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.Quartz;
import org.machairodus.topology.quartz.QuartzException;
import org.machairodus.topology.quartz.defaults.monitor.Statistic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Quartz(name = "StatisticEchoQuartz", beforeAfterOnly = true, interval = 1000, parallel = 0)
public class StatisticEchoQuartz extends BaseQuartz {
	
	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		LOG.info(JSON.toJSONString(Statistic.getInstance().getPointer(), SerializerFeature.WriteDateUseDateFormat));
	}

	@Override
	public void after() throws QuartzException {
		
	}

	@Override
	public void destroy() throws QuartzException {
		
	}

}
