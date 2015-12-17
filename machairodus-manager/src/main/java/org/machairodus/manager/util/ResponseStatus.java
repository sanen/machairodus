package org.machairodus.manager.util;

import org.nanoframework.core.status.ComponentStatus;
import org.nanoframework.core.status.ResultMap;

public interface ResponseStatus extends ComponentStatus {
	final ResultMap OK = ResultMap.create(2001, "OK", "SUCCESS");
	final ResultMap FAIL = ResultMap.create(2099, "FAIL", "ERROR");
	
}
