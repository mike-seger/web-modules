package com.net128.oss.web.lib.jpa.csv.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

public class PropertySerializerModifier extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (int i = 0; i < beanProperties.size(); i++) {
            var writer = beanProperties.get(i);
            var refMappingAnnotation =
                writer.getMember().getAllAnnotations().get(Props.RefMapping.class);
            if (refMappingAnnotation!=null) {
                beanProperties.set(i, new PropertyWriter(
                    writer, refMappingAnnotation.keyField(), List.of(refMappingAnnotation.labelField())));
            }
        }
        return beanProperties;
    }

    public static class PropertyWriter extends BeanPropertyWriter {
        private final BeanPropertyWriter writer;
        private final String keyField;
        private final List<String> labelFields;

        public PropertyWriter(BeanPropertyWriter writer,
                String keyField, List<String> labelFields) {
            super(writer);
            this.writer = writer;
            this.keyField = keyField;
            this.labelFields = labelFields;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
            gen.writeStringField(writer.getName(), RefMapper.readRef(writer.get(bean), keyField, labelFields));
        }

    }
}