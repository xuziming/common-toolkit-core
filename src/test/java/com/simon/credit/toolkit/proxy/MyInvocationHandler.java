package com.simon.credit.toolkit.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 调用处理器实现类
 * 每次生成动态代理类对象时都需要指定一个实现了该接口的调用处理器对象
 * @author simon 2020-03-10
 */
public class MyInvocationHandler implements InvocationHandler {

	/**
	 * 反射代理目标类(被代理，解耦的目标类)
	 */
	private Object subject;

	/**
	 * 可以通过构造器动态设置被代理目标类，以便于调用指定方法
	 * @param subject
	 */
	public MyInvocationHandler(Object subject) {
		this.subject = subject;
	}

	/**
	 * 代理过程中的扩展点 
	 * 该方法负责集中处理动态代理类上的所有方法调用。 
	 * 调用处理器根据这三个参数进行预处理或分派到委托类实例上反射执行
	 * @param proxy  代理类实例
	 * @param method 被调用的方法对象
	 * @param args   调用参数
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 在代理真实对象前我们可以添加一些自己的操作
		System.out.println("before invocation ...");

		System.out.println("proxy method: " + method);

		// 当代理对象调用真实对象的方法时，其会自动的跳转到代理对象关联的handler对象的invoke方法来进行调用
		Object returnValue = method.invoke(subject, args);

		// 在代理真实对象后我们也可以添加一些自己的操作
		System.out.println("after invocation ...");

		return returnValue;
	}

}
