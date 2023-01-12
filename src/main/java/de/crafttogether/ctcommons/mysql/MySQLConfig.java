package de.crafttogether.ctcommons.mysql;

public class MySQLConfig {
    String host;
    Integer port;
    String username;
    String password;
    String database;
    String tablePrefix;

    public MySQLConfig() { }

    public MySQLConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public MySQLConfig(String host, int port, String username, String password, String database, String tablePrefix) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.tablePrefix = tablePrefix;
    }

    public boolean checkInputs() {
        if (tablePrefix == null) tablePrefix = "";
        return (this.host != null && port != null && port > 1 && username != null && password != null);
    }

    public void setHost(String host) { this.host = host; }

    public void setPort(int port) { this.port = port; }

    public void setUsername(String username) { this.username = username; }

    public void setPassword(String password) { this.password = password; }

    public void setDatabase(String database) { this.database = database; }

    public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }

    public String getHost() { return host; }

    public int getPort() { return port; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public String getDatabase() { return database; }

    public String getTablePrefix() { return tablePrefix; }
}