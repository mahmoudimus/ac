package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.sal.core.pluginsettings.AbstractStringPluginSettings;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.atlassian.plugin.remotable.container.service.sal.JdbcPluginSettingsFactory
        .TABLE_NAME;

/**
 */
public class JdbcPluginSettings extends AbstractStringPluginSettings
{
    private final DataSource dataSource;
    private final String namespace;

    public JdbcPluginSettings(DataSource dataSource, String namespace)
    {
        this.dataSource = dataSource;
        this.namespace = namespace;
    }

    @Override
    protected void putActual(final String key, final String val)
    {
        runQuery(new JdbcCallback<Void>()
        {
            @Override
            public Void execute(Connection conn) throws SQLException
            {
                if (getActual(key) != null)
                {
                    PreparedStatement updateStatement = conn.prepareStatement(
                            "UPDATE " + TABLE_NAME + " SET value = ? " +
                                    "WHERE key = ? AND namespace = ?");
                    updateStatement.setString(1, val);
                    updateStatement.setString(2, key);
                    updateStatement.setString(3, namespace);
                    updateStatement.executeUpdate();
                }
                else
                {
                    PreparedStatement insertStatement = conn.prepareStatement(
                            "INSERT INTO " + TABLE_NAME + " (namespace, key, value) " +
                                    "VALUES (?, ?, ?)");
                    insertStatement.setString(1, namespace);
                    insertStatement.setString(2, key);
                    insertStatement.setString(3, val);
                    insertStatement.executeUpdate();
                }
                return null;
            }
        });
    }

    private <T> T runQuery(JdbcCallback<T> jdbcCallback)
    {
        Connection conn = null;
        try
        {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            T result =  jdbcCallback.execute(conn);
            conn.commit();
            return result;
        }
        catch (SQLException e)
        {
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (SQLException e1)
                {
                    throw new RuntimeException(e1);
                }
            }
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                // cannot close connection
            }
        }
    }

    @Override
    protected String getActual(final String key)
    {
        return runQuery(new JdbcCallback<String>()
        {
            @Override
            public String execute(Connection conn) throws SQLException
            {
                PreparedStatement getStatement = conn.prepareStatement(
                        "SELECT value FROM  " + TABLE_NAME + " WHERE " +
                                "namespace = ? AND " +
                                "key = ?");
                getStatement.setString(1, namespace);
                getStatement.setString(2, key);
                ResultSet rs = getStatement.executeQuery();
                if (rs.next())
                {
                    return rs.getString("value");
                }
                else
                {
                    return null;
                }
            }
        });
    }

    @Override
    protected void removeActual(final String key)
    {
        runQuery(new JdbcCallback<Void>()
        {
            @Override
            public Void execute(Connection conn) throws SQLException
            {
                PreparedStatement removeStatement = conn.prepareStatement(
                        "DELETE FROM " + TABLE_NAME + " WHERE " +
                                "namespace = ? AND " +
                                "key = ?");
                removeStatement.setString(1, namespace);
                removeStatement.setString(2, key);
                removeStatement.executeUpdate();
                return null;
            }
        });
    }

    private interface JdbcCallback<T>
    {
        public T execute(Connection conn) throws SQLException;
    }
}
