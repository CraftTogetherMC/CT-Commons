package de.crafttogether.common.messaging;

import java.io.*;

public class CustomObjectInputStream extends ObjectInputStream {
    private ClassLoader classLoader;

    public CustomObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String className = desc.getName();
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}
