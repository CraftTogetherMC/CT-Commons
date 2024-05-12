package de.crafttogether.common.util;

import de.crafttogether.common.Logging;
import de.crafttogether.common.configuration.file.FileConfiguration;
import de.crafttogether.common.configuration.file.YamlConfiguration;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PluginUtil {
    public static void saveDefaultConfig(File dataFolder, String fileName, InputStream templateFile) {
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File configFile = new File(dataFolder, fileName);

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                InputStream in = templateFile;
                in.transferTo(outputStream);
            }
            catch (IOException e) {
                Logging.getLogger().warn("Failed saving configuration file: '" + fileName + "'", e);
            }
        }
    }

    public static void saveDefaultConfig(File dataFolder, InputStream templateFile) {
        saveDefaultConfig(dataFolder, "config.yml", templateFile);
    }

    public static void saveDefaultConfig(PlatformAbstractionLayer platform, InputStream templateFile) {
        saveDefaultConfig(platform.getPluginInformation().getDataFolder(), templateFile);
    }
    public static void saveDefaultConfig(PlatformAbstractionLayer platform, String fileName, InputStream templateFile) {
        saveDefaultConfig(platform.getPluginInformation().getDataFolder(), fileName, templateFile);
    }
    public static void saveDefaultConfig(PlatformAbstractionLayer platform) {
        saveDefaultConfig(platform.getPluginInformation().getDataFolder(), platform.getPluginInformation().getResourceFromJar("config.yml"));
    }

    public static FileConfiguration getConfig(File dataFolder, String fileName) {
        File file = new File(dataFolder.getAbsolutePath() + File.separator + fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getConfig(PlatformAbstractionLayer platform, String fileName) {
        return getConfig(platform.getPluginInformation().getDataFolder(), fileName);
    }
    public static FileConfiguration getConfig(PlatformAbstractionLayer platform) {
        return getConfig(platform.getPluginInformation().getDataFolder(), "config.yml");
    }
}
