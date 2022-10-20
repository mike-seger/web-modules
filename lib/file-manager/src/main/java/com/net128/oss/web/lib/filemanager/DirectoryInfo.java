package com.net128.oss.web.lib.filemanager;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DirectoryInfo {
	final static Logger logger = LoggerFactory.getLogger(DirectoryInfo.class);

	public String name;
	public String path;
	public boolean isWritable;
	public List<FileInfo> files;
	public List<ParentInfoUtil.ParentInfo> parentInfos;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class FileInfo {
		public String parent;
		public String name;
		public Long length;
		public boolean isDirectory;
		public boolean isReadable;
		public boolean isWritable;
		public boolean isExecutable;
		public boolean hasChildren;
		public LocalDateTime modified;
		public LocalDateTime created;

		public FileInfo(String rootPath) {
			name = toUniversalPath(rootPath);
			isDirectory = true;
			isReadable = true;
			hasChildren = true;
		}
		public FileInfo(File file) {
			name = file.getName();
			File parentFile = file.getParentFile();
			if(parentFile!=null) {
				try {
					parent = file.getParentFile().getCanonicalPath();
				} catch (IOException e) {
					parent = file.getParentFile().getAbsolutePath();
				}
				parent = toUniversalPath(parent);
			}
			if(file.isDirectory()) {
				isDirectory = true;
				File [] files = file.listFiles();
				hasChildren = files != null && files.length > 0;
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
	}

	public DirectoryInfo(String path, List<File> files) {
		logger.debug("path: {}", path);
		this.path = path;
		if("\\".equals(File.separator) && path.equals("/")) {
			//4 windoes
			logger.debug("Windows Root: {}", this.files);
			this.files = Arrays.stream(File.listRoots()).map(
				f -> new FileInfo(f.getPath())).collect(Collectors.toList());
		} else {
			logger.debug("Not Windows Root: {}", this.files);
			this.parentInfos = ParentInfoUtil.getParentInfo(path);
			this.files = files.stream().map(FileInfo::new).collect(Collectors.toList());
			this.name = new File(path).getName();
		}
		if(this.name == null || this.name.isEmpty()) {
			this.name = this.path.replaceAll(":[/\\\\]$", "");
		}
		isWritable = Files.isWritable(new File(path).toPath());
	}

	public static String toUniversalPath(String path) {
		String delim = File.separator;
		if(path == null || "/".equals(path)) {
			return path;
		}
		//4 windoes
		return path.replace(delim, "/");
	}

	public static String toPlaformPath(String path) {
		String delim = File.separator;
		if(path == null || "/".equals(path)) {
			return path;
		}
		//4 windoes
		return path.replace("/", delim);
	}
}
