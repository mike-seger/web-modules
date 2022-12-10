package com.net128.oss.web.lib.jpa.csv;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.net128.oss.web.lib.jpa.csv.util.EntityMapper;
import com.net128.oss.web.lib.jpa.csv.util.PropertyDeserializerModifier;
import com.net128.oss.web.lib.jpa.csv.util.PropertySerializerModifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@ComponentScan(basePackageClasses = JpaCsvService.class)
public class JpaCsvService {
	private final CsvMapper readerMapper;
	private final CsvSchema readerSchema ;
	private final CsvSchema readerTsvSchema;
	private final EntityMapper entityMapper;
	private final List<SaveListener> preSaveListeners;
	private final List<SaveListener> postSaveListeners;

	public JpaCsvService(EntityMapper entityMapper) {
		this.entityMapper = entityMapper;
		readerMapper = csvMapper()
			.enable(CsvParser.Feature.TRIM_SPACES)
			.enable(CsvParser.Feature.SKIP_EMPTY_LINES)
			.enable(CsvParser.Feature.EMPTY_STRING_AS_NULL)
		;
		readerMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		readerSchema = CsvSchema.emptySchema()
			.withHeader().withLineSeparator(new String(CsvSchema.DEFAULT_LINEFEED));
		readerTsvSchema = readerSchema.withColumnSeparator('\t').withoutQuoteChar();
		preSaveListeners = new ArrayList<>();
		postSaveListeners = new ArrayList<>();
	}

	public void writeAllCsvZipped(OutputStream os,
		  List<String> entities, Boolean tabSeparated) throws IOException {
		try (var zos = new ZipOutputStream(os)) {
			for(var entity : entities)
				writeCsvZipEntry(zos, entity, tabSeparated);
			zos.flush();
			zos.finish();
		}
	}

	private void writeCsvZipEntry(ZipOutputStream zipOutputStream, String entity, Boolean tabSeparated) throws IOException {
		if(tabSeparated==null) tabSeparated=true;
		try (var bos = new ByteArrayInputStream(writeCsv(entity, tabSeparated).getBytes())) {
			var fileName = entity + (tabSeparated?".tsv":".csv");
			zipOutputStream.putNextEntry(new ZipEntry(fileName));
			IOUtils.copy(bos, zipOutputStream);
		}
		zipOutputStream.closeEntry();
	}

	public String writeCsv(String entity, Boolean tabSeparated) throws IOException {
		try (var bos = new ByteArrayOutputStream()) {
			writeCsv(bos, entity, tabSeparated);
			return bos.toString();
		}
	}

	public void writeCsv(OutputStream os, String entity, Boolean tabSeparated) throws IOException {
		if(tabSeparated==null) tabSeparated=true;
		var entityClass = entityMapper.getEntityClass(entity);
		var jpaRepository = entityMapper.getEntityRepository(entityClass);
		var jsonFactory = new CsvFactory()
			.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)	;
		var writerMapper = csvMapper().schemaFor(entityClass)
			.withLineSeparator("\n").withHeader()
			.withColumnReordering(true)
			.sortedBy(entityMapper.getFieldNames(entity).toArray(new String[0]))
			;

