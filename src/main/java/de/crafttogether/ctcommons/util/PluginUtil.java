package de.crafttogether.ctcommons.util;

import com.google.common.io.ByteStreams;
import de.crafttogether.ctcommons.CTCommons;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class PluginUtil {
    private static final CTCommons plugin = CTCommons.plugin;

    public static void exportResource(String fileName) {
        File outputFile = new File(plugin.getDataFolder() + File.separator + fileName);
        if (outputFile.exists())
            return;

        if (!plugin.getDataFolder().exists()) {
            if(!plugin.getDataFolder().mkdir())
                return;
        }

        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream == null) {
            plugin.getLogger().warning("Could not read resource '" + fileName + "'");
            return;
        }

        try {
            if (outputFile.createNewFile()) {
                OutputStream os = new FileOutputStream(outputFile);
                ByteStreams.copy(inputStream, os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration getPluginFile() {
        InputStream inputStream = plugin.getResource("plugin.yml");
        if (inputStream == null) return null;
        return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
    }
}
