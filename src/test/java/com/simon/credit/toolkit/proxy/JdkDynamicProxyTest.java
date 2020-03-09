package com.simon.credit.toolkit.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * JDK动态代理测试
 * @author simon 2020-03-10
 */
public class JdkDynamicProxyTest {

	public static void main(String[] args) throws Exception {
		// testJdkDynamicProxy();
		simpleTestJdkDynamicProxy();
	}

	public static void testJdkDynamicProxy() throws Exception {
		Subject realSubject = new RealSubject();
		// 设置生成代理类文件到本地
		System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
		// 1.0 获取代理类的类对象，主要设置相同的ClassLoader去加载目标类实现的接口Subject类
		Class<?> proxyClass = Proxy.getProxyClass(Subject.class.getClassLoader(), new Class[] { Subject.class });
		// 2.0 得到代理类后，就可以通过代理类的处理器句柄来得到构造器
		final Constructor<?> con = proxyClass.getConstructor(InvocationHandler.class);
		// 3.0 获取具体执行方法的句柄处理器，目的通过构造器传入被代理目标类对象，注入到代理类处理器句柄中进行代理调用
		final InvocationHandler handler = new JdkDynamicProxy(realSubject);
		// 4.0 通过构造器创建代理类对象
		Subject subject = (Subject) con.newInstance(handler);
		// 5.0 最后调用方法
		subject.sayHello("proxy");
	}

	public static void simpleTestJdkDynamicProxy() throws Exception {
		// 设置生成代理类文件到本地
		// System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

		// 代理的真实对象
		Subject realSubject = new RealSubject();

        /**
         * JdkDynamicProxy实现了InvocationHandler接口，并能实现方法调用从代理类到委托类的分派转发
         * 其内部通常包含指向委托类实例的引用，用于真正执行分派转发过来的方法调用.
         * 即：要代理哪个真实对象，就将该对象传进去，最后是通过该真实对象来调用其方法
         */
		InvocationHandler handler = new JdkDynamicProxy(realSubject);

		ClassLoader loader = realSubject.getClass().getClassLoader();
		Class<?>[] interfaces = realSubject.getClass().getInterfaces();

		// 该方法用于为指定类装载器、一组接口及调用处理器生成动态代理类实例
		Subject subject = (Subject) Proxy.newProxyInstance(loader, interfaces, handler);

		System.out.println("生成的动态代理对象类型为：" + subject.getClass().getName());

		subject.sayHello("stark");
	}

}
