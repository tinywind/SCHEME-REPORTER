package org.tinywind.schemereporter.html;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateUtils {
    public static String listJoin(List<?> list, String delimiter, String propertyName) {
        return list.stream().map(item -> {
            try {
                return item.getClass().getMethod("get" + capitalize(propertyName)).invoke(item).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(delimiter));
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
