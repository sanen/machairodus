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
package org.machairodus.mappers.domain;

import java.util.HashMap;
import java.util.Map;

import org.nanoframework.commons.entity.BaseEntity;

public class JmxMonitor extends BaseEntity {
	private static final long serialVersionUID = 743543178402183287L;

	private Long id;
	private String nodeName;
	private String host;
	private Integer port;
	
	/** Runtime */
	private Long uptime = 0L;
	private String name;
	private String hostName;
	private String pid;
	private Long startTime;
	
	/** ClassLoading */
	private Integer loadedClassCount = 0;
	private Long unloadedClassCount = 0L;
	private Long totalLoadedClassCount = 0L;
	
	/** Memory */
	private Map<MemoryUsage, Long> heapMemoryUsage = DEFAULT_MEMORY_STATUS;
	private Map<MemoryUsage, Long> nonHeapMemoryUsage = DEFAULT_MEMORY_STATUS;
	
	/** OS */
	private String os;
	private Long freePhysicalMemorySize = 0L;
	private Long freeSwapSpaceSize = 0L;
	private Long totalPhysicalMemorySize = 0L;
	private Long totalSwapSpaceSize = 0L;
	private Long commiteedVirtualMemorySize = 0L;
	private Integer availableProcessors = 0;
	private Double processCpuLoad = 0D;
	private Double systemCpuLoad = 0D;
	private Double systemLoadAverage = 0D;
	private Long processCpuTime = 0L;
	private Double cpuRatio = 0D;
	private Double totalCpuRatio = 0D;
	
	/** Thread */
	private Long totalStartedThreadCount = 0L;
	private Integer threadCount = 0;
	private Integer daemonThreadCount = 0;
	private Integer peakThreadCount = 0;
	private long[] allThreadIds;
	
	private Long updateTime;
	private JmxMonitorStatus status;
	private Integer flag = 0;
	
	private static final Map<MemoryUsage, Long> DEFAULT_MEMORY_STATUS = new HashMap<MemoryUsage, Long>() {
		private static final long serialVersionUID = -3324580956350564994L; {
		put(MemoryUsage.MAX, 0L);
		put(MemoryUsage.USED, 0L);
		put(MemoryUsage.INIT, 0L);
		put(MemoryUsage.COMMITTED, 0L);
		put(MemoryUsage.FREE, 0L);
		}
	};
	
	public enum MemoryUsage {
		INIT, USED, COMMITTED, MAX, FREE;
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Long getUptime() {
		return uptime;
	}

	public void setUptime(Long uptime) {
		this.uptime = uptime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Integer getLoadedClassCount() {
		return loadedClassCount;
	}

	public void setLoadedClassCount(Integer loadedClassCount) {
		this.loadedClassCount = loadedClassCount;
	}

	public Long getUnloadedClassCount() {
		return unloadedClassCount;
	}

	public void setUnloadedClassCount(Long unloadedClassCount) {
		this.unloadedClassCount = unloadedClassCount;
	}

	public Long getTotalLoadedClassCount() {
		return totalLoadedClassCount;
	}

	public void setTotalLoadedClassCount(Long totalLoadedClassCount) {
		this.totalLoadedClassCount = totalLoadedClassCount;
	}

	public Map<MemoryUsage, Long> getHeapMemoryUsage() {
		return heapMemoryUsage;
	}

	public void setHeapMemoryUsage(Map<MemoryUsage, Long> heapMemoryUsage) {
		this.heapMemoryUsage = heapMemoryUsage;
	}

	public Map<MemoryUsage, Long> getNonHeapMemoryUsage() {
		return nonHeapMemoryUsage;
	}

	public void setNonHeapMemoryUsage(Map<MemoryUsage, Long> nonHeapMemoryUsage) {
		this.nonHeapMemoryUsage = nonHeapMemoryUsage;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public Long getFreePhysicalMemorySize() {
		return freePhysicalMemorySize;
	}

	public void setFreePhysicalMemorySize(Long freePhysicalMemorySize) {
		this.freePhysicalMemorySize = freePhysicalMemorySize;
	}

	public Long getFreeSwapSpaceSize() {
		return freeSwapSpaceSize;
	}

	public void setFreeSwapSpaceSize(Long freeSwapSpaceSize) {
		this.freeSwapSpaceSize = freeSwapSpaceSize;
	}

	public Long getTotalPhysicalMemorySize() {
		return totalPhysicalMemorySize;
	}

	public void setTotalPhysicalMemorySize(Long totalPhysicalMemorySize) {
		this.totalPhysicalMemorySize = totalPhysicalMemorySize;
	}

	public Long getTotalSwapSpaceSize() {
		return totalSwapSpaceSize;
	}

	public void setTotalSwapSpaceSize(Long totalSwapSpaceSize) {
		this.totalSwapSpaceSize = totalSwapSpaceSize;
	}

	public Long getCommiteedVirtualMemorySize() {
		return commiteedVirtualMemorySize;
	}

	public void setCommiteedVirtualMemorySize(Long commiteedVirtualMemorySize) {
		this.commiteedVirtualMemorySize = commiteedVirtualMemorySize;
	}

	public Integer getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(Integer availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public Double getProcessCpuLoad() {
		return processCpuLoad;
	}

	public void setProcessCpuLoad(Double processCpuLoad) {
		this.processCpuLoad = processCpuLoad;
	}

	public Double getSystemCpuLoad() {
		return systemCpuLoad;
	}

	public void setSystemCpuLoad(Double systemCpuLoad) {
		this.systemCpuLoad = systemCpuLoad;
	}

	public Double getSystemLoadAverage() {
		return systemLoadAverage;
	}

	public void setSystemLoadAverage(Double systemLoadAverage) {
		this.systemLoadAverage = systemLoadAverage;
	}

	public Long getProcessCpuTime() {
		return processCpuTime;
	}

	public void setProcessCpuTime(Long processCpuTime) {
		this.processCpuTime = processCpuTime;
	}

	public Double getCpuRatio() {
		return cpuRatio;
	}

	public void setCpuRatio(Double cpuRatio) {
		this.cpuRatio = cpuRatio;
	}

	public Double getTotalCpuRatio() {
		return totalCpuRatio;
	}

	public void setTotalCpuRatio(Double totalCpuRatio) {
		this.totalCpuRatio = totalCpuRatio;
	}

	public Long getTotalStartedThreadCount() {
		return totalStartedThreadCount;
	}

	public void setTotalStartedThreadCount(Long totalStartedThreadCount) {
		this.totalStartedThreadCount = totalStartedThreadCount;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public Integer getDaemonThreadCount() {
		return daemonThreadCount;
	}

	public void setDaemonThreadCount(Integer daemonThreadCount) {
		this.daemonThreadCount = daemonThreadCount;
	}

	public Integer getPeakThreadCount() {
		return peakThreadCount;
	}

	public void setPeakThreadCount(Integer peakThreadCount) {
		this.peakThreadCount = peakThreadCount;
	}

	public long[] getAllThreadIds() {
		return allThreadIds;
	}

	public void setAllThreadIds(long[] allThreadIds) {
		this.allThreadIds = allThreadIds;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public JmxMonitorStatus getStatus() {
		return status;
	}

	public void setStatus(JmxMonitorStatus status) {
		this.status = status;
	}

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}
	
}
