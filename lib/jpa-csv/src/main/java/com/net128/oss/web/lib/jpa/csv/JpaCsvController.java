package com.net128.oss.web.lib.jpa.csv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.net128.oss.web.lib.jpa.csv.util.Attribute;
import com.net128.oss.web.lib.jpa.csv.util.EntityMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/csv")
public class JpaCsvController {
	private final JpaCsvService jpaCsvService;
	private final JpaService jpaService;
	private final String appName;

	private final static String uploadMsg = "Successfully uploaded items: ";
	private final static String uploadFailedMsg = "Failed uploading: ";
	private final static String deleteMsg = "Successfully deleted items: ";
	private final static String deleteFailedMsg = "Failed deleting: ";
	private final String invalidEntityMessage;
	private final Set<JpaCsvControllerEntityChangeListener> jpaCsvChangeListeners = new HashSet<>();

	public JpaCsvController(JpaCsvService jpaCsvService, JpaService jpaService, EntityMapper entityMapper,  @Value("${spring.application.name}") String appName) {
		this.jpaCsvService = jpaCsvService;
		this.jpaService = jpaService;
		this.appName = appName;
		this.invalidEntityMessage = "Invalid input parameters. Valid entities are:\n"+jpaService.getEntities();
	}

	@GetMapping(value="/{entity}.csv", produces = { TEXT_CSV })
	public void getCsv(@PathVariable String entity, HttpServletResponse response) throws IOException {
		getCsv(List.of(entity), false, false, response);
		log.debug("Finished GET: "+entity);
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
		noCache(response);
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
	public ResponseEntity<ModResponse> putCsv(
		@RequestParam("entity")
		String entity,
		@Schema( allowableValues = {"true", "false"}, description = "true: TSV output, false: CSV output" )
		@RequestParam(name="tabSeparated", required = false)
		Boolean tabSeparated,
		@RequestParam(name="deleteAll", required = false)
		Boolean deleteAll,
		@RequestParam(name="deleteIds", required = false)
		List<Long> deleteIds,
		@RequestBody(required = false)
		String csvData
	) {
		ResponseEntity<ModResponse> response = ResponseEntity.status(HttpStatus.OK).body(new ModResponse());
		if(deleteIds!=null && deleteIds.size()>0) response = deleteIds(entity, deleteIds);
		if(response.getStatusCode().value()!=200 || csvData==null) return response;
		try (InputStream is = new ByteArrayInputStream(csvData.getBytes())) {
			var count = jpaCsvService.readCsv(is, entity, tabSeparated, deleteAll);
			response = ResponseEntity.status(HttpStatus.OK).body(
				new ModResponse(HttpStatus.OK.value(),
						deleteIds==null?null:deleteIds.size(),count,
						uploadMsg+entity+" (count="+count+")"));
			callJpaCsvControllerEntityChangeListeners(List.of(entity));
		} catch(Exception e) {
			response = failedResponseEntity(entity, e);
		}
		log.debug("Finished PUT: "+entity);
		return response;
	}

	@PostMapping(consumes = { TEXT_TSV, TEXT_CSV, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<ModResponse> postCsv(
		@Schema( allowableValues = {"true", "false"}, description = "true: TSV output, false: CSV output" )
		@RequestParam(name="tabSeparated", required = false)
		Boolean tabSeparated,
		@RequestParam("file")
		MultipartFile file
	) {
		var fileName="";
		ResponseEntity<ModResponse> response;
		try (InputStream is = file.getInputStream()) {
			if(file.getOriginalFilename() == null)
				throw new IllegalArgumentException("file.originalFileName must not be empty");
			fileName = file.getOriginalFilename();
			var entity = fileName.replaceAll("[.].*", "");
			jpaCsvService.readCsv(is, entity, tabSeparated, true);
			response = ResponseEntity.status(HttpStatus.OK).body(new ModResponse(
					HttpStatus.OK.value(), null,null, uploadMsg+fileName));
			callJpaCsvControllerEntityChangeListeners(List.of(entity));
		} catch(Exception e) {
			response = failedResponseEntity(fileName, e);
		}
		return response;
	}

	@DeleteMapping
	public ResponseEntity<ModResponse> deleteIds(
		@RequestParam("entity")
		String entity,
		@RequestParam(name="ids")
		List<Long> ids) {
		var status = HttpStatus.OK;
		String message;
		try {
			var n = jpaService.deleteIds(entity, ids);
			message = deleteMsg+n;
			callJpaCsvControllerEntityChangeListeners(List.of(entity));
		} catch(Exception e) {
			message = deleteFailedMsg+"\n"+e.getMessage();
			if(e instanceof ValidationException ||
				e instanceof RuntimeJsonMappingException ||
				e instanceof EmptyResultDataAccessException)
				status = HttpStatus.BAD_REQUEST;
			else status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		log.debug("Finished DELETE: "+entity);
		return ResponseEntity.status(status).body(new ModResponse(
			status.value(), status.value()==200?ids.size():0,null, message));
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
			jpaCsvService.writeCsv(os, realEntities.get(0), tabSeparated);
		} else {
			var fileName = appName + "-data-export-" + timestampNow() + ".zip";
			response.setContentType(APPLICATION_ZIP);
			response.addHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
			jpaCsvService.writeAllCsvZipped(os, realEntities, tabSeparated);
		}
	}

	private ResponseEntity<ModResponse> failedResponseEntity(String entity, Throwable t) {
		var message = uploadFailedMsg+entity;
		var status = HttpStatus.BAD_REQUEST;
		if(t instanceof ValidationException || t instanceof RuntimeJsonMappingException) {
			message += "\n" + t.getMessage();
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			log.error(message, t);
		}
		return ResponseEntity.status(status).body(new ModResponse(status.value(), null,null, message));
	}

	private void writeError(HttpServletResponse response, OutputStream os, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		var osw = new OutputStreamWriter(os);
		osw.write(message);
		osw.flush();
	}

	private void noCache(HttpServletResponse response) {
		response.addHeader("Cache-Control", "no-store");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");
	}

	@SuppressWarnings("unused")
	public void addJpaCsvChangeListener(JpaCsvControllerEntityChangeListener jpaCsvChangeListener) {
		jpaCsvChangeListeners.add(jpaCsvChangeListener);
	}

	@SuppressWarnings("unused")
	public void removeJpaCsvChangeListener(JpaCsvControllerEntityChangeListener jpaCsvChangeListener) {
		jpaCsvChangeListeners.remove(jpaCsvChangeListener);
	}

	private void callJpaCsvControllerEntityChangeListeners(List<String> entities) {
		jpaCsvChangeListeners.forEach(l -> l.changed(entities));
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class ModResponse {
		private int status=HttpStatus.OK.value();
		private Integer deleted;
		private Integer saved;
		private String message;
	}

	private String timestampNow() { return isoTimeStampNow()
		.replaceAll("\\..*", "").replaceAll("[^0-9]", ""); }
	private String isoTimeStampNow() { return isoTimeStamp(Instant.now());}
	private String isoTimeStamp(Instant ts) { return ts.toString(); }

	private final static String APPLICATION_ZIP = "application/zip";
	private final static String TEXT_TSV = "text/tab-separated-values";
	private final static String TEXT_CSV = "text/csv";
}
