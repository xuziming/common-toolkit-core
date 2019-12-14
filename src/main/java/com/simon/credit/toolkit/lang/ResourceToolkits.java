package com.simon.credit.toolkit.lang;

import java.io.InputStream;
import java.net.URL;

/**
 * 资源加载工具类
 * <pre>
 * class.getResource("/") == class.getClassLoader().getResource("")
 * 其实Class.getResource和ClassLoader.getResource本质上是一样的，都是使用ClassLoader.getResource加载资源的，
 * 至于Class.getResource(String path)中path可以'/'开头，是因为在name=resolveName(name);进行了处理，详见JDK源码.
 * </pre>
 * @author XUZIMING 2019-12-14
 */
public class ResourceToolkits {

	/**
	 * 获取资源
	 * <pre>
	 * path不以'/'开头时，默认从此类所在的包下取资源；
	 * path若以'/'开头时，则是从项目的ClassPath根下获取资源，在这里'/'表示ClassPath的根目录.
	 * path不以'/'开头时，可以获取与当前类所在的路径相同的资源文件，而以'/'开头时可以获取ClassPath根下任意路径的资源.
	 * </pre>
	 * @param path 资源路径
	 * @return
	 */
	public static final URL getResourceWithClass(String path) {
		return ResourceToolkits.class.getResource(path);
	}

	/**
	 * 获取资源
	 * <pre>
	 * path不能以'/'开头，path是指类加载器的加载范围，在资源加载的过程中，使用的逐级向上委托的形式加载的，
	 * '/'表示Boot ClassLoader，类加载器中的加载范围，因为这个类加载器是C++实现的，所以加载范围为null.
	 * </pre>
	 * @param path 资源路径
	 * @return
	 */
	public static final URL getResourceWithClassLoader(String path) {
		return ResourceToolkits.class.getClassLoader().getResource(path);
	}

	/**
	 * 获取资源
	 * <pre>
	 * path不以'/'开头时，默认是指所在类的相对路径，从这个相对路径下取资源；
	 * path以'/'开头时，则是从项目的classpath根下获取资源，即相对于classpath根下的绝对路径.
	 * </pre>
	 * @param path 资源路径
	 * @return
	 */
	public static final InputStream getResourceAsStreamWithClass(String path) {
		return ResourceToolkits.class.getResourceAsStream(path);
	}

	/**
	 * 获取资源
	 * <pre>
	 * Class.getClassLoader.getResourceAsStream(String path):默认则是从ClassPath根下获取，path不能以’/'开头，最终是由ClassLoader获取资源.
	 * 如果以'/'开头，则返回的是classLoader加载器Boot ClassLoader的加载范围，即返回的是null，所以不能以'/'开头。
	 * class.getResourceAsStream最终调用是ClassLoader.getResourceAsStream，这两个方法最终调用还是ClassLoader.getResource()方法加载资源.
	 * </pre>
	 * @param path 资源路径
	 * @return
	 */
	public static final InputStream getResourceAsStreamWithClassLoader(String path) {
		return ResourceToolkits.class.getClassLoader().getResourceAsStream(path);
	}

}