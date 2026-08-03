package de.crafttogether.common.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.server.PluginLogger;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

@SuppressWarnings("unused")
public class MySQLConnection {
    private final PlatformAbstractionLayer platformLayer;
    private final PluginLogger logger;
    private final MySQLAdapter adapter;
    private final HikariDataSource dataSource;

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public interface Consumer<E extends Throwable, V> {
        void operation(E exception, V result);
    }

    MySQLConnection(PlatformAbstractionLayer platformLayer, MySQLAdapter adapter, HikariDataSource dataSource) {
        this.platformLayer = platformLayer;
        this.logger = platformLayer.getPluginLogger();
        this.adapter = adapter;
        this.dataSource = dataSource;
    }

    private void executeAsync(Runnable task) {
        platformLayer.getRunnableFactory().create(task).runTaskAsynchronously();
    }

    public ResultSet query(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        this.connection = this.dataSource.getConnection();
        if (this.connection != null && !this.connection.isClosed()) {
            this.preparedStatement = this.connection.prepareStatement(finalStatement);
            this.resultSet = this.preparedStatement.executeQuery();
        }

        return this.resultSet;
    }

    public int insert(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        int lastInsertedId = 0;

        this.connection = this.dataSource.getConnection();
        if (this.connection != null && !this.connection.isClosed()) {
            this.preparedStatement = this.connection.prepareStatement(finalStatement, Statement.RETURN_GENERATED_KEYS);
            this.preparedStatement.executeUpdate();
        }

        this.resultSet = this.preparedStatement.getGeneratedKeys();
        if (this.resultSet.next())
            lastInsertedId = this.resultSet.getInt(1);

        return lastInsertedId;
    }

    public int update(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        int rows = 0;

        this.connection = this.dataSource.getConnection();
        if (this.connection != null && !this.connection.isClosed()) {
            this.preparedStatement = this.connection.prepareStatement(finalStatement);
            rows = this.preparedStatement.executeUpdate();
        }

        return rows;
    }

    public Boolean execute(String statement, final Object... args) throws SQLException {
        if (args.length > 0) statement = String.format(statement, args);
        String finalStatement = statement;

        boolean result = false;

        this.connection = this.dataSource.getConnection();
        if (this.connection != null && !this.connection.isClosed()) {
            this.preparedStatement = this.connection.prepareStatement(finalStatement);
            result = this.preparedStatement.execute();
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

    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed() && this.connection.isValid(1);
        } catch (SQLException e) {
            logger.warn(e.getCause().getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (this.resultSet != null) {
            try {
                this.resultSet.close();
                this.resultSet = null;
            } catch (SQLException e) {
                logger.warn(e.getCause().getMessage(), e);
                e.printStackTrace();
            }
        }

        if (this.preparedStatement != null) {
            try {
                this.preparedStatement.close();
                this.preparedStatement = null;
            } catch (SQLException e) {
                logger.warn(e.getCause().getMessage(), e);
                e.printStackTrace();
            }
        }

        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException e) {
                logger.warn(e.getCause().getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    public MySQLAdapter getAdapter() {
        return this.adapter;
    }

    public String getTablePrefix() {
        return this.adapter.tablePrefix;
    }
}