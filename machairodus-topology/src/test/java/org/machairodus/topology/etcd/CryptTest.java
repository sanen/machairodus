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

import org.junit.Test;
import org.machairodus.topology.scheduler.defaults.monitor.JmxMonitor;
import org.machairodus.topology.util.CryptUtil;

import com.alibaba.fastjson.JSON;

public class CryptTest {

	@Test
	public void encode() {
		System.out.println(CryptUtil.encrypt("topology"));
	}
	
	@Test
	public void decode() {
		final String data = "NkU3ODUzNzI5MEU0NEY4Njg2N0MzQjU1Mzc2QUY1RDBEODNENUIzRjNDNDk0OEZCMTA5MjA1REMwRjNCMTU1MkFBQjJCRjRGQTY0QzQ2RDA5ODhBNTE4NTRDRkUxQkZEMUZEOTYyODU1OTIyMkFGRUM1REJDNDY0ODgzRkRFODRGOTg0NkVBMkU4MjI5NThDN0FFMDA5NDYzOUMyRjc2OTIwQTc1ODgyRjg2OUI5QzlENUJCNjIxMzAwQTIwMTI5MzIyODk2RTQ3NjBFMjYzNDgyMDk5Qzc5Q0Y2ODM4OEQyMjE3M0RERDNGRjMwQTFDNUJBREY2NzU4RkYwQ0NCQUNGOEFBM0U4MUJGMjQwMDNEMjgxMTlEMjFDOUY0MkYxRkM1QzUzQzJGN0RCNjI3OEEyNEIxNUFFNUE1RDRBRDlCNzdGNzU5RTlEMzQxREZBRjg2M0Y2RjJGQkNCRkVGMEFDNDk3NDQyMzQ2NjlDNjc2OEFGN0ZDMkRCNDgxNDNFQUVBMUYxOTNERURFNEYxODFDMkM4QzU0RDVERTVBMjExQzhENEVEQzgxRkI4MzA1QkRFODE5RENENkQ3RDgwODU3OTMyMzI2RTE1OUE5NUY1NUQ5OTYxODY2Q0EwRUMwRjBFQkRDRUVEMDk1RjkyODdBQUE2RTcwM0IzNkMzRUM0QTNEMDRENTUyNEQ2Q0I1RDVEMzQ1MDMxOTZDMEUyQjUwOTdCQzQ2ODA1QUFGN0VFQ0VFQUVEM0IxMEY2NkQyNUMzN0ZDMUJDOTJGMUEzRUJBOTQzM0I1RDlEOEQ1QTE4QjIwNDcyM0M2Qjk5QUEzODU2MjM4MTlGQzNBQjQ5NEY2QTc4RkE4MUVBRUUxRTAxRjc4M0UxRjVGQ0YxRUMwNERGRTI4M0NERUM3QTY2RTY0QjI5QkQ3MkJGRjhDMjRFREI5N0VBQkRFN0VGQTEzMzdBNjExMzc5Q0M5MjNGNDhBQjJENkY5NDMxQzlENzM2M0QzRTlCNTA5RjE3Njg1Q0RGQUE5NzU5QTRDOEJFQzZERjA1NkEyM0ZCRkFBMjY1";
		JmxMonitor monitor = JSON.parseObject(CryptUtil.decrypt(data, "d6b6e5ed62575ebfecefa9f12827276c"), JmxMonitor.class);
		System.out.println(monitor.getCpuRatio());
		System.out.println(monitor.getHeapMemoryUsage());
		System.out.println(monitor.getThreadCount());
	}
}
