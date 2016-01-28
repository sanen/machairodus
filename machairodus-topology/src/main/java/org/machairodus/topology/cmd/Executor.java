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
package org.machairodus.topology.cmd;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.QuartzConfig;
import org.machairodus.topology.quartz.QuartzFactory;
import org.machairodus.topology.quartz.defaults.monitor.Statistic;
import org.machairodus.topology.queue.BlockingQueueFactory;
import org.machairodus.topology.util.CollectionUtils;
import org.machairodus.topology.util.ResultMap;
import org.machairodus.topology.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

@Deprecated
public class Executor {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	public static final String COMMAND = "command";
	public static final String ID = "id";
	public static final String GROUP = "group";
	public static final String SIZE = "size";
	public static final String AUTO_START = "auto_start";
	public static final String KEY = "key";
	public static final String STARTED = "started";
	public static final String STARTED_GROUP = "started-group";
	public static final String STOPPING = "stopping";
	public static final String STOPPING_GROUP = "stopping-group";
	public static final String STOPPED = "stopped";
	public static final String STOPPED_GROUP = "stopped-group";
	
	private static QuartzFactory FACTORY = QuartzFactory.getInstance();
	
	public static final void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Writer out = response.getWriter();
		
		try {
			String command = request.getParameter(COMMAND);
			Command cmd = Command.value(command);
			if(cmd == null) {
				ResultMap resultMap = ResultMap.create(400, "无效的command指令", "WARN");
				out.write(resultMap.toString());
				return ;
			}
			
			ResultMap resultMap = null;
			switch(cmd) {
				case START:
					resultMap = start(request.getParameter(ID));
					break;
					
				case START_GROUP: 
					resultMap = startGroup(request.getParameter(GROUP));
					break;
					
				case START_ALL:
					resultMap = startAll();
					break;
					
				case STOP: 
					resultMap = stop(request.getParameter(ID));
					break;
					
				case STOP_GROUP: 
					resultMap = stopGroup(request.getParameter(GROUP));
					break;
					
				case STOP_ALL: 
					resultMap = stopAll();
					break;
					
				case APPEND: 
					String size = request.getParameter(SIZE);
					String autoStart = request.getParameter(AUTO_START);
					if(StringUtils.isEmpty(size)) {
						size = "0";
					} else {
						try {
							Integer.parseInt(size);
						} catch(NumberFormatException e) { size = "0"; }
					}
					
					if(StringUtils.isEmpty(autoStart)) 
						autoStart = "false";
					
					resultMap = append(request.getParameter(GROUP), Integer.parseInt(size), Boolean.parseBoolean(autoStart));
					break;
					
				case REMOVE: 
					size = request.getParameter(SIZE);
					if(StringUtils.isEmpty(size)) {
						size = "0";
					} else {
						try {
							Integer.parseInt(size);
						} catch(NumberFormatException e) { size = "0"; }
					}
					
					String group = request.getParameter(GROUP);
					String id = request.getParameter(ID);
					
					if(!StringUtils.isEmpty(id)) 
						resultMap = remove(id);
					else if(!StringUtils.isEmpty(group))
						resultMap = remove(group, Integer.parseInt(size));
					else 
						resultMap = ResultMap.create(400, "无效的请求参数列表", "ERROR");
						
					break;
					
				case QUEUE: 
					queue(out);
					break;
					
				case TPS: 
					tps(out);
					break;
					
				case QUARTZ:
					quartz(out);
					break;
					
				case CMD: 
					cmds(out);
					break;
					
				default: 
					resultMap = ResultMap.create(400, "无效的指令", "ERROR");
					break;
			}
			
			if(resultMap != null)
				out.write(resultMap.toString());
			
		} catch(Exception e) {
			LOG.error("执行指令异常: " + e.getMessage());
			out.write(ResultMap.create(500, "内部处理异常: " + e.getMessage(), "ERROR").toString());
		} finally {
			if(out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
	private static final ResultMap start(String id) throws IOException {
		if(StringUtils.isEmpty(id)) 
			return ResultMap.create(400, "无效的任务ID", "WARN");
			
		if(!FACTORY.closed(id) && !FACTORY.started(id))
			return ResultMap.create(400, "不存在指定任务ID", "WARN");
		
		if(FACTORY.started(id)) 
			return ResultMap.create(400, "指定任务ID已启动", "WARN");
			
		FACTORY.start(id);
		return ResultMap.create(200, "启动指定任务: " + id, "SUCCESS");
	}
	
	private static final ResultMap startGroup(String group) throws IOException {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务组", "WARN");
		
		if(!FACTORY.hasClosedGroup(group) && !FACTORY.hasStartedGroup(group)) 
			return ResultMap.create(400, "不存在任务组: " + group, "WARN");
		
		FACTORY.startGroup(group);
		return ResultMap.create(200, "启动指定任务组: " + group, "SUCCESS");
	}
	
	private static final ResultMap startAll() throws IOException {
		if(FACTORY.getStoppedQuratz().size() == 0 && FACTORY.getStartedQuartz().size() == 0)
			return ResultMap.create(400, "不存在任何任务", "WARN");
		
		FACTORY.startAll();
		return ResultMap.create(200, "启动所有任务", "SUCCESS");
	}
	
	private static final ResultMap stop(String id) throws IOException {
		if(StringUtils.isEmpty(id)) 
			return ResultMap.create(400, "无效的任务ID", "WARN");
		
		if(!FACTORY.closed(id) && !FACTORY.started(id))
			return ResultMap.create(400, "不存在指定任务ID", "WARN");
		
		if(FACTORY.closed(id)) 
			return ResultMap.create(400, "指定任务ID已停止", "WARN");
		
		FACTORY.close(id);
		return ResultMap.create(200, "停止指定任务: " + id, "SUCCESS");
	}
	
	private static final ResultMap stopGroup(String group) throws IOException {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务GROUP", "WARN");
		
		if(!FACTORY.hasClosedGroup(group) && !FACTORY.hasStartedGroup(group)) 
			return ResultMap.create(400, "不存在任务组: " + group, "WARN");
		
		FACTORY.closeGroup(group);
		return ResultMap.create(200, "停止指定任务组: " + group, "SUCCESS");
	}
	
	private static final ResultMap stopAll() {
		if(FACTORY.getStoppedQuratz().size() == 0 && FACTORY.getStartedQuartz().size() == 0)
			return ResultMap.create(400, "不存在任何任务", "WARN");
		
		FACTORY.closeAll();
		return ResultMap.create(200, "停止所有", "SUCCESS");
	}
	
	private static final ResultMap append(String group, int size, boolean autoStart) {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务组", "WARN");
			
		if(size <= 0)
			return ResultMap.create(400, "无效的追加任务数", "WARN");
			
		BaseQuartz quartz = FACTORY.findLast(group);
		if(quartz == null) 
			return ResultMap.create(400, "不存在此任务组", "WARN");
		
		for(int idx = 0; idx < size; idx ++) {
			QuartzConfig config = (QuartzConfig) quartz.getConfig().clone();
			int total = config.getTotal();
			config.setTotal(total + 1);
			config.setNum(total);
			config.setId(group + "-" + config.getNum());
			config.setName("Quartz-Thread-Pool: " + config.getId());
			BaseQuartz _new = quartz.clone();
			_new.setClose(true);
			_new.setConfig(config);
			FACTORY.addQuartz(_new);
			if(autoStart)
				FACTORY.start(config.getId());
			
		}
		
		return ResultMap.create(200, "添加任务完成", "SUCCESS");
	}
	
	private static final ResultMap remove(String group, int size) {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务组", "WARN");
			
		if(size <= 0)
			return ResultMap.create(400, "无效的追加任务数", "WARN");
			
		int gorupSize = FACTORY.getGroupSize(group);
		if(gorupSize < size)
			size = gorupSize;
		
		for(int idx = 0; idx < size; idx ++) {
			BaseQuartz quartz = FACTORY.findLast(group);
			if(quartz == null) 
				return ResultMap.create(400, "不存在此任务组", "WARN");
			
			if(FACTORY.getGroupSize(group) > 1)
				FACTORY.removeQuartz(quartz);
			else if(!quartz.isClose())
				FACTORY.close(quartz.getConfig().getId());
				
		}
		
		if(FACTORY.getGroupSize(group) == 1) 
			return ResultMap.create(200, "任务组列表只存在一个任务", "SUCCESS");
		
		return ResultMap.create(200, "移除任务完成", "SUCCESS");
	}
	
	private static final ResultMap remove(String id) {
		BaseQuartz quartz = FACTORY.find(id);
		if(quartz == null) {
			Collection<BaseQuartz> stopedQuartz = FACTORY.getStoppedQuratz();
			if(!CollectionUtils.isEmpty(stopedQuartz)) {
				for(BaseQuartz item : stopedQuartz) {
					if(item.getConfig().getId().equals(id)) {
						quartz = item;
						break;
					}
				}
			}
		}
		
		if(quartz != null) {
			String group = id.substring(0, id.lastIndexOf("-"));
			if(FACTORY.getGroupSize(group) > 1)
				FACTORY.removeQuartz(quartz);
			else if(!quartz.isClose())
				FACTORY.close(quartz.getConfig().getId());
				
		}
		
		return ResultMap.create(200, "移除任务完成", "SUCCESS");
	}
	
	private static final void queue(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "任务队列信息", "SUCCESS")._getBeanToMap();
		Set<String> keys = BlockingQueueFactory.getInstance().getQueueKeys();
		Map<String, Integer> sizes = new HashMap<String, Integer>();
		for(String key : keys) {
			int size = BlockingQueueFactory.getInstance().getQueue(key).size();
			sizes.put(key, size);
		}
		
		map.put("Queue", sizes);
		out.write(JSON.toJSONString(map));
	}
	
	private static final void tps(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "TPS列表", "SUCCESS")._getBeanToMap();
		map.put("items", Statistic.getInstance().getPointer());
		out.write(JSON.toJSONString(map));
	}
	
