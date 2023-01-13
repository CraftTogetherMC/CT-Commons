package de.crafttogether.common.update;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.crafttogether.common.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class UpdateChecker {
    private final Plugin plugin;

    public interface Consumer {
        void operation(String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType);
    }

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> checkUpdates(consumer, checkForDevBuilds));
    }

    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> checkUpdates(consumer, checkForDevBuilds), delay);
    }

    public void checkUpdates(Consumer consumer, boolean checkForDevBuilds) {
        Gson gson = new Gson();
        String json;

        try {
            json = readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + plugin.getDescription().getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to retrieve update-informations from ci.craft-together-mc.de");
            e.printStackTrace();
            return;
        }

        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            JsonObject lastDevBuild = response.getAsJsonObject("lastDevBuild");
            JsonObject lastRelease = response.getAsJsonObject("lastRelease");

            int installedBuildNumber = 0;
            String installedBuildVersion = plugin.getDescription().getVersion();
            Configuration pluginDescription = PluginUtil.getPluginFile(plugin);
            String stringBuildNumber = pluginDescription == null ? null : (String) pluginDescription.get("build");

            if (stringBuildNumber != null && !stringBuildNumber.equals("NO-CI"))
                installedBuildNumber = Integer.parseInt(stringBuildNumber);

            String lastDevBuildVersion = stringOrNull(lastDevBuild, "version");
            int lastDevBuildNumber = intOrZero(lastDevBuild, "build");
            String lastDevBuildFileName = stringOrNull(lastDevBuild, "fileName");
            int lastDevBuildFileSize = intOrZero(lastDevBuild, "fileSize");
            String lastDevBuildUrl = stringOrNull(lastDevBuild, "url");

            String lastReleaseVersion = stringOrNull(lastRelease, "version");
            String lastReleaseFileName = null, lastReleaseUrl = null;
            int lastReleaseNumber = 0, lastReleaseFileSize = 0;

            if (lastReleaseVersion != null) {
                lastReleaseNumber = intOrZero(lastRelease, "build");
                lastReleaseFileName = stringOrNull(lastRelease, "fileName");
                lastReleaseFileSize = intOrZero(lastRelease, "fileSize");
                lastReleaseUrl = stringOrNull(lastRelease, "url");
            }

            if (lastReleaseVersion != null && lastReleaseNumber > installedBuildNumber)
                consumer.operation(
                        lastReleaseVersion,
                        String.valueOf(lastReleaseNumber),
                        lastReleaseFileName,
                        lastReleaseFileSize,
                        lastReleaseUrl,
                        installedBuildVersion,
                        stringBuildNumber,
                        BuildType.RELEASE);

            else if (checkForDevBuilds && lastDevBuildNumber > installedBuildNumber)
                consumer.operation(
                        lastDevBuildVersion,
                        String.valueOf(lastDevBuildNumber),
                        lastDevBuildFileName,
                        lastDevBuildFileSize,
                        lastDevBuildUrl,
                        installedBuildVersion,
                        stringBuildNumber,
                        BuildType.SNAPSHOT);

            else
                consumer.operation(null, null, null, null, null,
                        installedBuildVersion, stringBuildNumber, BuildType.UP2DATE);
        }
        catch (Exception e) {
            plugin.getLogger().warning("An error occured while parsing update-informations from ci.craft-together-mc.de");
            e.printStackTrace();
        }
    }

    private String readUrl(String urlString) throws Exception {
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

    private int intOrZero(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement instanceof JsonNull ? 0 : jsonElement.getAsInt();
    }

    private String stringOrNull(JsonObject jsonObject, String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement instanceof JsonNull ? null : jsonElement.getAsString();
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
}