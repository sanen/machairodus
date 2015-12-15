package org.machairodus.topology.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 对象比较处理类
 * 
 * @author yanghe
 * @date 2015年6月5日 下午4:54:24 
 *
 */
public class ObjectCompare {
	
	/**
	 * 判断目标值是否存在与源列表中
	 * 
	 * @param target 源
	 * @param source 目标
	 * @return 返回是否存在结果 true=存在，false=不存在
	 */
	public static final boolean isInList(Object target , Object... source) {
		
		if(target == null)
			return false;
		
		if(source != null && source.length > 0) {
			for(Object src : source) {
				if(target.equals(src))
					return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * 判断目标值是否存在与源列表中
	 * 
	 * @param target 源
	 * @param source 目标
	 * @return 返回是否存在结果 true=存在，false=不存在
	 */
	public static final boolean isInList(Object target , String... source) {
		
		if(target == null)
			return false;
		
		if(source != null && source.length > 0) {
			for(String src : source) {
				if(target.equals(src))
					return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * 正则法比较target是否在regExs内
	 * 
	 * @param target 源
	 * @param regExs 正则列表
	 * @return 返回是否存在结果 true=存在，false=不存在
	 */
	public static final boolean isInListByRegEx(String target, String... regExs) {
		if(target == null || target.isEmpty())
			return false;
		
		if(regExs != null && regExs.length > 0) {
			for(String regEx : regExs) {
				if(Pattern.compile(regEx).matcher(target).find())
					return true;
			}
		}
		
		return false;
	}
	
	public static final boolean isInListByRegEx(String target, Set<String> regExs) {
		if(CollectionUtils.isEmpty(regExs))
			return false;
		
		return isInListByRegEx(target, regExs.toArray(new String[regExs.size()]));
	}
	
	public static final boolean isInEndWiths(String target, String... source) {
		if(target == null) 
			return false;
		
		if(source != null && source.length > 0) {
			for(String suffix : source) {
				if(target.endsWith(suffix))
					return true;
			}
		}
		
		return false;
	}
	
}