	private static final void quartz(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "任务列表", "SUCCESS")._getBeanToMap();
		Collection<BaseQuartz> startedQuartz = FACTORY.getStartedQuartz();
		List<String> startList = new ArrayList<String>();
		Set<String> startGroupSet = new HashSet<String>();
		for(BaseQuartz quartz : startedQuartz) {
			startList.add(quartz.getConfig().getId());
			startGroupSet.add(quartz.getConfig().getGroup());
		}
		
		Collection<BaseQuartz> stoppingQuartz = FACTORY.getStoppingQuartz();
		List<String> stoppingList = new ArrayList<String>();
		Set<String> stoppingGroupSet = new HashSet<String>();
		for(BaseQuartz quartz : stoppingQuartz) {
			stoppingList.add(quartz.getConfig().getId());
			stoppingGroupSet.add(quartz.getConfig().getGroup());
		}
		
		Collection<BaseQuartz> stoppedQuartz = FACTORY.getStoppedQuratz();
		List<String> stoppedList = new ArrayList<String>();
		Set<String> stoppedGroupSet = new HashSet<String>();
		for(BaseQuartz quartz : stoppedQuartz) {
			stoppedList.add(quartz.getConfig().getId());
			stoppedGroupSet.add(quartz.getConfig().getGroup());
		}
		
		Comparator<String> comp = new Comparator<String>() {
			@Override
			public int compare(String before, String after) {
				return before.compareTo(after);
			}
		};
		
		Collections.sort(startList, comp);
		Collections.sort(stoppingList, comp);
		Collections.sort(stoppedList, comp);
		
		map.put(STARTED, startList);
		map.put(STARTED_GROUP, startGroupSet);
		map.put(STOPPING, stoppingList);
		map.put(STOPPING_GROUP, stoppingGroupSet);
		map.put(STOPPED, stoppedList);
		map.put(STOPPED_GROUP, stoppedGroupSet);
		out.write(JSON.toJSONString(map));
	}
	
	private static final void cmds(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "指令列表", "SUCCESS")._getBeanToMap();
		map.put("commands", Command.commands());
		out.write(JSON.toJSONString(map));
	}
}
