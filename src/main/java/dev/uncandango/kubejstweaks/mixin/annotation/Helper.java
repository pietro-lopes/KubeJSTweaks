package dev.uncandango.kubejstweaks.mixin.annotation;

import com.google.common.collect.Maps;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.util.Cast;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {
    private static final Map<Class<?>, Map<String, Object>> DEFAULT_VALUES = Maps.newHashMap();

    @Nullable
    public static <T> T getValue(ModFileScanData.AnnotationData annotation, String fieldName) {
        Class<?> annotationClass = null;
        try {
            annotationClass = Class.forName(annotation.annotationType().getClassName());
        } catch (ClassNotFoundException e) {
            KubeJSTweaks.LOGGER.error("Annotation class {} not found", annotation.annotationType().getClassName());
        }
        Object defaultValue = getDefaultValue(annotationClass, fieldName);
        return Cast.cast(annotation.annotationData().getOrDefault(fieldName, defaultValue));
    }

    @Nullable
    private static Object getDefaultValue(@Nullable Class<?> annotation, String fieldName) {
        DEFAULT_VALUES.computeIfAbsent(annotation, annot ->
                Stream.ofNullable(annot).flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
                .filter(method -> !Objects.isNull(method.getDefaultValue()))
                .collect(Collectors.toMap(Method::getName, Helper::returnListIfArray))
        );

        Map<String, Object> defaultMap = DEFAULT_VALUES.get(annotation);
        if (defaultMap == null) {
            KubeJSTweaks.LOGGER.error("Field {} not found in annotation class {}", fieldName, annotation);
            return null;
        }
        return defaultMap.getOrDefault(fieldName, null);
    }

    private static Object returnListIfArray(Method method) {
        var value = method.getDefaultValue();
        return value.getClass().isArray() ? List.of((Object[]) value) : value;
    }
}

