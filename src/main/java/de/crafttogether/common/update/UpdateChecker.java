package de.crafttogether.common.update;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.util.CommonUtil;
import org.jetbrains.annotations.Nullable;

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
        void operation(@Nullable Exception error, String installedVersion, String installedBuild, @Nullable Build build);
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
     * @param projectName
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesAsync(String projectName, Consumer consumer, boolean checkForDevBuilds) {
        platform.getRunnableFactory().create(() -> checkUpdatesSync(projectName, consumer, checkForDevBuilds)).runTaskAsynchronously();
    }

    /**
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesAsync(Consumer consumer, boolean checkForDevBuilds) {
        platform.getRunnableFactory().create(() -> checkUpdatesSync(consumer, checkForDevBuilds)).runTaskAsynchronously();
    }

    /**
     * @param projectName
     * @param consumer
     * @param checkForDevBuilds
     * @param delay
     */
    public void checkUpdatesAsync(String projectName, Consumer consumer, boolean checkForDevBuilds, long delay) {
        platform.getRunnableFactory().create(() -> checkUpdatesSync(projectName, consumer, checkForDevBuilds)).runTaskLaterAsynchronously(delay);
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
        checkUpdatesSync(platform.getPluginInformation().getName(), consumer, checkForDevBuilds);
    }
    /**
     * @param projectName
     * @param consumer
     * @param checkForDevBuilds
     */
    public void checkUpdatesSync(String projectName, Consumer consumer, boolean checkForDevBuilds) {
        Gson gson = new Gson();
        String json;

        String installedVersion = platform.getPluginInformation().getVersion();
        String installedBuild = platform.getPluginInformation().getBuild();

        try {
            json = CommonUtil.readUrl("https://api.craft-together-mc.de/plugins/updates/?name=" + projectName);
        } catch (Exception e) {
            consumer.operation(e, installedVersion, installedBuild, null);
            return;
        }

        try {
            JsonObject response = gson.fromJson(json, JsonObject.class);

            if (response != null && response.has("error")) {
                Exception err = new UpdateFailedExeption(response.get("error").getAsString());
                consumer.operation(err, installedVersion, installedBuild, null);
            }
            else if (response != null && response.has("builds")) {
                JsonArray builds = response.getAsJsonArray("builds");

                for (JsonElement element : builds) {
                    Build build = gson.fromJson(element, Build.class);

                    int currentBuildNumber = 0, installedBuildNumber = 0;
                    try {
                        currentBuildNumber = build.getNumber();
                        installedBuildNumber = Integer.parseInt(installedBuild);
                    } catch (Exception ignored) {}

                    if ((checkForDevBuilds || build.getType().equals(BuildType.RELEASE)) && currentBuildNumber > installedBuildNumber) {
                        consumer.operation(null, installedVersion, installedBuild, build);
                        return;
                    }
                }

                consumer.operation(null, installedVersion, installedBuild, null);
            }
            else {
                consumer.operation(null, installedVersion, installedBuild, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            consumer.operation(e, installedVersion, installedBuild, null);
        }
    }
}