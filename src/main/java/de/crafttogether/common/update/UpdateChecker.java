package de.crafttogether.common.update;

import com.google.gson.*;
import de.crafttogether.common.util.CommonUtil;
import de.crafttogether.common.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class UpdateChecker {
    private final Plugin plugin;

    public interface Consumer {
        void operation(@Nullable Exception error, @Nullable Build build, String installedVersion, String installedBuild);
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

        String installedBuildVersion = plugin.getDescription().getVersion();
        Configuration pluginDescription = PluginUtil.getPluginFile(plugin);
        String stringBuildNumber = pluginDescription == null ? null : (String) pluginDescription.get("build");

        try {
            json = CommonUtil.readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + plugin.getDescription().getName());
        } catch (Exception e) {
            consumer.operation(e, null, installedBuildVersion, stringBuildNumber);
            return;
        }

        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);
            if (response.has("builds")) {
                JsonArray builds = response.getAsJsonArray("builds");

                for (JsonElement element : builds) {
                    Build build = gson.fromJson(element, Build.class);

                    if (checkForDevBuilds && !build.getType().equals(BuildType.RELEASE)) {
                        consumer.operation(null, build, installedBuildVersion, stringBuildNumber);
                        break;
                    }

                    else if (build.getType().equals(BuildType.RELEASE)) {
                        consumer.operation(null, build, installedBuildVersion, stringBuildNumber);
                        break;
                    }
                }
            }
            else
                consumer.operation(null, null, installedBuildVersion, stringBuildNumber);

        } catch (Exception e) {
            consumer.operation(e, null, installedBuildVersion, stringBuildNumber);
        }
    }

}