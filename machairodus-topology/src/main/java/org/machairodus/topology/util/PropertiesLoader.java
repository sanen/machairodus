package org.machairodus.topology.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @ClassName: PropertiesLoader 
 * @Description: 属性文件操作公有类，负责对属性文件进行读写操作
 * @author yanghe
 * @date 2015年6月5日 上午8:36:50 
 *
 */
public class PropertiesLoader {
	public static Map<String, Properties> PROPERTIES = new HashMap<String, Properties>();
	
	/**
	 * 通过输入流加载属性文件
	 * 
	 * @param input 文件输入流，例如：xxx.class.getResourceAsStream("")
	 * @return 返回加载后的Properties
	 * @throws LoaderException Loader异常
	 * @throws IOException IO异常
	 */
	public static final Properties load(InputStream input) throws LoaderException, IOException {

		if(input == null)
			throw new LoaderException("输入流为空");
		
		Properties prop = new Properties();
		prop.load(input);
		
		return prop;
		
	}
	
	/**
	 * 通过文件加载属性文件
	 * @param file 输入文件
	 * @return 返回加载后的Properties
	 * @throws LoaderException Loader异常
	 * @throws IOException IO异常
	 */
	public static final Properties load(File file) throws LoaderException, IOException {
		if(file == null)
			throw new LoaderException("文件对象为空");
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		
		return prop;
		
	}
	
	/**
	 * 加载属性文件
	 * 
	 * @param contextPath 文件相对路径
	 * @Param stream context属性流
	 * @param loadContext 是否加载context
	 * @throws LoaderException 加载异常
	 * @throws IOException IO异常
	 */
	public static final Properties load(String contextPath, InputStream stream) throws LoaderException, IOException {
		Properties prop = load(stream);
		PROPERTIES.put(contextPath, prop);
		return prop;
	}
	
}
