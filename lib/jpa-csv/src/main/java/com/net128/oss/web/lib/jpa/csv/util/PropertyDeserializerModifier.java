package com.net128.oss.web.lib.jpa.csv.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.SneakyThrows;

import java.io.IOException;

/*
https://vkuzel.com/custom-property-de-serialization-not-driven-by-jackson-annotations
 */

public class PropertyDeserializerModifier extends BeanDeserializerModifier {
    @SneakyThrows
    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
        beanDesc.findProperties().forEach(bd -> {
            var refMappingAnnotation = bd.getField().getAnnotated().getAnnotation(Props.RefMapping.class);
            if (refMappingAnnotation != null) {
                JsonDeserializer<?> deserializer =
                    new CustomDeserializer(bd.getRawPrimaryType(), refMappingAnnotation.keyField());
                var property = builder.findProperty(bd.getFullName());
                property = property.withValueDeserializer(deserializer);
                builder.addOrReplaceProperty(property, true);

            } else super.updateBuilder(config, beanDesc, builder);
        });

        return builder;
    }

    public static class CustomDeserializer extends StdDeserializer<Object> {
        private final Class<? extends Object> type;
        private final String keyField;

        private CustomDeserializer(Class<?> type, String keyField) {
            super(type);
            this.type = type;
            this.keyField = keyField;
        }

        @SneakyThrows
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object o = type.getDeclaredConstructor().newInstance();
            RefMapper.writeRef(o, keyField, p.getText());
            return o;
        }
    }
}