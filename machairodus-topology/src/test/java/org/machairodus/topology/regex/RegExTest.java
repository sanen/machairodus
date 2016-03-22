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
package org.machairodus.topology.regex;

import org.junit.Test;
import org.machairodus.topology.util.ObjectCompare;

public class RegExTest {

	@Test
	public void test0() {
		System.out.println(ObjectCompare.isInListByRegEx("TestDataLoaderScheduler", "."));
		System.out.println(ObjectCompare.isInListByRegEx("TestDataLoaderScheduler", ".\n"));
		System.out.println(ObjectCompare.isInListByRegEx("TestDataLoaderScheduler", "Test.*?Scheduler"));
		System.out.println(ObjectCompare.isInListByRegEx("TestDataLoaderScheduler", "Test.*?Scheduler2"));
		System.out.println(ObjectCompare.isInListByRegEx("TestDataLoaderScheduler", "TestDataLoaderScheduler"));
	}
}
