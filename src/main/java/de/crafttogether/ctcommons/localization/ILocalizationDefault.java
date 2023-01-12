package de.crafttogether.ctcommons.localization;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;

public interface ILocalizationDefault {
    String getName();

    String getDefault();

    default void initDefaults(YamlConfiguration config) {
        String path = this.getName().toLowerCase(Locale.ENGLISH);
        if (!config.contains(path)) {
            writeDefaults(config, path);
        }
    }

    default void writeDefaults(YamlConfiguration config, String path) {
        config.set(path, this.getDefault());
    }
}
