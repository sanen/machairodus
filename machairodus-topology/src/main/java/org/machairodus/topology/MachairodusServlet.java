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
package org.machairodus.topology;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.machairodus.topology.cmd.Executor;
import org.machairodus.topology.io.ClassPathResource;
import org.machairodus.topology.io.Resource;
import org.machairodus.topology.util.ContentType;
import org.machairodus.topology.util.MD5Utils;
import org.machairodus.topology.util.ResultMap;
import org.machairodus.topology.util.StringUtils;
import org.machairodus.topology.util.Token;
import org.machairodus.topology.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachairodusServlet extends HttpServlet {
	private static final long serialVersionUID = 8516684399529206854L;
	private static final Logger LOG = LoggerFactory.getLogger(MachairodusServlet.class);
	
	public static final String QUARTZ_CONFIG = "quartz-config";
	public static final String UTF8 = "UTF-8";
	public static final String KEY = "key";
	/** Default Key: 6a9d11e666414b11719ee140ab499b5d */
	protected static String DEFAULT_KEY = MD5Utils.getMD5String(MD5Utils.getMD5String(ZipUtils.gzip("Machairodus Topology Servlet Default KEY")));
	protected String key = DEFAULT_KEY;
	
	@Override
	public void init() throws ServletException {
		String configPath = this.getInitParameter(QUARTZ_CONFIG);
		String key = this.getInitParameter(KEY);
		if(key != null && !StringUtils.isEmpty(key.trim()))
			this.key = key;
		
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
		request.setCharacterEncoding(UTF8);
		response.setCharacterEncoding(UTF8);
		response.setContentType(ContentType.APPLICATION_JSON);
		String key;
		if(StringUtils.isEmpty(key = request.getParameter(KEY))) {
			Writer out = response.getWriter();
			ResultMap resultMap = ResultMap.create(400, "无效的校验KEY", "ERROR");
			out.write(resultMap.toString());
			return ;
		} else if(!key.equals(this.key) && !Token.decode(key)) {
			Writer out = response.getWriter();
			ResultMap resultMap = ResultMap.create(400, "校验KEY错误", "ERROR");
			out.write(resultMap.toString());
			return ;
		}
		
		Executor.execute(request, response);
		
//		Writer out = response.getWriter();
//		out.write(JSON.toJSONString(ResultMap.create(404, "Unknown resources", "WARN")));
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
