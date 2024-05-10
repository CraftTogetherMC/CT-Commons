package de.crafttogether.common.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.crafttogether.common.Logging;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.ctcommons.CTCommons;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class MySQLAdapter {
    private final PlatformAbstractionLayer platform;
    private final HikariConfig config;
    private HikariDataSource dataSource;

    String tablePrefix;

    public MySQLAdapter(PlatformAbstractionLayer platform, HikariConfig config, @Nullable String tablePrefix) {
        this.platform = platform;
        this.config = config;
        this.tablePrefix = tablePrefix;
        this.createDataSource();
    }

    public MySQLAdapter(PlatformAbstractionLayer platform, String host, int port, String username, String password, @Nullable String database, @Nullable String tablePrefix, @Nullable String jdbcArguments) {
        this.platform = platform;
        this.config = new HikariConfig();
        this.tablePrefix = tablePrefix;

        jdbcArguments = (jdbcArguments == null) ? "" : "?" + jdbcArguments;

        if (database != null)
            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database + jdbcArguments);
        else
            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + jdbcArguments);

        this.config.setDriverClassName("de.crafttogether.common.dep.org.mariadb.jdbc.Driver");
        this.config.setUsername(username);
        this.config.setPassword(password);
        this.config.setPoolName("[" + platform.getPluginInformation().getName() + "/MySQL-Pool]");
        this.config.setMaximumPoolSize(10);
        this.config.setMinimumIdle(1);
        this.config.setIdleTimeout(10000);
        this.config.setMaxLifetime(60000);
        this.config.setAutoCommit(true);
        this.createDataSource();
    }

    private void createDataSource() {
        Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.OFF);
        Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.OFF);
        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
        Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.OFF);
        Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.OFF);

        try { this.dataSource = new HikariDataSource(this.config); }
        catch (Exception e) {
            Logging.getLogger().warn("Can't connect to MySQL-Server!", e);
            Logging.getLogger().warn(e.getCause().getMessage());
        }
    }

    public MySQLConnection getConnection() {
        if (this.dataSource == null)
            return null;
        return new MySQLConnection(this, this.dataSource);
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