package de.crafttogether.ctcommons.localization;

import de.crafttogether.ctcommons.CTCommons;
import de.crafttogether.ctcommons.Localization;
import de.crafttogether.ctcommons.util.CommonUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalizationManager {
    private static final CTCommons plugin = CTCommons.plugin;

    private final YamlConfiguration localizationConfig;

    private String localeFolder;
    private String localeFile;
    private String localeKey;

    private List<String> headers = new ArrayList<>();

    public LocalizationManager(Plugin plugin, String useLocale, String defaultLocale, String localeFolder) {
        this.localeKey = useLocale;
        this.localeFolder = plugin.getDataFolder() + File.separator + localeFolder;
        this.localeFile = this.localeFolder + File.separator + useLocale + ".yml";

        // Set up information header
        addHeader("Below are the localization nodes set for plugin '" + plugin.getName() + "'.");
        addHeader("For colors and text-formatting use the MiniMessage format.");
        addHeader("https://docs.adventure.kyori.net/minimessage/format.html");
        addHeader("");

        // Create folder if not exists
        try {
            if (new File(localeFolder).createNewFile())
                plugin.getLogger().info("Created folder: '" + localeFolder + "'");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed creating folder: '" + localeFolder + "'");
            plugin.getLogger().warning(e.getMessage());
        }

        this.localizationConfig = new YamlConfiguration();

        // Create file if not existing
        if (!new File(localeFile).exists()) {
            plugin.getLogger().warning("Could not find locale file: '" + localeFile + "' switching to default language. (" + defaultLocale + ")");

            this.localeKey = defaultLocale;
            this.localeFile = this.localeFolder + File.separator + this.localeKey + ".yml";
            this.setupHeaders();
        }

        // Otherwise load its contents
        else
            this.loadLocalization();

        // Apply defaults
        this.loadLocales(Localization.class);

        // Save
        this.saveLocalization();
    }

    private void setupHeaders() {
        StringBuilder headers = new StringBuilder();

        for (var i = 0; i < this.headers.size(); i++) {
            if (i == headers.length() - 1)
                headers.append(this.headers.get(i));
            else
                headers.append(this.headers.get(i)).append(System.lineSeparator());
        }

        this.localizationConfig.options().header(headers.toString());
    }

    public void setHeader(String string) {
        headers = new ArrayList<>();
        addHeader(string);
    }

    public void addHeader(String string) {
        if (!string.isEmpty() && !string.startsWith("#")) string = "#> " + string;
        headers.add(string);
    }

    public void loadLocales(Class<? extends ILocalizationDefault> localizationDefaults) {
        for (ILocalizationDefault def : CommonUtil.getClassConstants(localizationDefaults))
            this.loadLocale(def);
    }

    public void loadLocale(ILocalizationDefault localizationDefault) {
        localizationDefault.initDefaults(this.localizationConfig);
    }

    public void loadLocale(String path, String defaultValue) {
        path = path.toLowerCase(Locale.ENGLISH);
        if (!this.localizationConfig.contains(path))
            this.localizationConfig.set(path, defaultValue);
    }

    public String getLocale(String path) {
        path = path.toLowerCase(Locale.ENGLISH);
        return this.localizationConfig.getString(path, "");
    }

    public String getLocaleKey() {
        return localeKey;
    }

    public void setLocaleKey(String localeKey) {
        this.localeKey = localeKey;
        this.localeFile = localeFolder + File.separator + localeKey + ".yml";
        this.loadLocalization();
    }

    public String getLocaleFolder() {
        return localeFolder;
    }

    public void setLocaleFolder(String localeFolder) {
        this.localeFolder = plugin.getDataFolder() + File.separator + localeFolder;
    }

    public void loadLocalization() {
        try {
            this.localizationConfig.load(localeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed reading locale file: '" + localeFile + "'");
            plugin.getLogger().warning(e.getMessage());
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning("Failed parsing locale file: '" + localeFile + "'");
            plugin.getLogger().warning(e.getMessage());
        }
    }

    public void saveLocalization() {
        if (this.localizationConfig == null) {
            plugin.getLogger().warning("Can't save locale file: '" + localeFile + "' because there is no localization loaded");
            return;
        }

        try {
            this.localizationConfig.save(localeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed saving locale file: '" + localeFile + "'");
            plugin.getLogger().warning(e.getMessage());
        }
    }
}
