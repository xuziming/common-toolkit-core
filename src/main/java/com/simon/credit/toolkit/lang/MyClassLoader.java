package com.simon.credit.toolkit.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.simon.credit.toolkit.common.CommonToolkits;

/**
 * 自定义类加载器
 * @author XUZIMING 2019-08-28
 */
public class MyClassLoader extends ClassLoader {

	private String clazzPath;

	public MyClassLoader(String clazzPath) {
		clazzPath = CommonToolkits.castToJavaFilePath(clazzPath);
		if (!clazzPath.endsWith("/")) {
			clazzPath += "/";
		}
		this.clazzPath = clazzPath;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return this.customFindClass(name);
	}

	/**
	 * 自定义查找类
	 * @param name 类名
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected Class<?> customFindClass(String name) throws ClassNotFoundException {
		try {
			String clazzLocation = clazzPath + name.replace('.', '/') + ".class";
			byte[] classBytes = Files.readAllBytes(Paths.get(clazzLocation));
			Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);
			if (clazz == null) {
				throw new ClassNotFoundException(name);
			}
			return clazz;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException(name);
		}
	}

/**********************************************************************************************/
//	private final XClassLoader parent = null;
//
//	private boolean checkName(String name) {
//		if ((name == null) || (name.length() == 0)) {
//			return true;
//		}
//		if ((name.indexOf('/') != -1) || (!VM.allowArraySyntax() && (name.charAt(0) == '['))) {
//			return false;
//		}
//		return true;
//	}
//
//	private Class<?> findBootstrapClassOrNull(String name) {
//		if (!checkName(name)) {
//			return null;
//		}
//
//		return findBootstrapClass(name);
//	}
//
//	private native Class<?> findBootstrapClass(String name);
//
//	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//		synchronized (getClassLoadingLock(name)) {
//			// 首先, 检查是否已经加载过这个类
//			// First, check if the class has already been loaded
//			Class<?> clazz = findLoadedClass(name);
//
//			if (clazz == null) {// 此类未被加载过
//				long t0 = System.nanoTime();
//				try {
//					if (parent != null) {
//						// 父加载器不为空, 调用父加载器的loadClass
//						clazz = parent.loadClass(name, false);
//					} else {
//						// 父加载器为空则, 调用Bootstrap ClassLoader
//						clazz = findBootstrapClassOrNull(name);
//					}
//				} catch (ClassNotFoundException e) {
//					// ClassNotFoundException thrown if class not found
//					// from the non-null parent class loader
//				}
//
//				if (clazz == null) {// 诸多父类加载器均没有找到该类并加载它
//					// If still not found, then invoke findClass in order to find the class.
//					long t1 = System.nanoTime();
//					// 父加载器没有找到, 则调用findClass进行自定义查找类
//					clazz = findClass(name);
//
//					// this is the defining class loader; record the stats
//					sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
//					sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
//					sun.misc.PerfCounter.getFindClasses().increment();
//				}
//			}
//
//			if (resolve) {
//				// 调用resolveClass()
//				resolveClass(clazz);
//			}
//
//			return clazz;
//		}
//	}

}