package com.simon.credit.toolkit.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 泛型类型信息获取工具
 * @author XUZIMING 2018-08-08
 */
public class TypeToolkits {

	/**
	 * 获取泛型类型
	 * @param genericityInstance 泛型实例
	 * @return
	 */
	public static Type getGenericityType(Object genericityInstance) {
		Class<?> clazz = genericityInstance.getClass();
		Type[] interfaces = clazz.getGenericInterfaces();
		if (interfaces.length != 0) {
			return ((ParameterizedType) interfaces[0]).getActualTypeArguments()[0];
		}

		Type superclass = clazz.getGenericSuperclass();
		return ((ParameterizedType) superclass).getActualTypeArguments()[0];
	}

	/**
	 * 获取泛型类型名称
	 * @param type
	 * @return
	 */
	public static String getGenericityTypeName(Type type) {
		if (type.getClass() == Class.class) {
			return ((Class<?>) type).getName();
		}

		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).toString();
		}

		if (type instanceof TypeVariable) {
			Type boundType = ((TypeVariable<?>) type).getBounds()[0];
			return ((Class<?>) boundType).getName();
		}

		return Object.class.getName();
	}

	/**
	 * 获取泛型Class信息
	 * @param type
	 * @return
	 */
	public static Class<?> getGenericityClass(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>) type;
		}

		if (type instanceof ParameterizedType) {
			return getGenericityClass(((ParameterizedType) type).getRawType());
		}

		if (type instanceof TypeVariable) {
			Type boundType = ((TypeVariable<?>) type).getBounds()[0];
			return (Class<?>) boundType;
		}

		return Object.class;
	}

}