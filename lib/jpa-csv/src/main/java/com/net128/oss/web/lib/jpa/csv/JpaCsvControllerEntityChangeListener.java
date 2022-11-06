package com.net128.oss.web.lib.jpa.csv;

import java.util.List;

public interface JpaCsvControllerEntityChangeListener {
	void changed(List<String> entities);
}
