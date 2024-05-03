package de.crafttogether.common.util;

import com.google.common.io.ByteStreams;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public class PluginUtil {

    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(name)).toList().get(0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void exportResource(Plugin plugin, String fileName) {
        File outputFile = new File(plugin.getDataFolder() + File.separator + fileName);
        if (outputFile.exists())
            return;

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream == null) {
            plugin.getLogger().warning("Could not read resource '" + fileName + "'");
            return;
        }

        try {
            outputFile.createNewFile();
            OutputStream os = new FileOutputStream(outputFile);
            ByteStreams.copy(inputStream, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration getPluginFile(Plugin plugin) {
        InputStream inputStream = plugin.getResource("plugin.yml");
        if (inputStream == null) return new YamlConfiguration();
        return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
    }

    public static BukkitAudiences adventure() {
        return CTCommonsBukkit.plugin.adventure();
    }
}
