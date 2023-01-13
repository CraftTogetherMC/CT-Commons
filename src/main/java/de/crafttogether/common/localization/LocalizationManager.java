package de.crafttogether.common.localization;

import de.crafttogether.common.util.CommonUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class LocalizationManager {
    private MiniMessage miniMessage;

    private final YamlConfiguration localizationConfig;
    private final Plugin plugin;

    private String localeFolder;
    private String localeFile;
    private String localeKey;

    private List<String> headers = new ArrayList<>();
    private List<Placeholder> placeholders = new ArrayList<>();
    private List<TagResolver> tagResolvers = new ArrayList<>();

    public LocalizationManager(Plugin plugin, Class<? extends ILocalizationDefault> localization, String useLocale, String defaultLocale, String localeFolder) {
        this.plugin = plugin;

        this.localeKey = useLocale;
        this.localeFolder = plugin.getDataFolder() + File.separator + localeFolder;
        this.localeFile = this.localeFolder + File.separator + useLocale + ".yml";

        // Set up information header
        addHeader("Below are the localization nodes set for plugin '" + plugin.getName() + "'.");
        addHeader("For colors and text-formatting use the MiniMessage format.");
        addHeader("https://docs.adventure.kyori.net/minimessage/format.html");

        // Create folder if not exists
        try {
            if (new File(localeFolder).createNewFile())
                plugin.getLogger().info("Created folder: '" + localeFolder + "'");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed creating folder: '" + localeFolder + "'");
            plugin.getLogger().warning(e.getMessage());
        }

        this.localizationConfig = new YamlConfiguration();
        this.localizationConfig.options().header(this.getHeaderString());

        // Create file if not existing
        if (!new File(localeFile).exists()) {
            if (!localeKey.equals(defaultLocale)) {
                plugin.getLogger().warning("Could not find locale file: '" + localeFile + "' switching to default language. (" + defaultLocale + ")");
                this.localeKey = defaultLocale;
                this.localeFile = this.localeFolder + File.separator + this.localeKey + ".yml";
            }
        }
        // Otherwise load its contents
        else
            this.loadLocalization();

        // Apply defaults
        this.loadLocales(localization);

        // Save
        this.saveLocalization();
    }

    public void setHeader(String string) {
        headers = new ArrayList<>();
        addHeader(string);
    }

    public void addHeader(String string) {
        if (!string.startsWith("#")) string = "#> " + string;
        headers.add(string);
    }

    public void writeHeaders() {
        this.localizationConfig.options().header(this.getHeaderString());
        this.saveLocalization();
    }

    private String getHeaderString() {
        StringBuilder headers = new StringBuilder();
        for (String header : this.headers)
            headers.append(header).append(System.lineSeparator());
        return headers.toString();
    }

    public void loadLocales(Class<? extends ILocalizationDefault> localizationDefaults) {
        for (ILocalizationDefault def : CommonUtil.getClassConstants(localizationDefaults))
            this.loadLocale(def);
    }

    public void loadLocale(ILocalizationDefault localizationDefault) {
        localizationDefault.initDefaults(this.localizationConfig);
    }

    public void loadLocale(String path, String defaultValue) {
        if (!this.localizationConfig.contains(path))
            this.localizationConfig.set(path, defaultValue);
    }

    public String getLocale(String path) {
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

    public List<TagResolver> getTagResolvers() {
        return tagResolvers;
    }

    public void setTagResolvers(List<TagResolver> tagResolvers) {
        this.tagResolvers = tagResolvers;
    }

    public void addTagResolver(TagResolver tagResolver) {
        this.tagResolvers.add(tagResolver);
    }

    public void addTagResolver(String key, Component value) {
        addTagResolver(TagResolver.resolver(key, Tag.selfClosingInserting(value)));
    }

    public void addTagResolver(String key, String value) {
        addTagResolver(key, Component.text(value));
    }

    public void addTagResolver(String key, Integer value) {
        addTagResolver(key, Component.text(value));
    }

    public void addTagResolver(String key, double value) {
        addTagResolver(key, Component.text(value));
    }

    public void addTagResolver(String key, long value) {
        addTagResolver(TagResolver.resolver(key, Tag.selfClosingInserting(Component.text(value))));
    }

    public List<Placeholder> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<Placeholder> placeholders) {
        this.placeholders = placeholders;
    }

    public void addPlaceholder(Placeholder placeholder) {
        this.placeholders.add(placeholder);
    }

    public void addPlaceholder(String key, String value) {
        this.placeholders.add(Placeholder.set(key, value));
    }

    public void addPlaceholder(String key, Integer value) {
        this.placeholders.add(Placeholder.set(key, value));
    }

    public void addPlaceholder(String key, double value) {
        this.placeholders.add(Placeholder.set(key, value));
    }

    public void addPlaceholder(String key, long value) {
        this.placeholders.add(Placeholder.set(key, value));
    }

    public void removePlaceholder(Placeholder placeholder) {
        this.placeholders.remove(placeholder);
    }

    public MiniMessage miniMessage() {
        return MiniMessage.builder().editTags(t -> t.resolvers(this.tagResolvers)).build();
    }
}
