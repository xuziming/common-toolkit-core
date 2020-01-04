package com.simon.credit.toolkit;

/**
 * 单元素的枚举类型已经称为实现Singleton的最佳方法
 * @author xuziming 2019-10-15
 */
public class SingletonEnumTest {

	private SingletonEnumTest() {}

	/**
	 * 让JVM来帮我们保证线程安全和单一实例的问题
	 * @author xuziming 2019-10-15
	 */
	static enum SingletonEnum {
		// 创建一个枚举对象，该对象天生为单例
		INST;

		private SingletonEnumTest instance;

		private SingletonEnum() {
			instance = new SingletonEnumTest();
		}

		public SingletonEnumTest getInstance() {
			return instance;
		}
	}

//	public static void main(String[] args) {
//		System.out.println(SingletonEnum.INST.getInstance());
//	}

}