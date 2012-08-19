package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.activeobjects.spi.DatabaseType;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Supplier;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.Driver;
import java.sql.SQLException;

import static com.google.common.base.Suppliers.*;

final class RemoteAppsDataSourceProvider implements DataSourceProvider
{
    private static final String DATABASE_URL_KEY = "DATABASE_URL";

    private static final Class<? extends Driver> POSTGRES_DRIVER = org.postgresql.Driver.class;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Supplier<DatabaseInfo> dbInfo;
    private final Supplier<DataSource> dataSource;
    private final String databaseUrl;

    RemoteAppsDataSourceProvider(ApplicationProperties applicationProperties)
    {
        this.dbInfo = memoize(getDatabaseInfo());
        this.dataSource = memoize(getC3p0DataSource());

        File dataDir = new File(applicationProperties.getHomeDirectory(), "data");
        dataDir.mkdir();
        this.databaseUrl = System.getProperty(DATABASE_URL_KEY, "jdbc:hsqldb:file:" + dataDir.getAbsolutePath() + "/db");
    }

    private Supplier<DataSource> getC3p0DataSource()
    {
        return new Supplier<DataSource>()
        {
            @Override
            public DataSource get()
            {
                return getC3p0DataSource(dbInfo.get());
            }
        };
    }

    private String getUrl()
    {
        return new HerokuUrlTransformer().transform(getDatabaseUrl());
    }

    private String getDatabaseUrl()
    {
        return this.databaseUrl;
    }

    private DataSource getC3p0DataSource(DatabaseInfo info)
    {
        try
        {
            log.debug("Connecting to database at '{}'", info);

            final ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass(info.driver); //loads the jdbc driver, apparently
            cpds.setJdbcUrl(info.url);
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
        return dbInfo.get().type;
    }

    @Override
    public String getSchema()
    {
        return null;
    }

    private Supplier<DatabaseInfo> getDatabaseInfo()
    {
        return new Supplier<DatabaseInfo>()
        {
            @Override
            public DatabaseInfo get()
            {
                return getDatabaseInfo(getUrl());
            }
        };
    }

    private DatabaseInfo getDatabaseInfo(String url)
    {
        if (url.startsWith("jdbc:postgresql"))
        {
            return new DatabaseInfo(url, POSTGRES_DRIVER.getName(), DatabaseType.POSTGRESQL);
        }
        else if (url.startsWith("jdbc:hsql"))
        {
            return new DatabaseInfo(url, "org.hsqldb.jdbc.JDBCDriver", DatabaseType.HSQL);
        }
        else
        {
            throw new IllegalStateException("Database URL was neither for HSQL nor for Postgres!: " + url);
        }
    }

    private static class DatabaseInfo
    {
        final String url;
        final String driver;
        final DatabaseType type;

        private DatabaseInfo(String url, String driver, DatabaseType type)
        {
            this.url = url;
            this.driver = driver;
            this.type = type;
        }

        private String load(String driver)
        {
            try
            {
                Class.forName(driver);
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalStateException("Could not load driver '" + driver + "'", e);
            }
            return driver;
        }
    }
}
