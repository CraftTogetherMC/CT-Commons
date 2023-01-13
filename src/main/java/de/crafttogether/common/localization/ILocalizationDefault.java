package de.crafttogether.common.localization;

import org.bukkit.configuration.file.YamlConfiguration;

public interface ILocalizationDefault {

    String getName();
    String getDefault();

    default void initDefaults(YamlConfiguration config) {
        String path = this.getName();
        if (!config.contains(path))
            writeDefaults(config, path);
    }

    default void writeDefaults(YamlConfiguration config, String path) {
        config.set(path, this.getDefault());
    }
}
