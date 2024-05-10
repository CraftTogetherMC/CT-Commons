package de.crafttogether.common;

import de.crafttogether.common.plugin.server.PluginLogger;
import de.crafttogether.ctcommons.CTCommons;

public class Logging {
    public static PluginLogger getLogger() {
        return CTCommons.getPlatform().getPluginLogger();
    }
}
