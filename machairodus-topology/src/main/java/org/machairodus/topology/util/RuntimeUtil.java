package org.machairodus.topology.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 系统运行时功能扩展类
 * 
 * @author yanghe
 * @date 2015年6月5日 下午10:59:47 
 *
 */
public class RuntimeUtil {

	public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	public static final String OSNAME = System.getProperty("os.name");
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors() + 1;
	
	/**
	 * 杀死当前系统进行
	 * 
	 * @throws IOException IO异常
	 */
	public static void killProcess() throws IOException {
		if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
			String[] cmds = new String[] { "/bin/sh", "-c", "kill -9 " + PID };
			Runtime.getRuntime().exec(cmds);
			
		} else if (OSNAME.indexOf("Windows") > -1) {
			Runtime.getRuntime().exec("cmd /c taskkill /pid " + PID + " /f ");

		}

	}

	/**
	 * 根据进程号杀死对应的进程
	 * 
	 * @param PID 进程号
	 * @throws IOException IO异常
	 */
	public static void killProcess(String PID) throws IOException {
		if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
			String[] cmds = new String[] { "/bin/sh", "-c", "kill -9 " + PID };
			Runtime.getRuntime().exec(cmds);

		} else if (OSNAME.indexOf("Windows") > -1) {
			Runtime.getRuntime().exec("cmd /c taskkill /pid " + PID + " /f ");

		}

	}

	/**
	 * 根据进程号查询该进程是否存在
	 * 
	 * @param PID 进程号
	 * @return 查询结果
	 * @throws IOException IO异常
	 */
	public static boolean exsitsProcess(String PID) throws IOException {

		if (PID == null || "".equals(PID))
			return false;

		Process process = null;
		Boolean exsits = false;
		String result = null;

		if (OSNAME.indexOf("Mac") > -1 || OSNAME.indexOf("Linux") > -1) {
			String[] cmds = new String[] { "/bin/sh", "-c", "ps -f -p " + PID };
			process = Runtime.getRuntime().exec(cmds);

			InputStream in = process.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(in));

			while ((result = input.readLine()) != null) {
				if (result != null && !"".equals(result) && result.indexOf(PID) > -1) {
					exsits = true;
				}
			}

		} else if (OSNAME.indexOf("Windows") > -1) {
			process = Runtime.getRuntime().exec("cmd /c Wmic Process where ProcessId=\"" + PID + "\" get ExecutablePath ");

			InputStream in = process.getInputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(in));

			while ((result = input.readLine()) != null) {
				if (result != null && !"".equals(result) && result.indexOf("No Instance(s) Available") < 0) {
					exsits = true;
				}
			}

		}

		return exsits;
	}
	
	/**
	 * 判断当前运行的系统是否是Windows
	 * 
	 * @return
	 */
	public static boolean isWindows() {
		if(OSNAME.contains("Windows"))
			return true;
		else 
			return false;
	}
	
	/**
	 * 根据Class获取该Class所在的磁盘路径
	 * 
	 * @param clz 查询的类
	 * @return 返回该类的所在位置
	 */
	public static String getPath(Class<?> clz) {
		String runJarPath = clz.getProtectionDomain().getCodeSource().getLocation().getPath();
		String tmpPath = runJarPath.substring(0, runJarPath.lastIndexOf("/"));
		if(tmpPath.endsWith("/lib"))
			tmpPath = tmpPath.replace("/lib", "");
		
		return tmpPath.substring(isWindows() ? 1 : 0, tmpPath.lastIndexOf("/")) + "/";

	}
	
	/**
	 * 获取运行时中的所有Jar文件
	 * @return List
	 * @throws IOException
	 */
	public static List<JarFile> classPaths() throws IOException {
		String[] classPaths = System.getProperty("java.class.path").split(":");
		if(classPaths.length > 0) {
			List<JarFile> jars = new ArrayList<JarFile>(classPaths.length);
			for(final String classPath : classPaths) {
				if(!classPath.endsWith("jar"))
					continue ;
				
				JarFile jar = new JarFile(new File(classPath));
				jars.add(jar);
				
			}
			
			return jars;
		}
		
		return Collections.emptyList();
	}
	
}
