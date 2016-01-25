package org.machairodus.topology.quartz.defaults;

import static org.machairodus.topology.quartz.QuartzFactory.DEFAULT_QUARTZ_NAME_PREFIX;
import static org.machairodus.topology.quartz.QuartzFactory.threadFactory;

import java.text.ParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.CronExpression;
import org.machairodus.topology.quartz.QuartzConfig;
import org.machairodus.topology.quartz.QuartzException;

/**
 * 现在作为插件包中默认启动的任务随进程启动
 * 
 * @author yanghe
 * @date 2016年1月25日 上午10:32:17
 */
public class StatisticQuartz extends BaseQuartz {

	public StatisticQuartz() {
		QuartzConfig config = new QuartzConfig();
		config.setId("StatisticQuartz-0");
		config.setName(DEFAULT_QUARTZ_NAME_PREFIX + "StatisticQuartz-0");
		config.setGroup("StatisticQuartz");
		threadFactory.setBaseQuartz(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
		try { config.setCron(new CronExpression("* * * * * ?")); } catch(ParseException e) {}
		config.setTotal(1);
		config.setDaemon(true);
		setConfig(config);
		setClose(false);
	}
	
	
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