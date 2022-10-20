package com.net128.oss.web.lib.jpa.csv;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.net128.oss.web.lib.jpa.csv.util.Attribute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/csv")
public class CsvController {
	private final CsvService csvService;
	private final JpaService jpaService;
	private final String appName;

	private final static String uploadMsg = "Successfully uploaded items: ";
	private final static String uploadFailedMsg = "Failed uploading: ";
	private final static String deleteMsg = "Successfully deleted items: ";
	private final static String deleteFailedMsg = "Failed deleting: ";
	private final String invalidEntityMessage;

	public CsvController(CsvService csvService, JpaService jpaService, @Value("${spring.application.name}") String appName) {
		this.csvService = csvService;
		this.jpaService = jpaService;
		this.appName = appName;
		this.invalidEntityMessage = "Invalid input parameters. Valid entities are:\n"+jpaService.getEntities();
	}

	@GetMapping(value="/{entity}.csv", produces = { TEXT_CSV })
	public void getCsv(@PathVariable String entity, HttpServletResponse response) throws IOException {
		getCsv(List.of(entity), false, false, response);
	}

	@GetMapping(produces = { APPLICATION_ZIP, TEXT_TSV, TEXT_CSV, MediaType.TEXT_PLAIN_VALUE })
	public void getCsv(
		@RequestParam(name="entity", required = false)
		List<String> entities,
		@Schema( description = "true: TSV output, false: CSV output", required = true, defaultValue = "true" )
		@RequestParam(name="tabSeparated", required = false, defaultValue = "true")
		Boolean tabSeparated,
		@RequestParam(name="zippedSingleTable", required = false, defaultValue = "false")
		Boolean zippedSingleTable,
		HttpServletResponse response
	) throws IOException {
		try (OutputStream os = response.getOutputStream()) {
			try {
				if (CollectionUtils.isEmpty(entities)) {
					writeError(response, os, HttpServletResponse.SC_BAD_REQUEST, invalidEntityMessage);
				} else {
					response.setStatus(HttpServletResponse.SC_OK);
					writeCsv(os, entities, tabSeparated, zippedSingleTable, response);
				}
			}  catch(Exception e) {
				if(e instanceof ValidationException)
					writeError(response, os, HttpServletResponse.SC_BAD_REQUEST, invalidEntityMessage);
				else {
					writeError(response, os, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
					log.error("Error getting entities: {}", entities, e);
				}
			}
			os.flush();
		}
	}

	@PutMapping(consumes = { TEXT_TSV, TEXT_CSV, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> putCsv(
		@RequestParam("entity")
		String entity,
		@Schema( allowableValues = {"true", "false"}, description = "true: TSV output, false: CSV output" )
		@RequestParam(name="tabSeparated", required = false)
		Boolean tabSeparated,
		@RequestParam(name="deleteAll", required = false)
		Boolean deleteAll,
		@RequestBody
		String csvData
	) {
		ResponseEntity<String> response;
		try (InputStream is = new ByteArrayInputStream(csvData.getBytes())) {
			var count = csvService.readCsv(is, entity, tabSeparated, deleteAll);
			response = ResponseEntity.status(HttpStatus.OK).body(uploadMsg+entity+" (count="+count+")");
		} catch(Exception e) {
			response = failedResponseEntity(entity, e);
		}
		return response;
	}

	@PostMapping(consumes = { TEXT_TSV, TEXT_CSV, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> postCsv(
		@Schema( allowableValues = {"true", "false"}, description = "true: TSV output, false: CSV output" )
		@RequestParam(name="tabSeparated", required = false)
		Boolean tabSeparated,
		@RequestParam("file")
		MultipartFile file
	) {
		var fileName="";
		ResponseEntity<String> response;
		try (InputStream is = file.getInputStream()) {
			if(file.getOriginalFilename() == null)
				throw new IllegalArgumentException("file.originalFileName must not be empty");
			fileName = file.getOriginalFilename();
			var entity = fileName.replaceAll("[.].*", "");
			csvService.readCsv(is, entity, tabSeparated, true);
			response = ResponseEntity.status(HttpStatus.OK).body(uploadMsg+fileName);
		} catch(Exception e) {
			response = failedResponseEntity(fileName, e);
		}
		return response;
	}

	@DeleteMapping
	public ResponseEntity<String> deleteIds(
		@RequestParam("entity")
		String entity,
		@RequestParam(name="id")
		List<Long> ids) {
		var status = HttpStatus.OK;
		String message;
		try {
			var n = jpaService.deleteIds(entity, ids);
			message = deleteMsg+n;
		} catch(Exception e) {
			message = deleteFailedMsg+"\n"+e.getMessage();
			if(e instanceof ValidationException || e instanceof RuntimeJsonMappingException)
				status = HttpStatus.BAD_REQUEST;
			else status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return ResponseEntity.status(status).body(message);
	}

	@GetMapping(path = "/entities")
	public List<String> getEntities() {
		return jpaService.getEntities();
	}

	@GetMapping(path = "/attributes")
	public Map<String, Attribute> getAttributes(@RequestParam("entity") String entity) {
		return jpaService.getAttributes(entity);
	}

	@GetMapping(path = "/configuration")
	public JpaService.Configuration getConfiguration() {
		return jpaService.getConfiguration();
	}

	private void writeCsv(OutputStream os,
		List<String> entities, boolean tabSeparated,
		boolean zippedSingleTable, HttpServletResponse response) throws IOException {
		var realEntities = entities.contains("*")? getEntities():entities;
		if(!zippedSingleTable && realEntities.size() == 1) {
			response.setContentType(tabSeparated?TEXT_TSV:TEXT_CSV);
			csvService.writeCsv(os, realEntities.get(0), tabSeparated);
		} else {
			var fileName = appName + "-data-export-" + timestampNow() + ".zip";
			response.setContentType(APPLICATION_ZIP);
			response.addHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
			csvService.writeAllCsvZipped(os, realEntities, tabSeparated);
		}
	}

	private ResponseEntity<String> failedResponseEntity(String entity, Throwable t) {
		var message = uploadFailedMsg+entity;
		var status = HttpStatus.BAD_REQUEST;
		if(t instanceof ValidationException || t instanceof RuntimeJsonMappingException) {
			message += "\n" + t.getMessage();
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			log.error(message, t);
		}
		return ResponseEntity.status(status).body(message);
	}

	private void writeError(HttpServletResponse response, OutputStream os, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		var osw = new OutputStreamWriter(os);
		osw.write(message);
		osw.flush();
	}

	private String timestampNow() { return isoTimeStampNow()
		.replaceAll("\\..*", "").replaceAll("[^0-9]", ""); }
	private String isoTimeStampNow() { return isoTimeStamp(Instant.now());}
	private String isoTimeStamp(Instant ts) { return ts.toString(); }

	private final static String APPLICATION_ZIP = "application/zip";
	private final static String TEXT_TSV = "text/tab-separated-values";
	private final static String TEXT_CSV = "text/csv";
}
