package com.net128.oss.web.app.fileman;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.fileupload.ParameterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeroturnaround.zip.ZipUtil;

@SuppressWarnings({"ResultOfMethodCallIgnored", "SameParameterValue"})
@Configuration
@MultipartConfig
public class Servlet extends HttpServlet {
	final static Logger logger = LoggerFactory.getLogger(Servlet.class);
	//Required to automatically pick up this old-school servlet in a Spring application
	@Bean
	public ServletRegistrationBean<Servlet> fileManagerBean(MultipartConfigElement mce) {
		ServletRegistrationBean<Servlet> bean =
			new ServletRegistrationBean<>(new Servlet(), mapping);
		bean.setMultipartConfig(mce);
		bean.setLoadOnStartup(0);
		return bean;
	}

	private static final String mapping = "/filemanager/api";
	private static final int BUFFER_SIZE = 4096;
	private static final String ENCODING = StandardCharsets.UTF_8.name();
	private final ObjectMapper om=new ObjectMapper().registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("path");
		String type = request.getContentType();
		String mode;
		boolean redirectToApiWeb = true;

		File file;
		if (path==null || !new File(path).exists()) {
			redirectToApiWeb = false;
			file = new File("/");
		} else {
			file=new File(path);
			if (request.getParameter("zip") != null) {
				redirectToApiWeb = false;
				zipFile(file, response);
			} else if (request.getParameter("delete") != null) {
				deleteFileOrDirectory(file);
			} else if ((mode = request.getParameter("mode")) != null) {
				changeMode(file, mode);
			} else if (file.isFile()) {
				redirectToApiWeb = false;
				downloadFile(response, file);
			} else if (file.isDirectory()) {
				if (type != null && type.startsWith("multipart/form-data")) {
					receiveUpload(file, request);
				} else {
					redirectToApiWeb = false;
				}
			} else throw new ServletException("Unknown type of file or folder.");
		}

		FileList fileList=new Directory(file);
		if(redirectToApiWeb) {
			if(!file.isDirectory()) {
				file = file.getParentFile();
			}
			response.sendRedirect("index.html?path="+
				URLEncoder.encode(file.getAbsolutePath(), ENCODING));
		} else {
			api(response, file, fileList);
		}
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class ApiResponse {
		public ApiResponse(String path, List<File> files) {
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

	private void api(HttpServletResponse response, File path, FileList fileList) throws IOException {
		File [] files = fileList.listFiles();
		ApiResponse apiResponse=new ApiResponse(path.getAbsolutePath(), Arrays.asList(files));
		om.writeValue(response.getOutputStream(), apiResponse);
	}

	private void zipFile(File file, HttpServletResponse response) throws IOException {
		File zipFile = File.createTempFile(file.getName()+"-",".zip");
		if(file.isFile())
			ZipUtil.addEntry(zipFile, file.getName(), file);
		else if(file.isDirectory())
			ZipUtil.pack(file, zipFile);
		downloadFile(response, zipFile, permamentName(zipFile.getName()), "application/zip");
	}

	private void deleteFileOrDirectory(File fileOrDirectory) throws IOException {
		if(fileOrDirectory.isFile())
			fileOrDirectory.delete();
		else if(fileOrDirectory.isDirectory()) {
			java.nio.file.Files.walkFileTree(fileOrDirectory.toPath(),new SimpleFileVisitor<Path>() {
				@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					java.nio.file.Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					java.nio.file.Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	private void changeMode(File file, String mode) {
		boolean add = mode.startsWith("+");
		if(mode.indexOf('r')>-1)
			file.setReadable(add);
		if(mode.indexOf('w')>-1)
			file.setWritable(add);
		if(mode.indexOf('x')>-1)
			file.setExecutable(add);
	}

	private void receiveUpload(File file, HttpServletRequest request) throws IOException, ServletException {
		for(Part part:request.getParts()) {
			String name=partFileName(part);
			if(name==null || name.trim().length()==0) //retrieves <input type="file" name="...">, no other (e.g. input) form fields
				continue;
			if(request.getParameter("unzip")==null)
				try(OutputStream output=new FileOutputStream(new File(file,name))) {
					copyStream(part.getInputStream(), output); }
			else ZipUtil.unpack(part.getInputStream(), file);
		}
	}

	protected interface FileList {
		File[] listFiles();
	}

	protected static class Directory implements FileList {
		public final File directory;

		public Directory(File directory) {
			if(!directory.isDirectory()) {
				directory=directory.getParentFile();
			}
			this.directory = directory;
		}

		@Override public String toString() { return directory.getAbsolutePath(); }
		@Override public File[] listFiles() {
			File[] files = directory.listFiles();
			if(files==null || files.length==0) {
				return new File []{};
			}
			Arrays.sort(files, (o1, o2) ->
				o1.getAbsolutePath().compareToIgnoreCase(o2.getAbsolutePath()));
			return files;
		}
	}

	private static void copyStream(InputStream input, OutputStream output) throws IOException {
		int read;
		byte[] buffer = new byte[BUFFER_SIZE];
		while((read=input.read(buffer))>0)
			output.write(buffer, 0, read);
	}
	
	private static void downloadFile(HttpServletResponse response, File file) throws IOException {
		downloadFile(response, file, file.getName());
	}
	private static void downloadFile(HttpServletResponse response, File file, String name) throws IOException {
		String contentType = java.nio.file.Files.probeContentType(file.toPath());
		downloadFile(response, file, name, contentType!=null?contentType:"application/octet-stream");
	}
	private static void downloadFile(HttpServletResponse response, File file, String name, String contentType) throws IOException {
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", "attachment; filename=\""+name+"\"");
		copyStream(new FileInputStream(file),response.getOutputStream());
	}
	
	private static String permamentName(String temporaryName) {
		return temporaryName.replaceAll("-\\d+(?=\\.(?!.*\\.))","");
	}

	private String partFileName(Part part) {
		String header, file = null;
		if((header=part.getHeader("content-disposition"))!=null) {
			String lowerHeader = header.toLowerCase(Locale.ENGLISH);
			if(lowerHeader.startsWith("form-data")||lowerHeader.startsWith("attachment")) {
				ParameterParser parser = new ParameterParser();
				parser.setLowerCaseNames(true);
				Map<String, String> parameters = parser.parse(header, ';');
				if(parameters.containsKey("filename"))
					file = (file=parameters.get("filename"))!=null?file.trim():"";
			}
		}
		return file;
	}
}