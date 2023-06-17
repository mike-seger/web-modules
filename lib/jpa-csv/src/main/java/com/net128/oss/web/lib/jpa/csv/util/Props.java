package com.net128.oss.web.lib.jpa.csv.util;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.annotation.ElementType.*;

public class Props {
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Hidden{}

    @Target({ ANNOTATION_TYPE, TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface Sortable{}

    @Target({ FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface RefMapping {
        String keyField() default "id";
        String[] labelField();
    }

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReadOnly{}

    @Target({ TYPE_USE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TailFieldOrder{}

    public static boolean isAnnotatedClass(Class<?> clazz, Class<?> annotationClass) {
        try {
            return Arrays.stream(clazz.getDeclaredAnnotations())
                .anyMatch(x -> Objects.equals(
                    x.annotationType().getCanonicalName(),
                    annotationClass.getCanonicalName()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAnnotatedField(Class<?> clazz, Class<?> annotationClass, String field) {
        try {
            return Arrays.stream(clazz.getDeclaredField(field).getDeclaredAnnotations())
                .anyMatch(x -> Objects.equals(
                    x.annotationType().getCanonicalName(),
                    annotationClass.getCanonicalName()));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTailFieldOrder(Class<?> clazz) {
        return isAnnotatedClass(clazz, TailFieldOrder.class);
    }

    public static boolean isSortable(Class<?> clazz) {
        return isAnnotatedClass(clazz, Sortable.class);
    }

    public static boolean isReadOnlyField(Class<?> clazz, String field) {
        return isAnnotatedField(clazz, ReadOnly.class, field);
    }

    public static boolean isHiddenClass(Class<?> clazz) {
        return isAnnotatedClass(clazz, Hidden.class);
    }

    public static boolean isHiddenField(Class<?> clazz, String field) {
        return isAnnotatedField(clazz, Hidden.class, field);
    }
}
