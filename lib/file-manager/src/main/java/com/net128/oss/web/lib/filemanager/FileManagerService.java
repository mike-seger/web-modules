package com.net128.oss.web.lib.filemanager;

import net.sf.jmimemagic.Magic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

@Service
public class FileManagerService {
	private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);

	public DirectoryInfo listDirectory(String path) {
		path = DirectoryInfo.toPlaformPath(path);
		File file;
		if (path == null || !new File(path).exists()) {
			file = new File("/");
			path = "/";
		} else {
			file = new File(path);
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
		}
		File[] files = listSorted(file);
		return new DirectoryInfo(
			"/".equals(path) ? "/" : file.getAbsolutePath(),
			Arrays.asList(files));
	}

	public ResponseEntity<Resource> downloadFile(String path) throws IOException {
		path = DirectoryInfo.toPlaformPath(path);
		File file = resolveExistingFile(path);
		if (!file.isFile()) {
			return ResponseEntity.badRequest().build();
		}
		String contentType = detectContentType(file);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getName() + "\"")
			.contentLength(file.length())
			.body(new FileSystemResource(file));
	}

	public ResponseEntity<Resource> viewFile(String path) throws IOException {
		path = DirectoryInfo.toPlaformPath(path);
		File file = resolveExistingFile(path);
		if (!file.isFile()) {
			return ResponseEntity.badRequest().build();
		}
		String contentType = detectContentType(file);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.contentLength(file.length())
			.body(new FileSystemResource(file));
	}

	public ResponseEntity<Resource> zipDownload(String path) throws IOException {
		path = DirectoryInfo.toPlaformPath(path);
		File file = resolveExistingFile(path);
		File zipFile = File.createTempFile(file.getName() + "-", ".zip");
		zipFile.deleteOnExit();
		if (file.isFile()) {
			ZipUtil.addEntry(zipFile, file.getName(), file);
		} else if (file.isDirectory()) {
			ZipUtil.pack(file, zipFile);
		}
		String downloadName = permanentName(zipFile.getName());
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType("application/zip"))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + downloadName + "\"")
			.contentLength(zipFile.length())
			.body(new FileSystemResource(zipFile));
	}

	public void deleteFileOrDirectory(String path) throws IOException {
		path = DirectoryInfo.toPlaformPath(path);
		File fileOrDirectory = resolveExistingFile(path);
		if (fileOrDirectory.isFile()) {
			fileOrDirectory.delete();
		} else if (fileOrDirectory.isDirectory()) {
			Files.walkFileTree(fileOrDirectory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	public void uploadFiles(String path, List<MultipartFile> files) throws IOException {
		path = DirectoryInfo.toPlaformPath(path);
		File directory = resolveExistingFile(path);
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Target path is not a directory: " + path);
		}
		for (MultipartFile file : files) {
			String name = file.getOriginalFilename();
			if (name == null || name.trim().isEmpty()) {
				continue;
			}
			file.transferTo(new File(directory, name));
		}
	}

	public void changeMode(String path, String mode) {
		path = DirectoryInfo.toPlaformPath(path);
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("File not found: " + path);
		}
		boolean add = mode.startsWith("+");
		if (mode.indexOf('r') > -1) file.setReadable(add);
		if (mode.indexOf('w') > -1) file.setWritable(add);
		if (mode.indexOf('x') > -1) file.setExecutable(add);
	}

	private File resolveExistingFile(String path) {
		if (path == null) {
			throw new IllegalArgumentException("Path must not be null");
		}
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("File not found: " + path);
		}
		return file;
	}

	private File[] listSorted(File directory) {
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			return new File[]{};
		}
		Arrays.sort(files, (a, b) ->
			a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath()));
		return files;
	}

	private String detectContentType(File file) throws IOException {
		String contentType = Files.probeContentType(file.toPath());
		if (contentType == null) {
			try {
				contentType = Magic.getMagicMatch(file, false).getMimeType();
			} catch (Exception e) {
				logger.warn("Could not determine MIME type of: {}", file.getAbsolutePath());
			}
		}
		return contentType != null ? contentType : "application/octet-stream";
	}

	private static String permanentName(String temporaryName) {
		return temporaryName.replaceAll("-\\d+(?=\\.(?!.*\\.))","");
	}
}

