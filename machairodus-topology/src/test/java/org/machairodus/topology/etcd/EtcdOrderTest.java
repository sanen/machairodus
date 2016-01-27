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
package org.machairodus.topology.etcd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.machairodus.topology.quartz.QuartzStatus;
import org.machairodus.topology.quartz.defaults.JmxMonitor;
import org.machairodus.topology.quartz.defaults.etcd.EtcdAppInfo;
import org.machairodus.topology.quartz.defaults.etcd.EtcdQuartz;
import org.machairodus.topology.util.CollectionUtils;
import org.machairodus.topology.util.CryptUtil;
import org.machairodus.topology.util.LoaderException;
import org.machairodus.topology.util.PropertiesLoader;
import org.machairodus.topology.util.ResourceUtils;
import org.machairodus.topology.util.StringUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.etcd.client.retry.RetryWithExponentialBackOff;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class EtcdOrderTest {
	private Logger LOG = LoggerFactory.getLogger(EtcdOrderTest.class);
	private EtcdClient etcd = null;
	private Properties properties;
	private String ROOT_RESOURCE;
	
	private void initEtcd() throws LoaderException, FileNotFoundException, IOException {
		properties = PropertiesLoader.load(ResourceUtils.getFile("classpath:quartz-config.properties"));
		String username = properties.getProperty(EtcdQuartz.ETCD_USER);
		String password = CryptUtil.decrypt(properties.getProperty(EtcdQuartz.ETCD_PASSWD));
		String[] uris = properties.getProperty(EtcdQuartz.ETCD_URI, "").split(",");
		ROOT_RESOURCE = properties.getProperty(EtcdQuartz.ETCD_RESOURCE);
		if(!StringUtils.isEmpty(username.trim()) && !StringUtils.isEmpty(password.trim()) && uris.length > 0) {
			List<URI> uriList = new ArrayList<URI>();
			for(String uri : uris) {
				if(StringUtils.isEmpty(uri))
					continue ;
				
				try {
					uriList.add(URI.create(uri));
				} catch(Throwable e) {
					LOG.error("Etcd URI Error: " + e.getMessage());
				}
			}
			
			if(uriList.size() > 0) {
				etcd = new EtcdClient(username, password, uriList.toArray(new URI[uriList.size()]));
				etcd.setRetryHandler(new RetryWithExponentialBackOff(20, 4, -1));
			}
		}
	}
	
	@Test
	public void readSystemID() throws Throwable {
		initEtcd();
		if(etcd != null) {
			String resource;
			int resourceLen = (resource = ROOT_RESOURCE + "/").length();
			EtcdKeysResponse response = etcd.get(resource).sorted().dir().send().get();
			if(response.node != null) {
				List<EtcdNode> nodes = response.node.nodes;
				if(!CollectionUtils.isEmpty(nodes)) {
					for(EtcdNode node : nodes) {
						if(node.ttl == null || node.ttl == 0)
							continue ;
						
						LOG.debug("SystemID: " + node.key.substring(resourceLen));
					}
				}
			}
		}
	}
	
	@Test
	public void readAppInfo() throws Throwable {
		initEtcd();
		if(etcd != null) {
			List<String> systemIds = new ArrayList<String>();
			
			String resource;
			int resourceLen = (resource = ROOT_RESOURCE + "/").length();
			EtcdKeysResponse response = etcd.get(resource).sorted().dir().send().get();
			if(response.node != null) {
				List<EtcdNode> nodes = response.node.nodes;
				if(!CollectionUtils.isEmpty(nodes)) {
					for(EtcdNode node : nodes) {
						if(node.ttl == null || node.ttl == 0)
							continue ;
						
						systemIds.add(node.key.substring(resourceLen));
					}
				}
			}
			
			List<EtcdAppInfo> appInfos = new ArrayList<EtcdAppInfo>();
			TypeReference<EtcdAppInfo> appInfoType = new TypeReference<EtcdAppInfo>() { };
			if(!CollectionUtils.isEmpty(systemIds)) {
				for(String systemId : systemIds) {
					response = etcd.get(resource + systemId + "/App.info").send().get();
					if(response.node != null) {
						appInfos.add(JSON.parseObject(CryptUtil.decrypt(response.node.value, systemId), appInfoType));
					}
				}
			}
			
			if(!CollectionUtils.isEmpty(appInfos)) {
				for(EtcdAppInfo info : appInfos) {
					LOG.debug(info.toString());
				}
			}
		}
	}
	
	@Test
	public void readQuartzList() throws Throwable {
		initEtcd();
		if(etcd != null) {
			List<String> systemIds = new ArrayList<String>();
			
			String resource;
			int resourceLen = (resource = ROOT_RESOURCE + "/").length();
			EtcdKeysResponse response = etcd.get(resource).sorted().dir().send().get();
			if(response.node != null) {
				List<EtcdNode> nodes = response.node.nodes;
				if(!CollectionUtils.isEmpty(nodes)) {
					for(EtcdNode node : nodes) {
						if(node.ttl == null || node.ttl == 0)
							continue ;
						
						systemIds.add(node.key.substring(resourceLen));
					}
				}
			}
			
			List<QuartzStatus> quartzStatus = new ArrayList<QuartzStatus>();
			TypeReference<QuartzStatus> quartzStatusType = new TypeReference<QuartzStatus>() { };
			if(!CollectionUtils.isEmpty(systemIds)) {
				for(String systemId : systemIds) {
					response = etcd.get(resource + systemId + "/Quartz.list").send().get();
					List<EtcdNode> nodes;
					if(response.node != null && !CollectionUtils.isEmpty(nodes = response.node.nodes)) {
						for(EtcdNode node : nodes) {
							quartzStatus.add(JSON.parseObject(CryptUtil.decrypt(node.value, systemId), quartzStatusType));
						}
					}
				}
			}
			
			if(!CollectionUtils.isEmpty(quartzStatus)) {
				for(QuartzStatus status : quartzStatus) {
					LOG.debug(status.toString());
				}
			}
		}
	}
	
	@Test
	public void readJmxStore() throws Throwable {
		initEtcd();
		if(etcd != null) {
			List<String> systemIds = new ArrayList<String>();
			
			String resource;
			int resourceLen = (resource = ROOT_RESOURCE + "/").length();
			EtcdKeysResponse response = etcd.get(resource).sorted().dir().send().get();
			if(response.node != null) {
				List<EtcdNode> nodes = response.node.nodes;
				if(!CollectionUtils.isEmpty(nodes)) {
					for(EtcdNode node : nodes) {
						if(node.ttl == null || node.ttl == 0)
							continue ;
						
						systemIds.add(node.key.substring(resourceLen));
					}
				}
			}
			
			for(int idx = 0; idx < 10; idx ++) {
				List<JmxMonitor> jmxMonitors = new ArrayList<JmxMonitor>();
				TypeReference<JmxMonitor> jmxMonitorType = new TypeReference<JmxMonitor>() { };
				if(!CollectionUtils.isEmpty(systemIds)) {
					for(String systemId : systemIds) {
						response = etcd.get(resource + systemId + "/Jmx.store").send().get();
						if(response.node != null) {
							jmxMonitors.add(JSON.parseObject(CryptUtil.decrypt(response.node.value, systemId), jmxMonitorType));
						}
					}
				}
				
				if(!CollectionUtils.isEmpty(jmxMonitors)) {
					for(JmxMonitor jmx : jmxMonitors) {
						LOG.debug(jmx.toString());
					}
				}
				
				Thread.sleep(1000L);
			}
		}
	}
}
