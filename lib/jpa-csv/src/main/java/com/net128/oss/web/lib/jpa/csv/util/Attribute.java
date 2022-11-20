package com.net128.oss.web.lib.jpa.csv.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Time;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Data
@NoArgsConstructor
public class Attribute {
	String name;
	String columnName;
	String title;
	AttributeType type;
	Set<String> enumConstants;
	boolean hidden;
	boolean readOnly;

	boolean isId;

	Attribute(javax.persistence.metamodel.Attribute<?, ?> attribute, List<String> titleRegexes, boolean isId) {
		name = attribute.getName();
		columnName = NameUtil.camel2Snake(name).toLowerCase();
		title = name;
		this.isId = isId;
		for(var regex : titleRegexes) {
			int pos = regex.indexOf(';');
			if(pos >= 0) title = title.replaceAll(regex.substring(0,pos), regex.substring(pos+1));
		}
		var javaType = attribute.getJavaType();
		if (Number.class.isAssignableFrom(javaType) || javaType.isPrimitive()) {
			var typeName = javaType.getName().toLowerCase();
			if(typeName.equals("char"))
				type = AttributeType.String;
			else if (typeName.matches(".*(long|integer|short|byte|int|boolean).*"))
				type = AttributeType.Int;
			else type = AttributeType.Float;
		} else if (javaType.isEnum()) {
			type = AttributeType.Enum;
			enumConstants = Arrays.stream(javaType.getEnumConstants())
				.map(Object::toString).collect(Collectors.toSet());
		} else if (javaType.equals(Instant.class)
				||javaType.equals(ZonedDateTime.class)
				||javaType.equals(OffsetDateTime.class)
				||javaType.equals(LocalDateTime.class)
			) {
			type = AttributeType.DateTime;
		} else if (javaType.equals(Time.class)
				|| javaType.equals(OffsetTime.class)
				|| javaType.equals(LocalTime.class)
			) {
			type = AttributeType.Time;
		} else if (javaType.equals(Date.class)
				|| javaType.equals(LocalDate.class)
			) {
			type = AttributeType.Date;
		} else type = AttributeType.String;
		hidden = Props.isHiddenField(attribute.getJavaMember().getDeclaringClass(), name);
		readOnly = Props.isReadOnlyField(attribute.getJavaMember().getDeclaringClass(), name);
		log.debug(name + ": " + attribute.getJavaType() + " -> " + type);
	}
}
