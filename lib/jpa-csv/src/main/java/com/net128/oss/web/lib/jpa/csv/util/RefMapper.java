package com.net128.oss.web.lib.jpa.csv.util;

import java.util.ArrayList;
import java.util.List;

public class RefMapper {
	public static String readRef(Object object, String keyFieldName, List<String> labelFields) throws IllegalAccessException {
		var keyField = FieldAccessor.getField(object.getClass(), keyFieldName);
		var id = keyField.get(object)+"";
		List<String> labelValues = new ArrayList<>();
		for(String f  : labelFields) {
			var labelField = FieldAccessor.getField(object.getClass(), f);
			labelField.setAccessible(true);
			labelValues.add(labelField.get(object)+"");
		}
		return toRefMapping(id, labelValues);
	}

	public static void writeRef(Object object, String keyFieldName, String value) throws IllegalAccessException {
		var keyField = FieldAccessor.getField(object.getClass(), keyFieldName);
		var stringValue = fromRefMapping(value);
		if(keyField.getType().isAssignableFrom(Long.class)) {
			keyField.set(object, Long.parseLong(stringValue));
		} else if(keyField.getType().isAssignableFrom(Integer.class)) {
			keyField.set(object, Integer.parseInt(stringValue));
		} else if(keyField.getType().isAssignableFrom(String.class)) {
			keyField.set(object, stringValue);
		} else throw new IllegalArgumentException("Cannot assign "+stringValue+" to "+object.getClass().getName()+":"+keyFieldName);
	}

	private static String toRefMapping(String id, List<String> labels) {
		return String.format("%s [%s]", String.join(", ", labels), id);
	}

	private static String fromRefMapping(String refMapping) {
		return refMapping.replaceAll(".*\\[([0-9-]+)].*", "$1");
	}
}
