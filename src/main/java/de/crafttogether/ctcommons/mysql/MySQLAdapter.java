package de.crafttogether.ctcommons.mysql;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

public class MySQLAdapter {
    private static MySQLAdapter adapter;

    private final Plugin plugin;
    private final MySQLConfig config;

    private HikariDataSource dataSource;

    public MySQLAdapter(Plugin pluginInstance, MySQLConfig _config) {
        plugin = pluginInstance;
        adapter = this;
        config = _config;
        setupHikari();
    }

    public MySQLAdapter(Plugin bukkitPlugin, String host, int port, String database, String username, String password, String tablePrefix) {
        adapter = this;
        this.plugin = bukkitPlugin;
        this.config = new MySQLConfig(host, port, database, username, password, tablePrefix);
        setupHikari();
    }

    private void setupHikari() {
        this.dataSource = new HikariDataSource();

        if (config.getDatabase() != null)
            this.dataSource.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());
        else
            this.dataSource.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort());

        this.dataSource.setUsername(config.getUsername());
        this.dataSource.setPassword(config.getPassword());
        this.dataSource.setPoolName("[" + plugin.getDescription().getName() + "/MySQL-Pool]");

        this.dataSource.setMaximumPoolSize(10);
        this.dataSource.setMinimumIdle(1);
        this.dataSource.setIdleTimeout(10000);
        this.dataSource.setMaxLifetime(60000);
        this.dataSource.setAutoCommit(true);
    }

    public static MySQLAdapter getAdapter() {
        return adapter;
    }

    public static MySQLConnection getConnection() {
        return new MySQLConnection(adapter.dataSource, adapter.plugin);
    }

    public MySQLConfig getConfig() {
        return this.config;
    }

    public void disconnect() {
        dataSource.close();
    }
}