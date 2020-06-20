package com.net128.oss.web.app.fileman;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.fileupload.ParameterParser;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeroturnaround.zip.ZipUtil;

//@MultipartConfig
@Configuration
@MultipartConfig/*(
	fileSizeThreshold = 1024 * 1024,
	maxFileSize = 1024 * 1024 * 5,
	maxRequestSize = 1024 * 1024 * 5 * 5)*/
public class Servlet extends HttpServlet {
	@Bean
	public ServletRegistrationBean<Servlet> fileManagerBean(MultipartConfigElement mce) {
		ServletRegistrationBean<Servlet> bean =
			new ServletRegistrationBean<>(new Servlet(), "/filemanager/index");
		bean.setMultipartConfig(mce);
		bean.setLoadOnStartup(0);
		return bean;
	}

	private static final int BUFFER_SIZE = 4096;
	private static String GROUP = "ul";
	private static String ITEM = "li";
	private static final String ENCODING = StandardCharsets.UTF_8.name();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Files files = null;
		File file;
		String path = request.getParameter("path");
		String type = request.getContentType();
		String mode;

		file = new File(path);

		if(path==null||!file.exists()) {
			files = new Roots();
		} else if(request.getParameter("zip")!=null) {
			zipFile(file, response);
		} else if(request.getParameter("delete")!=null) {
			deleteFileOrDirectory(file);
		} else if((mode=request.getParameter("mode"))!=null) {
			changeMode(file, mode);
		} else if(file.isFile()) {
			downloadFile(response, file);
		} else if(file.isDirectory()) {
			if(type!=null && type.startsWith("multipart/form-data")) {
				receiveUpload(file, request);
			}
		} else throw new ServletException("Unknown type of file or folder.");

		if(files==null) {
			files = new Directory(file);
		}
		listFiles(files, response);
	}

	private void listFiles(Files files, HttpServletResponse response) throws IOException {
		final PrintWriter writer = response.getWriter();
		writer.println(header());
		writer.println(breadCrumb(files));
		writer.print(tools());
		File [] fileList=files.listFiles();
		fileList = fileList!=null?fileList:new File[]{};
		writer.print("<"+GROUP+" class=\"file-list\">");
		writer.print(fileList(fileList, true));
		writer.print(fileList(fileList, false));
		writer.println("</"+GROUP+">");
		writer.print(footer());
		writer.flush();
	}

	private String header() {
		return "<!DOCTYPE html><html><head>" +
			"<link rel=\"stylesheet\" href=\"css/file-manager.css\">" +
			"</head><body>"
			;
	}

	private String breadCrumb(Files files) throws UnsupportedEncodingException {
		File file=new File(files.toString());
		List<String> parentList=new ArrayList<>();
		parentList.add(file.getName());
		file=file.getParentFile();
		while(file!=null) {
			String name = file.getName();
			if(name.isEmpty()) {
				name = ">";
			}
			String encodedPath = URLEncoder.encode(file.getAbsolutePath(), ENCODING);
			file=file.getParentFile();
			parentList.add(0, String.format("<a href=\"?path=%s\">%s</a>", encodedPath , name));
		}
		return "<p class=\"breadcrumb\"> "+String.join(" / ", parentList)+"</p>";
	}

	private String singleFile(File child) throws UnsupportedEncodingException {
		//hide dot files
		if (child.getName().matches("^[.]+[^.]+.*")) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(ITEM);
		if (child.isDirectory()) {
			sb.append(" class=\"directory\"><a href=\"?path=")
					.append(URLEncoder.encode(child.getAbsolutePath(), ENCODING))
					.append("\"><i class=\"item\" ></i>").append(child.getName()).append("</a>");
		} else {
			sb.append(" class=\"file\"><span class=\"name\"><i class=\"item\"></i>")
					.append(child.getName()).append("</span>");
		}

		sb.append(" <span class=\"item-tool\">")
				.append("<a class=\"download\" href=\"?path=")
				.append(URLEncoder.encode(child.getAbsolutePath(), ENCODING))
				.append(child.isDirectory()?"&zip":"")
				.append("\">&#x2B07;</a>")
				.append("</span>")
				.append("</").append(ITEM).append(">\n");
		return sb.toString();
	}

	private String tools() {
		StringBuilder sb=new StringBuilder();
		sb.append(
			"<form class=\"tools\" method=\"post\" enctype=\"multipart/form-data\">" +
				" <button class=\"tool\" type=\"submit\">&#x2B06;</button>" +
				" <input type=\"file\" name=\"file\" id=\"file\" multiple>" +
				"</form>\n");
		return sb.toString();
	}

	private String footer() {
		return "</body></html>";
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

	private String fileList(File[] fileList, boolean directory) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		for (File child : fileList) {
			if(directory == child.isDirectory()) {
				sb.append(singleFile(child));
			}
		}
		return sb.toString();
	}

	protected interface Files {
		public File[] listFiles();
	}
	protected static class Directory implements Files {
		public final File directory;

		public Directory(File directory) {
			if(!(this.directory=directory).isDirectory())
				throw new IllegalArgumentException();
		}

		@Override public String toString() { return directory.getAbsolutePath(); }
		@Override public File[] listFiles() {
			File[] files = directory.listFiles();
			if(files==null || files.length==0) {
				return new File []{};
			}
			Arrays.sort(files);
			return files;
		}
	}
	protected static class Roots implements Files {
		@Override public String toString() { return "root"; }
		@Override public File[] listFiles() {
			File[] roots = File.listRoots();
			for(int root=0;root<roots.length;root++) {
				final File originalRoot = roots[root];
				roots[root] = new File(roots[root].toURI()) {
					private static final long serialVersionUID = 1l;
					@Override public String getName() {
						String displayName = null;
						try { displayName = FileSystemView.getFileSystemView().getSystemDisplayName(originalRoot); }
						catch(NoClassDefFoundError e) { /* some JRE implementations may not feature the FileSystemView */ }
						return displayName!=null&&!displayName.isEmpty()?displayName:originalRoot.getPath();
					}
				};
			} return roots;
		}
	}

	@SuppressWarnings("unused") private static void checkForPost(HttpServletRequest request) throws ServletException {
		if(!"POST".equals(request.getMethod()))
			throw new ServletException("method must be POST");
	}
	
	@SuppressWarnings("unused") private static byte[] readStream(InputStream input) throws IOException { return readStream(input, -1, true); }
	private static byte[] readStream(InputStream input, int length, boolean readAll) throws IOException {
		byte[] output = {}; int position = 0;
		if(length==-1) length = Integer.MAX_VALUE;
		while(position<length) {
			int bytesToRead;
			if(position>=output.length) { // Only expand when there's no room
				bytesToRead = Math.min(length - position, output.length + 1024);
				if(output.length < position + bytesToRead)
					output = Arrays.copyOf(output, position + bytesToRead);
			} else bytesToRead = output.length - position;
			int bytesRead = input.read(output, position, bytesToRead);
			if(bytesRead<0) {
				if(!readAll||length==Integer.MAX_VALUE) {
					if(output.length!=position)
						output = Arrays.copyOf(output, position);
					break;
				} else throw new EOFException("Detect premature EOF");
			}
			position += bytesRead;
		}
		return output;
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