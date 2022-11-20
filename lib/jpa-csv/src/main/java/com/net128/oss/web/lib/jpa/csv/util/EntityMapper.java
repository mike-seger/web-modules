package com.net128.oss.web.lib.jpa.csv.util;

import com.net128.oss.web.lib.jpa.csv.Identifiable;
import com.net128.oss.web.lib.jpa.csv.JpaCsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class EntityMapper {
	private final EntityManager entityManager;
	private final Map<String, Class<?>> entityClassMap;
	private final Map<Class<?>, JpaRepository<?, Long>> entityRepoMap;
	private final List<String> titleRegexes;

	public EntityMapper(EntityManager entityManager, Set<JpaRepository<?, Long>> jpaRepositories,
			@Value("${com.net128.oss.web.lib.jpa.csv.util.title-format-regex:}") List<String> titleRegexes) {
		this.entityManager = entityManager;
		this.titleRegexes = titleRegexes;
		entityClassMap = getEntityClassMap();
		entityRepoMap =	jpaRepositories.stream().collect(Collectors.toMap(this::getEntity, j -> j));
	}

	public List<String> getFieldNames(String entity) {
		return getFieldNames(getEntityClass(entity));
	}

	public List<String> getFieldNames(Class<?> clazz) {
		List<String> fields = new ArrayList<>();
		while (clazz != Object.class) {
			var clazzFields = Arrays.stream(clazz.getDeclaredFields())
				.map(Field::getName).collect(Collectors.toList());
			if(Props.isTailFieldOrder(clazz)) fields.addAll(clazzFields);
			else fields.addAll(0, clazzFields);
			clazz = clazz.getSuperclass();
		}
		return fields.stream().distinct().collect(Collectors.toList());
	}

	public LinkedHashMap<String, Attribute> getAttributes(String entity) {
		var idFieldName = getIdFieldName(entity);
		var metaAttributes = getMetaAttributes(entity);
		var clazz = getEntityClass(entity);
		var fieldNames =  getFieldNames(clazz);
		@SuppressWarnings("Convert2MethodRef")
		var metaFieldMap = metaAttributes.stream()
			.collect(Collectors.toMap(a -> a.getName(), a -> a));
		var result = new LinkedHashMap<String, Attribute>();
		fieldNames.forEach(name -> {
			var attr = metaFieldMap.get(name);
			if(attr!=null) {
				metaFieldMap.remove(name);
				result.put(name, new Attribute(attr, titleRegexes, attr.getName().equals(idFieldName)));
			}
		});
		if(metaFieldMap.size()>0) {
			throw new RuntimeException(String.format(
				"Unexpected error: Could not map %s with fields: %s", entity, metaFieldMap.keySet()));
		}
		return result;
	}

	public List<String> getEntities() {
		return entityClassMap.keySet().stream().sorted().collect(Collectors.toList());
	}

	private Set<javax.persistence.metamodel.Attribute<?,?>> getMetaAttributes(String entity) {
		return getEntityMetaData(getEntityClass(entity));
	}

	public Class<?> getEntityClass(String entity) {
		var entityClass = entityClassMap.get(entity);
		if(entityClass == null)
			throw new JpaCsvValidationException("Unable to find entity class for: "+entity);
		return entityClass;
	}

	public String getIdFieldName(String entity) {
		var entityModel = entityManager.getMetamodel().entity(getEntityClass(entity));
		return entityModel.getId(Long.class).getName();
	}

	@SuppressWarnings("unchecked")
	private Set	<javax.persistence.metamodel.Attribute<?, ?>> getEntityMetaData(Class<?> entityClass) {
		return (Set<javax.persistence.metamodel.Attribute<?, ?>>) entityManager.getMetamodel().entity(entityClass).getAttributes();
	}

	private Map<String, Class<?>> getEntityClassMap() {
		return entityManager.getMetamodel().getEntities()
			.stream().collect(Collectors.toMap(
				e -> NameUtil.camel2Snake(e.getName()), javax.persistence.metamodel.Type::getJavaType));
	}

	public JpaRepository<?, Long> getEntityRepository(Class<?> entityClass) {
		var repo = entityRepoMap.get(entityClass);
		if(repo == null) {
			if(Identifiable.class.isAssignableFrom(entityClass)) {
				return entityRepoMap.get(Identifiable.class);
			}
			throw new JpaCsvValidationException("Unable to get repository for entity class: " + entityClass.getSimpleName());
		}
		return repo;
	}

	@SuppressWarnings("rawtypes")
	private Class<?> getEntity(JpaRepository repo) {
		var cz = getGenericType(repo.getClass())[0];
		var jpaClass = getGenericType(getClassFromType(cz));
		return getClassFromType( ((ParameterizedType)jpaClass[0]).getActualTypeArguments()[0]);
	}

	private Type[] getGenericType(Class<?> target) {
		if (target == null) return new Type[0];
		var types = target.getGenericInterfaces();
		if (types.length > 0) return types;
		var type = target.getGenericSuperclass();
		if (type instanceof ParameterizedType) return new Type[] { type };
		return new Type[0];
	}

	@SuppressWarnings("rawtypes")
	private Class<?> getClassFromType(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClassFromType(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			var componentType = ((GenericArrayType) type).getGenericComponentType();
			var componentClass = getClassFromType(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