		if(tabSeparated) writerMapper = writerMapper.withColumnSeparator('\t').withoutQuoteChar();
		try (var cos = os) {
			try (var writer = configureMapper(new ObjectMapper(jsonFactory))
				.writer(writerMapper).writeValues(cos)) {
				var count = new AtomicInteger();
				var errors = new ArrayList<>();
				var items = jpaRepository.findAll();
				items.forEach(e -> {
					try {
						writer.write(e);
						count.getAndIncrement();
						if(count.get() < 3) log.debug(e.toString());
					} catch (Exception ex) {
						if (errors.isEmpty()) {
							log.error("Failed to write entity", ex);
						}
						errors.add(ex.getMessage());
					}
				});

				if (errors.size() == 0) {
					log.info("Loaded {} items of {}", count.get(), entityClass.getSimpleName());
				} else {
					var message = String.format("Failed to load %d items", items.size() * count.get());
					throw new JpaCsvValidationException(message);
				}
				writer.flush();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> int readCsv(InputStream inputStream, String entityName,
			Boolean tabSeparated, Boolean deleteAll) throws IOException {
		if(tabSeparated==null) {
			SvInputStream svInputStream = new SvInputStream(inputStream, 2048);
			tabSeparated = svInputStream.isTsv();
			inputStream = svInputStream;
		}
		Class<T> entityClass = (Class<T>) entityMapper.getEntityClass(entityName);
		JpaRepository<T, Long> jpaRepository =
			(JpaRepository<T, Long>) entityMapper.getEntityRepository(entityClass);
		if(Boolean.TRUE.equals(deleteAll)) jpaRepository.deleteAll();
		return saveEntities(inputStream, jpaRepository, entityClass, tabSeparated);
	}

	public interface SaveListener {
		<T> void listen(Class<T> entityClass, JpaRepository<T, Long> jpaRepository, List<T> entities);
	}

	@SuppressWarnings("unused")
	public void addPreSaveListener(SaveListener preSaveListener) {
		preSaveListeners.add(preSaveListener);
	}

	@SuppressWarnings("unused")
	public void addPostSaveListener(SaveListener postSaveListener) {
		postSaveListeners.add(postSaveListener);
	}

	private <T> int saveEntities(InputStream inputStream, JpaRepository<T, Long> jpaRepository,
			 Class<T> entityClass, boolean tabSeparated) throws IOException {
		try (InputStream is = inputStream) {
			var reader = genericCsvReader(entityClass, is, tabSeparated);
			var count = 0;
			var readErrors = new ArrayList<String>();
			var items = new ArrayList<T>();
			while (reader.hasNext()) {
				T item = null;
				try {
					item = reader.next();
					items.add(item);
					count++;
				} catch (Exception e) {
					readErrors.add("Data line: " + count + ", item: " + item + ", error: " + e.getMessage());
				}
			}

			if(readErrors.size()>0) {
				throw new JpaCsvValidationException("Error reading CSV data: " + String.join("\n", readErrors));
			}

			for(var l : preSaveListeners) {
				l.listen(entityClass, jpaRepository, items);
			}

			count = 0;
			var persistErrors = new ArrayList<String>();
			for(T item : items) {
				try {
					jpaRepository.save(item);
					count++;
				} catch(Exception e) {
					if(e instanceof javax.validation.ValidationException || (e.getMessage()!=null && e.getMessage().toLowerCase(Locale.ROOT).contains("constraint"))) {
						persistErrors.add(String.format(
							"On data line %d:\nAttempted to save: %s.\nEncountered error: %s",
							count+1, item, e.getMessage()));
					} else {
						throw e;
					}
				}
			}

			if(persistErrors.size()>0) {
				throw new JpaCsvValidationException("Error persisting CSV data: " + String.join("\n", persistErrors));
			}

			jpaRepository.flush();

			for(var l : postSaveListeners) {
				l.listen(entityClass, jpaRepository, items);
			}

			log.info("Saved {} items of {}", count, entityClass.getSimpleName());
			return count;
		}
	}

	private <T> MappingIterator<T> genericCsvReader(Class<T> clazz,
			InputStream inputStream, boolean tabSeparated) throws IOException {
		MappingIterator<T> result = readerMapper.readerFor(clazz)
			.with(tabSeparated?readerTsvSchema:readerSchema)
			.readValues(inputStream);
		if(result == null) {
			throw new IOException("Cannot find csvReader for: "+clazz.getName());
		}
		return result;
	}

	private ObjectMapper configureMapper(ObjectMapper mapper) {
		final DeserializationConfig originalConfig = mapper.getDeserializationConfig();
		final DeserializationConfig newConfig = originalConfig

		//.with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
			.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
		mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
		mapper.setConfig(newConfig);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

		return customPropertyDeSerialization(mapper)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.findAndRegisterModules()
			.registerModule(new JavaTimeModule())
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
	}

	private CsvMapper csvMapper() {
		CsvMapper csvMapper = new CsvMapper();
		csvMapper = (CsvMapper)configureMapper(csvMapper);
		csvMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		return customPropertyDeSerialization(csvMapper);
	}

	private <T extends ObjectMapper> T customPropertyDeSerialization(T mapper) {
		SimpleModule simpleModule = new SimpleModule()
			.setSerializerModifier(new PropertySerializerModifier())
			.setDeserializerModifier(new PropertyDeserializerModifier());
		mapper.registerModule(simpleModule);
		return mapper;
	}
}
