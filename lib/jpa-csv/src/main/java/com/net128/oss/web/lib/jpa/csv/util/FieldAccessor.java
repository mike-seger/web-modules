package com.net128.oss.web.lib.jpa.csv.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FieldAccessor {
	private final static Map<String, Field> fieldCache = new HashMap<>();

	public static Field getField(Class<?> clazz, String fieldName) {
		Field f = fieldCache.get(clazz.getName()+":"+fieldName);
		if(f!=null) return f;
		Class<?> tmpClass = clazz;
		do {
			for ( Field field : tmpClass.getDeclaredFields() ) {
				String candidateName = field.getName();
				if ( ! candidateName.equals(fieldName) ) { continue; }
				field.setAccessible(true);
				fieldCache.put(clazz.getName()+":"+fieldName, field);
				return field;
			}
			tmpClass = tmpClass.getSuperclass();
		} while ( tmpClass != null );
		throw new RuntimeException("Field '" + fieldName + "' not found on class " + clazz);
	}
}
