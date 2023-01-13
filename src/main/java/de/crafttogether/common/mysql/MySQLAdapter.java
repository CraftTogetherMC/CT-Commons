package de.crafttogether.common.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class MySQLAdapter {
    private static MySQLAdapter adapter;

    private final Plugin plugin;
    private final HikariConfig config;
    private HikariDataSource dataSource;

    static String prefix;

    public MySQLAdapter(Plugin plugin, HikariConfig config) {
        adapter = this;
        this.plugin = plugin;
        this.config = config;
        this.createDataSource();
    }

    public MySQLAdapter(Plugin plugin, String host, int port, String username, String password, @Nullable String database, @Nullable String tablePrefix) {
        adapter = this;
        prefix = tablePrefix;

        this.plugin = plugin;
        this.config = new HikariConfig();
        
        if (database != null)
            this.config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        else
            this.config.setJdbcUrl("jdbc:mysql://" + host + ":" + port);

        this.config.setUsername(username);
        this.config.setPassword(password);
        this.config.setPoolName("[" + plugin.getDescription().getName() + "/MySQL-Pool]");
        this.config.setMaximumPoolSize(10);
        this.config.setMinimumIdle(1);
        this.config.setIdleTimeout(10000);
        this.config.setMaxLifetime(60000);
        this.config.setAutoCommit(true);
        this.createDataSource();
    }

    private void createDataSource() {
        try { this.dataSource = new HikariDataSource(config); }
        catch (Exception ignored) { }
    }

    public static MySQLAdapter getAdapter() {
        return adapter;
    }

    public static MySQLConnection getConnection() {
        if (adapter.dataSource == null)
            return null;
        return new MySQLConnection(adapter.dataSource, adapter.plugin);
    }

    public HikariConfig getConfig() {
        return this.config;
    }

    public void disconnect() {
        if (dataSource != null)
            dataSource.close();
    }
}