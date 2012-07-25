package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.activeobjects.spi.DatabaseType;
import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;

public final class RemoteAppsDataSourceProvider implements DataSourceProvider
{
    private final DataSource dataSource;

    public RemoteAppsDataSourceProvider()
    {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:ra");
        ds.setUser("sa");
        ds.setPassword("");

        dataSource = ds;
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }

    @Override
    public DatabaseType getDatabaseType()
    {
        return DatabaseType.HSQL;
    }

    @Override
    public String getSchema()
    {
        return null;
    }
}
