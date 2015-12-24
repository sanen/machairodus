package org.machairodus.topology.quartz.defaults;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.Quartz;
import org.machairodus.topology.quartz.QuartzException;

@Quartz(name = "StatisticQuartz", beforeAfterOnly = true, cron = "* * * * * ?", parallel = 1)
public class StatisticQuartz extends BaseQuartz {

	@Override
	public void before() throws QuartzException {
		
	}

	@Override
	public void execute() throws QuartzException {
		Statistic.getInstance().setPointer(1);
	}

	@Override
	public void after() throws QuartzException {
		
	}

	@Override
	public void destroy() throws QuartzException {

	}

}