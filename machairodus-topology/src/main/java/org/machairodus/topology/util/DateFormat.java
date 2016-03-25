package org.machairodus.topology.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yanghe
 * @date 2015年8月19日 上午8:58:41
 */
public class DateFormat {
	private static final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> FORMAT_MAP = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>();
	
	public static final SimpleDateFormat get(String pattern) {
		SimpleDateFormat format;
		ThreadLocal<SimpleDateFormat> formatLocal;
		if((formatLocal = FORMAT_MAP.get(pattern)) == null) {
			formatLocal = new ThreadLocal<SimpleDateFormat>();
			formatLocal.set(format = new SimpleDateFormat(pattern));
			FORMAT_MAP.put(pattern, formatLocal);
		}
		
		if((format = formatLocal.get()) == null) {
			format = new SimpleDateFormat(pattern);
			formatLocal.set(format);
		}
		
		return format;
	}
	
	public static final SimpleDateFormat get(Pattern pattern) {
		Assert.notNull(pattern, "pattern must be not null.");
		return get(pattern.get());
	}
	
	public static final String format(Date date, Pattern pattern) {
		return get(pattern).format(date);
	}
	
	public static final String format(Date date, String pattern) {
		return get(pattern).format(date);
	}
	
	public static final String format(Object date, Pattern pattern) {
		return get(pattern).format(date); 
	}
	
	public static final String format(Object date, String pattern) {
		return get(pattern).format(date); 
	}

	@SuppressWarnings("unchecked")
	public static final <T extends Date> T parse(String date, Pattern pattern) throws ParseException {
		return (T) get(pattern).parse(date);
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends Date> T parse(String date, String pattern) throws ParseException {
		return (T) get(pattern).parse(date);
	}
}
