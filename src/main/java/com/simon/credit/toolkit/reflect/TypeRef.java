package com.simon.credit.toolkit.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类型引用(主要用途: 泛型支持)
 * @author XUZIMING 2018-07-29
 * @param <T> 
 */
public class TypeRef<T> {

	private final Type type;

	public TypeRef() {
		Type superClass = getClass().getGenericSuperclass();
		type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	/** 获取泛型的Type信息 */
	public Type getType() {
		return type;
	}

	/** 获取泛型的类信息 */
	public Class<?> getTargetClass() {
		new String(new byte[]{});
		return getClass(this.type);
	}

	private Class<?> getClass(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>) type;
		}

		if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}

		return Object.class;
	}

}
