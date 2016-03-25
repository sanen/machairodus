/**
 * Copyright 2015- the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务调度注解
 * 
 * @author yanghe
 * @date 2015年6月11日 下午2:32:03 
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduler {
	
	/**
	 * 调度任务before()和after()方法是否在整个任务生命周期中只执行一次, 为true时只执行, 默认: false
	 * @return boolean
	 */
	boolean beforeAfterOnly() default false;
	
	/**
	 * 任务执行次数, 为0时无限执行, 直到被主动关闭 默认: 0
	 * @return int
	 */
	int runNumberOfTimes() default 0;
	
	/**
	 * 执行间隔时间, 单位: 毫秒, 默认时间: 0
	 * @return int
	 */
	long interval() default 0;
	
	/**
	 * 并行度，调度任务线程数, 并行度必须大于等于0 , 默认并行度: 0
	 * @return int
	 */
	int parallel() default 0;
	
	/**
	 * 通过属性文件设置并行度, 如果设置了此属性并且可以获取到属性值，则使用此属性设置，并无视parallel的设置, 默认值: ""
	 * @return String
	 */
	String parallelProperty() default "";
	
	/**
	 * 采用核心并行度，并无视parallel及parallelProperty的设置, 默认值: false
	 * @return boolean
	 */
	boolean coreParallel() default false;
	
	/**
	 * cron表达式
	 * @return
	 * @see org.nanoframework.extension.concurrent.scheduler.CronExpression
	 */
	String cron() default "";
	
	/**
	 * 通过属性文件设置cron表达式
	 * @return
	 */
	String cronProperty() default "";
	
	/**
	 * 是否守护线程，默认为用户线程
	 * @return
	 */
	boolean daemon() default false;
	
	/**
	 * 启动时进行延迟
	 * @return
	 */
	boolean lazy() default false;
	
	/**
	 * 用户自定义属性
	 * @return
	 */
	String[] defined() default { };
	
	/**
	 * 针对剑齿虎扩展包的扩展实现, 只在数据获取线程中有效
	 * @return
	 */
	Class<? extends BaseScheduler> workerClass() default BaseScheduler.class;
	
	String workerClassProperty() default "";
	
	/**
	 * 针对剑齿虎扩展包的扩展实现, 增加队列名称, Default: "", 只在工作线程(worker)中有效
	 * @return
	 */
	String queueName() default "";
	
	String queueNameProperty() default "";
	
	/**
	 * 针对剑齿虎扩展包的扩展实现，停止工作线程(worker)超时时长，默认: 180000，当timeout为0时，表示无限等待，单位: ms
	 * @return
	 */
	long closeTimeout() default 180000;
	
	String closeTimeoutProperty() default "";
	
}
