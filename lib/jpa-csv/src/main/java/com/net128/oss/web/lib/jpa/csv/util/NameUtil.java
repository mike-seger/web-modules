package com.net128.oss.web.lib.jpa.csv.util;

public class NameUtil {
	public static String camel2Snake(String str) {
		return str.replaceAll("([A-Z][a-z])", "_$1")
				.replaceAll("^_", "").toUpperCase();
	}
}
