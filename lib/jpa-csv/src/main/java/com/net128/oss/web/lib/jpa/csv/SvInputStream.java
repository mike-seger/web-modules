package com.net128.oss.web.lib.jpa.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

class SvInputStream extends PushbackInputStream {
	private Boolean isTsv;
	private final int peekSize;

	public SvInputStream(InputStream in, int peekSize) {
		super(in, peekSize);
		this.peekSize = peekSize;
	}

	public boolean isTsv() throws IOException {
		if (isTsv != null) return isTsv;
		byte[] buffer = new byte[peekSize];
		int size = read(buffer);
		unread(buffer, 0, size);
		boolean result = true;
		if (size > 0) {
			int i = -1;
			while (++i < size) if (buffer[i] == '\n') break;
			if (buffer[i] == '\n') {
				String line = new String(buffer, 0, i);
				result = ! (line.startsWith("\"") || line.contains(","));
			}
		}
		isTsv = result;
		return result;
	}
}
