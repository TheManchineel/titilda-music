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
    protected @interface RequiredField {
        String name();
        String[] contentTypes();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface OptionalField {
        String name();
        String[] contentTypes();
    }

    private static boolean hasSameMime(Field field, Part part) {
        if (field.isAnnotationPresent(RequiredField.class)) {
            String[] requiredContentTypes = field.getAnnotation(RequiredField.class).contentTypes();
            if (requiredContentTypes.length == 0) {
                return part.getContentType() == null;
            }
            return Arrays.stream(requiredContentTypes).anyMatch(ct -> ct.equals(part.getContentType()));
        }
        else if (field.isAnnotationPresent(OptionalField.class)) {
            String[] optionalContentTypes = field.getAnnotation(OptionalField.class).contentTypes();
            if (optionalContentTypes.length == 0) {
                return part.getContentType() == null;
            }
            return Arrays.stream(optionalContentTypes).anyMatch(ct -> ct.equals(part.getContentType()));
        }
        return false;
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
        }
        catch (IOException | IllegalAccessException | NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public MultiPartValidator(HttpServletRequest request) {
        if (!request.getContentType().startsWith("multipart/form-data;")) {
            throw new IllegalArgumentException("Invalid content type.");
        }
        try {
            Collection<Part> parts = request.getParts();
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(RequiredField.class)) {
                    Part part = parts.stream().filter(p -> p.getName().equals(field.getAnnotation(RequiredField.class).name())).findFirst().orElse(null);
                    if (part == null || part.getSize() == 0 || !hasSameMime(field, part)) {
                        throw new IllegalArgumentException("Missing required field: " + field.getName());
                    }
                    assignField(field, this, part);
                }
                else if (field.isAnnotationPresent(OptionalField.class)) {
                    Part part = parts.stream().filter(p -> p.getName().equals(field.getAnnotation(OptionalField.class).name())).findFirst().orElse(null);
                    if (part != null && part.getSize() > 0) {
                        if (!hasSameMime(field, part)) {
                            throw new IllegalArgumentException("Invalid content type for field: " + field.getName());
                        }
                        assignField(field, this, part);
                    }
                    else {
                        field.set(this, null);
                    }
                }
            }

        }
        catch (IOException | ServletException e ) {
            throw new IllegalArgumentException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
