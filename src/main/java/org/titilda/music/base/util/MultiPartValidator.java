package org.titilda.music.base.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

public abstract class MultiPartValidator {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface MultipartField {
        String name();

        String[] contentTypes();

        boolean required() default true;

        long maxSize() default -1L;
    }

    public static class InvalidFieldDataException extends Exception {
        private final Field field;

        public InvalidFieldDataException(Field field, String message) {
            super(message);
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }

    public static class InvalidFormDataException extends Exception {
        public InvalidFormDataException(String message) {
            super(message);
        }
    }

    public static class InvalidDataSizeException extends Exception {
        private final Field field;
        private final long maxSize;

        public InvalidDataSizeException(Field field, long maxSize, String message) {
            super(message);
            this.field = field;
            this.maxSize = maxSize;
        }

        public Field getField() {
            return field;
        }

        public long getMaxSize() {
            return maxSize;
        }
    }

    private static void assignField(Field field, Object obj, Part part) {
        try {
            if (field.getType().equals(Part.class)) {
                field.set(obj, part);
            } else if (field.getType().equals(String.class)) {
                field.set(obj, new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } else if (field.getType().equals(Integer.class)) {
                field.set(obj, Integer.parseInt(new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));
            }
        } catch (IOException | IllegalAccessException | NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public MultiPartValidator(HttpServletRequest request) throws InvalidFieldDataException, InvalidFormDataException, InvalidDataSizeException {
        if (!request.getContentType().startsWith("multipart/form-data;")) {
            throw new InvalidFormDataException("Invalid content type");
        }
        try {
            Collection<Part> parts = request.getParts();
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                MultipartField annotation = field.getAnnotation(MultipartField.class);
                if (annotation == null) continue;

                Part part = parts.stream().filter(p -> p.getName().equals(annotation.name())).findFirst().orElse(null);
                if (part == null || part.getSize() == 0) {
                    if (annotation.required()) {
                        throw new InvalidFieldDataException(field, "Missing required field in multipart form");
                    } else {
                        continue;
                    }
                }

                if (annotation.maxSize() > 0 && part.getSize() > annotation.maxSize()) {
                    throw new InvalidDataSizeException(field, annotation.maxSize(), "Field exceeds maximum size");
                }

                String partContentType = part.getContentType();
                String[] allowedContentTypes = annotation.contentTypes();

                // Apparently, the content type is null when dealing with raw text fields, which is represented by an
                // empty array in the @MultipartField annotation. This means that we must check for nullity if the array
                // is empty, for inclusion if it's not.
                if ((allowedContentTypes.length == 0 && partContentType != null) ||
                        (allowedContentTypes.length != 0 && !Arrays.asList(allowedContentTypes).contains(partContentType))) {
                    throw new InvalidFieldDataException(field, "Field has wrong content type");
                }

                assignField(field, this, part);
            }

        } catch (IOException | ServletException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
