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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.machairodus.topology.quartz.BaseQuartz;
import org.machairodus.topology.quartz.QuartzFactory;
import org.machairodus.topology.util.ResultMap;
import org.machairodus.topology.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class Executor {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	public static final String COMMAND = "command";
	public static final String ID = "id";
	public static final String GROUP = "group";
	
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
			
		if(!QuartzFactory.closed(id) && !QuartzFactory.started(id))
			return ResultMap.create(400, "不存在指定任务ID", "WARN");
		
		if(QuartzFactory.started(id)) 
			return ResultMap.create(400, "指定任务ID已启动", "WARN");
			
		QuartzFactory.start(id);
		return ResultMap.create(200, "启动指定任务: " + id, "SUCCESS");
	}
	
	private static final ResultMap startGroup(String group) throws IOException {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务组", "WARN");
		
		if(!QuartzFactory.hasClosedGroup(group) && !QuartzFactory.hasStartedGroup(group)) 
			return ResultMap.create(400, "不存在任务组: " + group, "WARN");
		
		QuartzFactory.startGroup(group);
		return ResultMap.create(200, "启动指定任务组: " + group, "SUCCESS");
	}
	
	private static final ResultMap startAll() throws IOException {
		if(QuartzFactory.getInstance().getStopQuratz().size() == 0 && QuartzFactory.getInstance().getQuartzs().size() == 0)
			return ResultMap.create(400, "不存在任何任务", "WARN");
		
		QuartzFactory.startAll();
		return ResultMap.create(200, "启动所有任务", "SUCCESS");
	}
	
	private static final ResultMap stop(String id) throws IOException {
		if(StringUtils.isEmpty(id)) 
			return ResultMap.create(400, "无效的任务ID", "WARN");
		
		if(!QuartzFactory.closed(id) && !QuartzFactory.started(id))
			return ResultMap.create(400, "不存在指定任务ID", "WARN");
		
		if(QuartzFactory.closed(id)) 
			return ResultMap.create(400, "指定任务ID已停止", "WARN");
		
		QuartzFactory.getInstance().close(id);
		return ResultMap.create(200, "停止指定任务: " + id, "SUCCESS");
	}
	
	private static final ResultMap stopGroup(String group) throws IOException {
		if(StringUtils.isEmpty(group)) 
			return ResultMap.create(400, "无效的任务GROUP", "WARN");
		
		if(!QuartzFactory.hasClosedGroup(group) && !QuartzFactory.hasStartedGroup(group)) 
			return ResultMap.create(400, "不存在任务组: " + group, "WARN");
		
		QuartzFactory.getInstance().closeGroup(group);
		return ResultMap.create(200, "停止指定任务组: " + group, "SUCCESS");
	}
	
	private static final ResultMap stopAll() {
		if(QuartzFactory.getInstance().getStopQuratz().size() == 0 && QuartzFactory.getInstance().getQuartzs().size() == 0)
			return ResultMap.create(400, "不存在任何任务", "WARN");
		
		QuartzFactory.getInstance().closeAll();
		return ResultMap.create(200, "停止所有", "SUCCESS");
	}
	
	private static final void quartz(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "任务列表", "SUCCESS")._getBeanToMap();
		Collection<BaseQuartz> startedQuartz = QuartzFactory.getInstance().getQuartzs();
		Set<String> startSet = new HashSet<String>();
		Set<String> startGroupSet = new HashSet<String>();
		for(BaseQuartz quartz : startedQuartz) {
			startSet.add(quartz.getConfig().getId());
			startGroupSet.add(quartz.getConfig().getGroup());
		}
		
		Collection<BaseQuartz> stopedQuartz = QuartzFactory.getInstance().getStopQuratz();
		Set<String> stopSet = new HashSet<String>();
		Set<String> stopGroupSet = new HashSet<String>();
		for(BaseQuartz quartz : stopedQuartz) {
			stopSet.add(quartz.getConfig().getId());
			stopGroupSet.add(quartz.getConfig().getGroup());
		}
		
		map.put("started", startSet);
		map.put("started-group", startGroupSet);
		map.put("stoped", stopSet);
		map.put("stoped-group", stopGroupSet);
		out.write(JSON.toJSONString(map));
	}
	
	private static final void cmds(Writer out) throws IOException {
		Map<String, Object> map = ResultMap.create(200, "指令列表", "SUCCESS")._getBeanToMap();
		map.put("commands", Command.commands());
		out.write(JSON.toJSONString(map));
	}
}
