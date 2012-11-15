package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Implementation of plugin settings that saves data into the activeobjects database provider
 * under the table "plugin_settings"
 */
public class JdbcPluginSettingsFactory implements PluginSettingsFactory
{
    static final String TABLE_NAME = "plugin_settings";
    private static final Logger log = LoggerFactory.getLogger(JdbcPluginSettingsFactory.class);

    private final DataSource dataSource;

    public JdbcPluginSettingsFactory(DataSourceProvider dataSourceProvider)
    {
        this.dataSource = dataSourceProvider.getDataSource();

        createTableIfNotExists(dataSource);
    }

    private void createTableIfNotExists(DataSource dataSource)
    {
        try
        {
            Connection conn = dataSource.getConnection();

            ResultSet rs = conn.getMetaData().getTables(null, null, TABLE_NAME, null);
            if (!rs.next())
            {
                rs = conn.getMetaData().getTables(null, null, TABLE_NAME.toUpperCase(Locale.ENGLISH), null);
            }

            if (!rs.next())
            {
                log.info("Creating " + TABLE_NAME + " table");
                Statement statement = conn.createStatement();
                statement.executeUpdate(
                        "CREATE TABLE " + TABLE_NAME + " (" +
                                "key VARCHAR(2048) NOT NULL, " +
                                "namespace VARCHAR(255) NOT NULL, " +
                                "value VARCHAR(10240) NOT NULL, " +
                                "CONSTRAINT pk_" + TABLE_NAME + " PRIMARY KEY(key,namespace)" +
                                ")"
                );
            }
            else
            {
                log.debug("Table " + TABLE_NAME + " already exists");
            }
        }
        catch (SQLException e)
        {
            log.error("Unable to create table " + TABLE_NAME + " due to: " + e.getMessage());
            log.debug("Error creating table", e);
        }
    }

    @Override
    public PluginSettings createSettingsForKey(String key)
    {
        return new JdbcPluginSettings(dataSource, key);
    }

    @Override
    public PluginSettings createGlobalSettings()
    {
        return createSettingsForKey("");
    }
}
