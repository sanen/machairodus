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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.machairodus.topology.cmd.Executor;
import org.machairodus.topology.util.ContentType;

public class MachairodusServlet extends HttpServlet {
	private static final long serialVersionUID = 8516684399529206854L;
	public static final String QUARTZ_CONFIG = "quartz-config";
	public static final String UTF8 = "UTF-8";
	
	@Override
	public void init() throws ServletException {
		String configPath = this.getInitParameter(QUARTZ_CONFIG);
		new MachairodusPortal(configPath).init();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding(UTF8);
		response.setCharacterEncoding(UTF8);
		response.setContentType(ContentType.APPLICATION_JSON);
		Executor.execute(request, response);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
