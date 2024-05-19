package de.crafttogether.common.util;

import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;

public class CommonUtil {
    public static <T> T[] getClassConstants(Class<T> theClass) {
        return getClassConstants(theClass, theClass);
    }

    public static String humanReadableFileSize(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public static <T> T[] getClassConstants(Class<?> theClass, Class<T> type) {
        if (theClass.isEnum() && type.isAssignableFrom(theClass)) {
            if (type.equals(theClass)) {
                // If same class, return the enum constants instantly
                return type.getEnumConstants();
            } else {
                // Need to create a new array of the type specified
                Object[] constants = theClass.getEnumConstants();
                T[] result = createArray(type, constants.length);
                System.arraycopy(constants, 0, result, 0, constants.length);
                return result;
            }
        } else {
            // Get using reflection
            try {
                Field[] declaredFields = theClass.getDeclaredFields();
                ArrayList<T> constants = new ArrayList<>(declaredFields.length);
                for (Field field : declaredFields) {
                    if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                        T constant = (T) field.get(null);
                        if (constant != null) {
                            constants.add(constant);
                        }
                    }
                }
                return toArray(constants, type);
            } catch (Throwable t) {
                Bukkit.getLogger().warning("Failed to find class constants of " + theClass);
                t.printStackTrace();
                return createArray(type, 0);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] createArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    private static <T> T[] toArray(Collection<?> collection, Class<T> type) {
        return collection.toArray(createArray(type, collection.size()));
    }
}
