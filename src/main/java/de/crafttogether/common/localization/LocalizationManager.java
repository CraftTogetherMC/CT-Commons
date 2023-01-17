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

@SuppressWarnings({"unused", "deprecation"})
public class LocalizationManager {
    private final Plugin plugin;

    private final Class<? extends ILocalizationDefault> localization;
    private final String defaultLocale;

    private YamlConfiguration localizationConfig;
    private String localeKey;
    private String localeFolder;
    private String localeFile;

    private List<String> headers = new ArrayList<>();
    private List<Placeholder> placeholders = new ArrayList<>();
    private List<TagResolver> tagResolvers = new ArrayList<>();

    public LocalizationManager(Plugin plugin, Class<? extends ILocalizationDefault> localization, String defaultLocale, String localeFolder) {
        this.plugin = plugin;
        this.localization = localization;
        this.defaultLocale = defaultLocale;
        this.localeKey = defaultLocale;
        this.localeFolder = plugin.getDataFolder() + File.separator + localeFolder;
        this.localeFile = this.localeFolder + File.separator + defaultLocale + ".yml";

        // Set up information header
        setHeader("Below are the localization nodes set for plugin '" + this.plugin.getName() + "'.");
        addHeader("For colors and text-formatting use the MiniMessage format.");
        addHeader("https://docs.adventure.kyori.net/minimessage/format.html");
    }

    public void loadLocalization(String localeKey) {
        this.localizationConfig = new YamlConfiguration();
        this.localeKey = localeKey;
        this.localeFile = this.localeFolder + File.separator + localeKey + ".yml";

        File folder = new File(this.localeFolder);
        if ((!folder.exists() || folder.isFile()) && folder.mkdir())
            this.plugin.getLogger().info("Created folder: '" + this.localeFolder + "'");

        if (!new File(this.localeFile).exists()) {
            this.localizationConfig.options().header(this.getHeaderString());

            if (!localeKey.equals(this.defaultLocale)) {
                this.plugin.getLogger().warning("Could not find locale file: '" + this.localeFile + "' switching to default language. (" + this.defaultLocale + ")");
                this.loadLocalization(this.defaultLocale);
                return;
            }
        }
        else {
            this.loadLocalization();
        }

        this.loadLocales(this.localization);
        this.saveLocalization();
    }

    private void loadLocalization() {
        try {
            this.localizationConfig.load(this.localeFile);
        } catch (IOException e) {
            this.plugin.getLogger().warning("Failed reading locale file: '" + this.localeFile + "'");
            this.plugin.getLogger().warning(e.getMessage());
        } catch (InvalidConfigurationException e) {
            this.plugin.getLogger().warning("Failed parsing locale file: '" + this.localeFile + "'");
            this.plugin.getLogger().warning(e.getMessage());
        }
    }

    public void saveLocalization() {
        if (this.localizationConfig == null) {
            this.plugin.getLogger().warning("Can't save locale file: '" + this.localeFile + "' because there is no localization loaded yet.");
            return;
        }

        try {
            this.localizationConfig.save(this.localeFile);
        } catch (IOException e) {
            this.plugin.getLogger().warning("Failed saving locale file: '" + this.localeFile + "'");
            this.plugin.getLogger().warning(e.getMessage());
        }
    }

    public void loadLocales(Class<? extends ILocalizationDefault> localizationDefaults) {
        for (ILocalizationDefault def : CommonUtil.getClassConstants(localizationDefaults))
            this.loadLocale(def);
    }

    public void loadLocale(ILocalizationDefault localizationDefault) {
        localizationDefault.initDefaults(this.localizationConfig);
    }

    public String getLocale(String path) {
        return this.localizationConfig.getString(path, "");
    }

    public void setLocale(String path, String defaultValue) {
        if (!this.localizationConfig.contains(path))
            this.localizationConfig.set(path, defaultValue);
    }

    public void setHeader(String string) {
        this.headers = new ArrayList<>();
        addHeader(string);
    }

    public void addHeader(String string) {
        if (!string.startsWith("#")) string = "#> " + string;
        this.headers.add(string);
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

    public String getLocaleKey() {
        return this.localeKey;
    }

    public void setLocaleKey(String localeKey) {
        this.localeFile = this.localeFolder + File.separator + this.defaultLocale + ".yml";
    }

    public String getLocaleFolder() {
        return localeFolder;
    }

    public void setLocaleFolder(String localeFolder) {
        this.localeFolder = this.plugin.getDataFolder() + File.separator + localeFolder;
        this.localeFile = this.localeFolder + File.separator + this.defaultLocale + ".yml";
    }

    public void addTagResolver(TagResolver tagResolver) {
        this.tagResolvers.add(tagResolver);
    }

    public void addTagResolver(String key, Component value) {
        addTagResolver(TagResolver.resolver(key, Tag.selfClosingInserting(value)));
    }

    public void addTagResolver(String key, Object value) {
        addTagResolver(key, Component.text(value.toString()));
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

    public List<TagResolver> getTagResolvers() {
        return tagResolvers;
    }

    public void setTagResolvers(List<TagResolver> tagResolvers) {
        this.tagResolvers = tagResolvers;
    }

    public void addPlaceholder(Placeholder placeholder) {
        this.placeholders.add(placeholder);
    }

    public void addPlaceholder(String key, Object value) {
        this.placeholders.add(Placeholder.set(key, value.toString()));
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

    public void setPlaceholders(List<Placeholder> placeholders) {
        this.placeholders = placeholders;
    }

    public void removePlaceholder(Placeholder placeholder) {
        this.placeholders.remove(placeholder);
    }

    public List<Placeholder> getPlaceholders() {
        return placeholders;
    }

    public MiniMessage miniMessage() {
        return MiniMessage.builder().editTags(t -> t.resolvers(this.tagResolvers)).build();
    }
}
