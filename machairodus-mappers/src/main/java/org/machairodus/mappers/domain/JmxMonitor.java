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

import java.util.Map;

import org.nanoframework.commons.entity.BaseEntity;

public class JmxMonitor extends BaseEntity {
	private static final long serialVersionUID = 743543178402183287L;

	/** Runtime */
	private Long uptime;
	private String name;
	private String pid;
	private Long startTime;
	
	/** ClassLoading */
	private Integer loadedClassCount;
	private Long unloadedClassCount;
	private Long totalLoadedClassCount;
	
	/** Memory */
	private Map<MemoryUsage, Long> heapMemoryUsage;
	private Map<MemoryUsage, Long> nonHeapMemoryUsage;
	
	/** OS */
	private String os;
	private Long freePhysicalMemorySize;
	private Long freeSwapSpaceSize;
	private Long totalPhysicalMemorySize;
	private Long totalSwapSpaceSize;
	private Long commiteedVirtualMemorySize;
	private Integer availableProcessors;
	private Double processCpuLoad;
	private Double systemCpuLoad;
	private Double systemLoadAverage;
	private Long processCpuTime;
	private Double cpuRatio;
	
	/** Thread */
	private Long totalStartedThreadCount;
	private Integer threadCount;
	private Integer daemonThreadCount;
	private Integer peakThreadCount;
	private long[] allThreadIds;
	
	public enum MemoryUsage {
		INIT("init"), USED("used"), COMMITTED("committed"), MAX("max");
		
		private String value;
		private MemoryUsage(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
		
		public MemoryUsage value(String value) {
			switch(value) {
				case "init": 
					return INIT;
				case "used": 
					return USED;
				case "committed": 
					return COMMITTED;
				case "max": 
					return MAX;
				default: 
					throw new IllegalArgumentException("Unknown MemoryUsage value");
			}
		}
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
	
}
