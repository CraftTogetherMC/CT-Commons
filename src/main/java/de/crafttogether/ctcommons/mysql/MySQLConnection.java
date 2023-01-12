package de.crafttogether.ctcommons.mysql;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class MySQLConnection {
    private final Plugin plugin;

    private final HikariDataSource dataSource;
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public interface Consumer<E extends Throwable, V> {
        void operation(E exception, V result);
    }

    public  MySQLConnection(HikariDataSource dataSource, Plugin bukkitPlugin) {
        this.dataSource = dataSource;
        this.plugin = bukkitPlugin;
    }

    private void executeAsync(Runnable task) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    public ResultSet query(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        connection = dataSource.getConnection();
        if (connection != null && !connection.isClosed()) {
            preparedStatement = connection.prepareStatement(finalStatement);
            resultSet = preparedStatement.executeQuery();
        }

        return resultSet;
    }

    public int insert(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        int lastInsertedId = 0;

        connection = dataSource.getConnection();
        if (connection != null && !connection.isClosed()) {
            preparedStatement = connection.prepareStatement(finalStatement, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();
        }

        resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next())
            lastInsertedId = resultSet.getInt(1);

        return lastInsertedId;
    }

    public int update(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        int rows = 0;

        connection = dataSource.getConnection();
        if (connection != null && !connection.isClosed()) {
            preparedStatement = connection.prepareStatement(finalStatement);
            rows = preparedStatement.executeUpdate();
        }

        return rows;
    }

    public Boolean execute(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        boolean result = false;

        connection = dataSource.getConnection();
        if (connection != null && !connection.isClosed()) {
            preparedStatement = connection.prepareStatement(finalStatement);
            result = preparedStatement.execute();
        }

        return result;
    }

    public MySQLConnection queryAsync(String statement, final Consumer<SQLException, ResultSet> consumer, final Object... args) {
        if (args.length > 0) statement = String.format(statement, args);
        final String finalStatement = statement;

        executeAsync(() -> {
            try {
                ResultSet resultSet = query(finalStatement);
                consumer.operation(null, resultSet);
            } catch (SQLException e) {
                consumer.operation(e, null);
            }
        });

        return this;
    }

    public MySQLConnection insertAsync(String statement, final @Nullable Consumer<SQLException, Integer> consumer, final Object... args) {
        if (args.length > 0) statement = String.format(statement, args);
        final String finalStatement = statement;

        executeAsync(() -> {
            try {
                int lastInsertedId = insert(finalStatement);

                if (consumer != null)
                    consumer.operation(null, lastInsertedId);
            } catch (SQLException e) {
                if (consumer != null)
                    consumer.operation(e, 0);
            }
        });

        return this;
    }

    public MySQLConnection updateAsync(String statement, final @Nullable Consumer<SQLException, Integer> consumer, final Object... args) {
        if (args.length > 0) statement = String.format(statement, args);
        final String finalStatement = statement;

        executeAsync(() -> {
            try {
                int rows = update(finalStatement);

                if (consumer != null)
                    consumer.operation(null, rows);
            } catch (SQLException e) {
                if (consumer != null)
                    consumer.operation(e, 0);
            }
        });

        return this;
    }

    public MySQLConnection executeAsync(String statement, final @Nullable Consumer<SQLException, Boolean> consumer, final Object... args) {
        if (args.length > 0) statement = String.format(statement, args);
        final String finalStatement = statement;

        executeAsync(() -> {
            try {
                boolean result = execute(finalStatement);
                if (consumer != null)
                    consumer.operation(null, result);
            } catch (SQLException e) {
                if (consumer != null)
                    consumer.operation(e, false);
            }
        });

        return this;
    }

    public MySQLConnection close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return this;
    }

    public String getTablePrefix() {
        return MySQLAdapter.getAdapter().getConfig().getTablePrefix();
    }
}