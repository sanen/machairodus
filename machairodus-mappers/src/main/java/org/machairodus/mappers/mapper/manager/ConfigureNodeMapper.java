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
package org.machairodus.mappers.mapper.manager;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.machairodus.mappers.domain.NodeConfig;

public interface ConfigureNodeMapper {
	List<NodeConfig> find(@Param("server") Long[] server, @Param("node") String[] node, @Param("type") Integer[] type,
			@Param("sort") String sort, @Param("order") String order, @Param("offset") Integer offset,
			@Param("limit") Integer limit);

	long findTotal(@Param("server") Long[] server, @Param("node") String[] node, @Param("type") Integer[] type);

	NodeConfig findById(@Param("id") Long id);

	long findExistsById(@Param("id") Long id);

	long insert(NodeConfig nodeConfig);

	long update(NodeConfig nodeConfig);

	long delete(@Param("id") Long id, @Param("modifyUserId") Long modifyUserId);

	List<NodeConfig> findSimple(@Param("param") String param, @Param("type") Integer[] type, @Param("offset") Integer offset, @Param("limit") Integer limit);
	
	long findSimpleTotal(@Param("param") String param, @Param("type") Integer[] type);
	
	List<NodeConfig> findUnAssign(@Param("schedulerId") Long schedulerId);
	
	List<NodeConfig> findAssign(@Param("schedulerId") Long schedulerId);
	
	List<NodeConfig> findAllBalancer();
}
