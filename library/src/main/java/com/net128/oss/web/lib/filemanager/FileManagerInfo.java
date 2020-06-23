package com.net128.oss.web.lib.filemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FileManagerInfo {
	final static Logger logger = LoggerFactory.getLogger(FileManagerInfo.class);
	public FileManagerInfo(String path, List<File> files) {
		this.path = path;
		this.files = files.stream().map(FileInfo::new).collect(Collectors.toList());
		isWritable = Files.isWritable(new File(path).toPath());
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class FileInfo {
		public FileInfo(File file) {
			name = file.getName();
			File parentFile = file.getParentFile();
			if(parentFile!=null) {
				try {
					parent = file.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					parent = file.getParentFile().getAbsolutePath();
				}
			}
			if(file.isDirectory()) {
				isDirectory = true;
			} else {
				length = file.length();
			}
			Path path = file.toPath();
			isReadable = Files.isReadable(path);
			isWritable = Files.isWritable(path);
			isExecutable = Files.isExecutable(path);
			try {
				modified = new Date(file.lastModified()).toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime();
				if(isReadable) {
					FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
					if (creationTime != null) {
						created = creationTime.toInstant()
								.atZone(ZoneId.systemDefault())
								.toLocalDateTime();
					}
				}
			} catch (IOException e) {
				logger.error("Failed to complete file information", e);
			}
		}

		public String parent;
		public String name;
		public Long length;
		public boolean isDirectory;
		public boolean isReadable;
		public boolean isWritable;
		public boolean isExecutable;
		public LocalDateTime modified;
		public LocalDateTime created;
	}

	public String path;
	public boolean isWritable;
	public List<FileInfo> files;
}
