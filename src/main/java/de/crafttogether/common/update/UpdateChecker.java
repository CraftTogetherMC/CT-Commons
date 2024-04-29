package de.crafttogether.common.update;

import com.google.gson.*;
import de.crafttogether.common.util.CommonUtil;
import de.crafttogether.common.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Retrieve update information from api.craft-together.de
 **/

public class UpdateChecker {
    private final Plugin plugin;

    /**
     *
     */
    public interface Consumer {
        /**
         * @param error
         * @param build
         * @param installedVersion
         * @param installedBuild
         */
        void operation(@Nullable Exception error, @Nullable Build build, String installedVersion, String installedBuild);
    }

    public class UpdateFailedExeption extends Exception {
        public UpdateFailedExeption(String errorMessage) {
            super(errorMessage);
        }
    }

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> checkUpdatesSync(consumer, checkForDevBuilds));
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     * @param delay
     */
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> checkUpdatesSync(consumer, checkForDevBuilds), delay);
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesSync(Consumer consumer, boolean checkForDevBuilds) {
        Gson gson = new Gson();
        String json;

        String installedVersion = this.plugin.getDescription().getVersion();
        Configuration pluginDescription = PluginUtil.getPluginFile(this.plugin);
        String installedBuild = pluginDescription.get("build") == null ? "unkown" : String.valueOf(pluginDescription.get("build"));

        try {
            json = CommonUtil.readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + plugin.getDescription().getName());
        } catch (Exception e) {
            consumer.operation(e, null, installedVersion, installedBuild);
            return;
        }

        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);

            if (response != null && response.has("error")) {
                Exception err = new UpdateFailedExeption(response.get("error").getAsString());
                consumer.operation(err, null, installedVersion, installedBuild);
            }
            else if (response != null && response.has("builds")) {
                JsonArray builds = response.getAsJsonArray("builds");

                for (JsonElement element : builds) {
                    Build build = gson.fromJson(element, Build.class);

                    int currentBuildNumber = 0, installedBuildNumber = 0;
                    try {
                        currentBuildNumber = Integer.parseInt(build.getVersion());
                        installedBuildNumber = Integer.parseInt(installedBuild);
                    } catch (Exception ignored) {}

                    if (checkForDevBuilds || build.getType().equals(BuildType.RELEASE) && currentBuildNumber > installedBuildNumber) {
                        consumer.operation(null, build, installedVersion, installedBuild);
                        return;
                    }
                }

                consumer.operation(null, null, installedVersion, installedBuild);
            }
            else {
                consumer.operation(null, null, installedVersion, installedBuild);
            }
        } catch (Exception e) {
            e.printStackTrace();
            consumer.operation(e, null, installedVersion, installedBuild);
        }
    }
}