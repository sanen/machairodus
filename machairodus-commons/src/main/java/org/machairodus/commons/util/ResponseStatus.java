package org.machairodus.commons.util;

import java.util.Map;

import org.nanoframework.core.status.ComponentStatus;
import org.nanoframework.core.status.ResultMap;

public interface ResponseStatus extends ComponentStatus {
	final ResultMap OK = ResultMap.create(2001, "OK", "SUCCESS");
	final ResultMap FAIL = ResultMap.create(2099, "FAIL", "ERROR");
	
	final Map<String, Object> OK_MAP = OK._getBeanToMap();
	final Map<String, Object> FAIL_MAP = FAIL._getBeanToMap();
}
