package com.net128.oss.web.lib.jpa.csv.util;

import com.net128.oss.web.lib.jpa.csv.Identifiable;
import com.net128.oss.web.lib.jpa.csv.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class JpaMapper {
	private final EntityManager entityManager;
	private final Map<String, Class<?>> entityClassMap;
	private final Map<Class<?>, JpaRepository<?, Long>> entityRepoMap;
	private final List<String> titleRegexes;

	public JpaMapper(EntityManager entityManager, Set<JpaRepository<?, Long>> jpaRepositories,
			 @Value("${com.net128.lib.spring.jpa.csv.util.title-format-regex:}") List<String> titleRegexes) {
		this.entityManager = entityManager;
		this.titleRegexes = titleRegexes;
		entityClassMap = getEntityClassMap();
		entityRepoMap =	jpaRepositories.stream().collect(Collectors.toMap(this::getEntity, j -> j));
	}

	public LinkedHashMap<String, Attribute> getAttributes(String entity) {
		var idFieldName = getIdFieldName(entity);
		var metaAttributes = getMetaAttributes(entity);
		var fieldOrder = Arrays.stream(getEntityClass(entity).getDeclaredFields())
			.map(f -> f.getName().toLowerCase()).collect(Collectors.toList());
		@SuppressWarnings("rawtypes")
		var attributeComparator = (Comparator<javax.persistence.metamodel.Attribute>) (a1, a2) -> {
			var pos1 = fieldOrder.indexOf(a1.getName().toLowerCase());
			var pos2 = fieldOrder.indexOf(a2.getName().toLowerCase());
			return Integer.compare(pos1, pos2);
		};
		var result = metaAttributes.stream()
				.sorted(/*Comparator.comparing(javax.persistence.metamodel.Attribute::getName)*/attributeComparator).collect(
			Collectors.toMap(
				javax.persistence.metamodel.Attribute::getName,
				a -> new Attribute(a, titleRegexes, a.getName().equals(idFieldName)), (v1,v2) -> v1, LinkedHashMap::new));
		if(result.containsKey(idFieldName)) {
			var id = result.remove(idFieldName);
			var old = result;
			result = new LinkedHashMap<>();
			result.put(id.name, id);
			result.putAll(old);
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
			throw new ValidationException("Unable to find entity class for: "+entity);
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
			throw new ValidationException("Unable to get repository for entity class: " + entityClass.getSimpleName());
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
