package de.crafttogether.common.messaging;

import de.crafttogether.CTCommons;

public class CustomClassLoader extends ClassLoader {
    public CustomClassLoader() {  }

    @Override
    protected Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception ignored) { }

        CTCommons.getLogger().warn("FIND REGISTERED CLASS: " + name);
        byte[] classData = MessagingServer.getPacketImplementation(name);

        if (classData == null)
            return null;
        CTCommons.getLogger().warn("SUCCESS");
        return defineClass(name, classData, 0, classData.length);
    }
}