package de.crafttogether.common.update;

import com.google.gson.*;
import de.crafttogether.common.configuration.Configuration;
import de.crafttogether.common.configuration.file.FileConfiguration;
import de.crafttogether.common.configuration.file.YamlConfiguration;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.util.CommonUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStreamReader;

/**
 * Retrieve update information from api.craft-together.de
 **/

public class UpdateChecker {
    private final PlatformAbstractionLayer platform;

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

    public static class UpdateFailedExeption extends Exception {
        public UpdateFailedExeption(String errorMessage) {
            super(errorMessage);
        }
    }

    public UpdateChecker(PlatformAbstractionLayer platform) {
        this.platform = platform;
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds) {
        platform.getRunnableFactory().create(() -> checkUpdatesSync(consumer, checkForDevBuilds)).runTaskAsynchronously();
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     * @param delay
     */
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds, long delay) {
        platform.getRunnableFactory().create(() -> checkUpdatesSync(consumer, checkForDevBuilds)).runTaskLaterAsynchronously(delay);
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesSync(Consumer consumer, boolean checkForDevBuilds) {
        Gson gson = new Gson();
        String json;

        String installedVersion = platform.getPluginInformation().getVersion();
        String installedBuild = platform.getPluginInformation().getBuild();

        try {
            json = CommonUtil.readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + platform.getPluginInformation().getName());
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