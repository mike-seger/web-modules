package com.net128.oss.web.lib.filemanager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file-manager/api")
@Tag(name = "File Manager", description = "File system browsing, upload, download, and management")
public class FileManagerController {

	private final FileManagerService service;

	public FileManagerController(FileManagerService service) {
		this.service = service;
	}

	@GetMapping("/list")
	@Operation(summary = "List directory contents",
		description = "Returns the contents of the specified directory including file metadata")
	@ApiResponse(responseCode = "200", description = "Directory listing")
	public DirectoryInfo listDirectory(
		@Parameter(description = "Absolute path of the directory to list")
		@RequestParam(defaultValue = "/") String path) {
		return service.listDirectory(path);
	}

	@GetMapping("/download")
	@Operation(summary = "Download a file",
		description = "Downloads the specified file as an attachment")
	@ApiResponse(responseCode = "200", description = "File content")
	@ApiResponse(responseCode = "400", description = "Path is not a file")
	public ResponseEntity<Resource> download(
		@Parameter(description = "Absolute path of the file to download")
		@RequestParam String path) throws IOException {
		return service.downloadFile(path);
	}

	@GetMapping("/zip")
	@Operation(summary = "Download as ZIP",
		description = "Downloads the specified file or directory as a ZIP archive")
	@ApiResponse(responseCode = "200", description = "ZIP archive")
	public ResponseEntity<Resource> zip(
		@Parameter(description = "Absolute path of the file or directory to zip")
		@RequestParam String path) throws IOException {
		return service.zipDownload(path);
	}

	@GetMapping("/view")
	@Operation(summary = "View a file inline",
		description = "Returns the file content for inline viewing in the browser")
	@ApiResponse(responseCode = "200", description = "File content (inline)")
	@ApiResponse(responseCode = "400", description = "Path is not a file")
	public ResponseEntity<Resource> view(
		@Parameter(description = "Absolute path of the file to view")
		@RequestParam String path) throws IOException {
		return service.viewFile(path);
	}

	@DeleteMapping("/delete")
	@Operation(summary = "Delete a file or directory",
		description = "Recursively deletes the specified file or directory")
	@ApiResponse(responseCode = "204", description = "Successfully deleted")
	public ResponseEntity<Void> delete(
		@Parameter(description = "Absolute path of the file or directory to delete")
		@RequestParam String path) throws IOException {
		service.deleteFileOrDirectory(path);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/upload")
	@Operation(summary = "Upload files to a directory",
		description = "Uploads one or more files to the specified directory")
	@ApiResponse(responseCode = "200", description = "Files uploaded successfully")
	public ResponseEntity<Void> upload(
		@Parameter(description = "Absolute path of the target directory")
		@RequestParam String path,
		@RequestParam("file") List<MultipartFile> files) throws IOException {
		service.uploadFiles(path, files);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/mode")
	@Operation(summary = "Change file permissions",
		description = "Changes read/write/execute permissions. Mode format: +rwx or -rwx")
	@ApiResponse(responseCode = "200", description = "Permissions changed")
	public ResponseEntity<Void> changeMode(
		@Parameter(description = "Absolute path of the file")
		@RequestParam String path,
		@Parameter(description = "Permission mode (e.g. +rw, -x)")
		@RequestParam String mode) {
		service.changeMode(path, mode);
		return ResponseEntity.ok().build();
	}
}

