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
package org.machairodus.controller.quartz;

import java.util.Map;

import org.machairodus.controller.service.LoadBalanceService;
import org.machairodus.mappers.domain.JmxMonitor;
import org.machairodus.mappers.domain.NodeType;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.Quartz;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Quartz(name = "LoadBalancerQuartz", cron = "* * * * * ?", parallel = 1)
public class LoadBalanceQuartz extends BaseQuartz {

	@Inject
	private LoadBalanceService loadBalanceService;
	
	private TypeReference<JmxMonitor> type = new TypeReference<JmxMonitor>() { };
	private Map<String, JmxMonitor> balancer;
	private Map<String, JmxMonitor> scheduler;
	private Map<String, JmxMonitor> service;
	
	@Override
	public void before() throws QuartzException {
		balancer = loadBalanceService.load(NodeType.BALANCER.name(), type);
		scheduler = loadBalanceService.load(NodeType.SCHEDULER.name(), type);
		service = loadBalanceService.load(NodeType.SERVICE_NODE.name(), type);
		
	}
	
	@Override
	public void execute() throws QuartzException {
		loadBalanceService.executeTimeout(NodeType.BALANCER.name(), balancer);
		loadBalanceService.executeTimeout(NodeType.SCHEDULER.name(), scheduler);
		loadBalanceService.executeTimeout(NodeType.SERVICE_NODE.name(), service);
		
		Map<String, Double> cpuRatios = Maps.newHashMap();
		loadBalanceService.loadBalance(NodeType.BALANCER.name(), balancer, cpuRatios);
		loadBalanceService.loadBalance(NodeType.SCHEDULER.name(), scheduler, cpuRatios);
		loadBalanceService.loadBalance(NodeType.SERVICE_NODE.name(), service, cpuRatios);
		
		loadBalanceService.loadBalance0(NodeType.BALANCER.name(), balancer, cpuRatios);
		loadBalanceService.loadBalance0(NodeType.SCHEDULER.name(), scheduler, cpuRatios);
		loadBalanceService.loadBalance0(NodeType.SERVICE_NODE.name(), service, cpuRatios);
	}
	
	@Override
	public void after() throws QuartzException {
		if(!CollectionUtils.isEmpty(balancer)) {
			balancer.clear();
			balancer = null;
		}
		
		if(!CollectionUtils.isEmpty(scheduler)) {
			scheduler.clear();
			scheduler = null;
		}
		
		if(!CollectionUtils.isEmpty(service)) {
			service.clear();
			service = null;
		}
			
	}

	@Override
	public void destroy() throws QuartzException {

	}

}
