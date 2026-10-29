package org.tinywind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

public final class GuavaPayloadProbe {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        String prefix = "org.jetbrains.kotlin.com.google.common.collect.";
        Object value;
        if (args[0].equals("MapMakerInternalMap")) {
            Class<?> type = Class.forName(prefix + "MapMaker");
            Object maker = type.getConstructor().newInstance();
            type.getMethod("weakKeys").invoke(maker);
            value = type.getMethod("makeMap").invoke(maker);
        } else {
            Constructor<?> constructor = Class.forName(prefix + args[0]).getDeclaredConstructor();
            constructor.setAccessible(true);
            value = constructor.newInstance();
            if (value instanceof Map) ((Map<Object, Object>) value).put(1, "one");
            else ((Set<Object>) value).add(1);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())) {
            @Override
            public int readInt() throws IOException {
                super.readInt();
                return Integer.MAX_VALUE;
            }
        }) {
            input.readObject();
            System.out.println("DESERIALIZED");
        } catch (InvalidClassException rejected) {
            if (!rejected.getMessage().contains("REJECTED")) throw rejected;
            System.out.println("FILTER_REJECTED");
        } catch (OutOfMemoryError exhausted) {
            System.out.println("VULNERABLE_ALLOCATION");
        }
    }
}
