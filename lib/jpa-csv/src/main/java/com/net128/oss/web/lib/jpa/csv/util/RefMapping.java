package com.net128.oss.web.lib.jpa.csv.util;

public interface RefMapping {
	default String toRefMapping(Long id, String label) {
		return label + " (" + id + ")";
	}

	default Long fromRefMapping(String refMapping) {
		return Long.parseLong(refMapping.replaceAll(".*[(][0-9-]*[)]", "$1"));
	}
}
