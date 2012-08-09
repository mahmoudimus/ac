package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.activeobjects.spi.DatabaseType;
import com.google.common.base.Supplier;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Driver;
import java.sql.SQLException;

import static com.google.common.base.Suppliers.memoize;

final class RemoteAppsDataSourceProvider implements DataSourceProvider
{
    private static final String DATABASE_URL_KEY = "DATABASE_URL";
    private static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost/ra?user=ra_user&password=ra_pwd";

    private static final Class<? extends Driver> POSTGRES_DRIVER = org.postgresql.Driver.class;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Supplier<DataSource> dataSource;

    RemoteAppsDataSourceProvider()
    {
        this.dataSource = memoize(getC3p0DataSource());
    }

    private Supplier<DataSource> getC3p0DataSource()
    {
        return new Supplier<DataSource>()
        {
            @Override
            public DataSource get()
            {
                return getC3p0DataSource(getUrl());
            }
        };
    }

    private String getUrl()
    {
        return new HerokuUrlTransformer().transform(getDatabaseUrl());
    }

    private String getDatabaseUrl()
    {
        return System.getProperty(DATABASE_URL_KEY, DEFAULT_DATABASE_URL);
    }

    private DataSource getC3p0DataSource(String url)
    {
        try
        {
            log.debug("Connecting to database at '{}'", url);

            final ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass(POSTGRES_DRIVER.getName()); //loads the jdbc driver, apparently
            cpds.setJdbcUrl(url);
            return cpds;
        }
        catch (PropertyVetoException e)
        {
            throw new IllegalStateException("Error loading the postgres JDBC driver '" + POSTGRES_DRIVER + "'", e);
        }
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource.get();
    }

    public void destroy()
    {
        try
        {
            DataSources.destroy(dataSource.get());
        }
        catch (SQLException e)
        {
            log.warn("An error happened 'destroying' the c3p0 data source", e);
        }
    }

    @Override
    public DatabaseType getDatabaseType()
    {
        return DatabaseType.POSTGRESQL;
    }

    @Override
    public String getSchema()
    {
        return null;
    }
}
