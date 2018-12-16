package com.simon.credit.toolkit.reflect;

import java.lang.reflect.Field;

/**
 * Object工具类
 * @author XUZIMING 2018-12-17
 */
public class PropertyToolkits {

	/**
	 * 根据属性名获取属性值
	 * @param obj
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T getProperty(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);

			// 设置对象的访问权限(跳过Java安全检查), 保证对private的属性的访问
			field.setAccessible(true);

			return (T) field.get(obj);
		} catch (Exception e) {
			return null;
		}
	}

}
