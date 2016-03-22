/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.machairodus.topology;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.machairodus.topology.io.ClassPathResource;
import org.machairodus.topology.io.Resource;
import org.machairodus.topology.util.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class MachairodusServlet extends HttpServlet {
	private static final long serialVersionUID = 8516684399529206854L;
	private static final Logger LOG = LoggerFactory.getLogger(MachairodusServlet.class);
	
	public static final String SCHEDULER_CONFIG = "scheduler-config";
	public static final String UTF8 = "UTF-8";
	
	@Override
	public void init() throws ServletException {
		String configPath = this.getInitParameter(SCHEDULER_CONFIG);
		MachairodusPortal portal = new MachairodusPortal(configPath);
		try {
			Resource resource = new ClassPathResource(configPath);
			portal.init(resource.getInputStream());
		} catch(IOException e) {
			LOG.error("Resource must not found config file: " + e.getMessage());
			portal.init(null);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Writer out = response.getWriter();
		out.write(JSON.toJSONString(ResultMap.create(200, "OK", "SUCCESS")));
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
