package com.net128.oss.web.lib.jpa.csv.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public interface ResourceLoader {
	default String loadString(String location) throws IOException {
		try (var is = load(location)) {
			if(is==null) throw new RuntimeException("Failed to load resource from: "+location);
			return new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
		}
	}

	default InputStream load(String location) {
		var is = getClass().getResourceAsStream(location);
		if(is==null) throw new RuntimeException("Failed to load resource from: "+location);
		return is;
	}
}
