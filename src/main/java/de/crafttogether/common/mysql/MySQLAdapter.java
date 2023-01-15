package de.crafttogether.common.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MySQLAdapter {
    private final Plugin plugin;
    private final HikariConfig config;
    private HikariDataSource dataSource;

    String tablePrefix;

    public MySQLAdapter(Plugin plugin, HikariConfig config, @Nullable String tablePrefix) {
        this.plugin = plugin;
        this.config = config;
        this.tablePrefix = tablePrefix;
        this.createDataSource();
    }

    public MySQLAdapter(Plugin plugin, String host, int port, String username, String password, @Nullable String database, @Nullable String tablePrefix) {
        this.plugin = plugin;
        this.config = new HikariConfig();
        this.tablePrefix = tablePrefix;
        
        if (database != null)
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        else
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port);

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
        try { this.dataSource = new HikariDataSource(this.config); }
        catch (Exception ignored) { }
    }

    public MySQLConnection getConnection() {
        if (this.dataSource == null)
            return null;
        return new MySQLConnection(this, this.dataSource, this.plugin);
    }

    public boolean isActive() {
        return this.dataSource != null && !this.dataSource.isClosed() && this.dataSource.isRunning();
    }

    public HikariConfig getConfig() {
        return this.config;
    }

    public void disconnect() {
        if (this.dataSource != null)
            this.dataSource.close();
    }
}