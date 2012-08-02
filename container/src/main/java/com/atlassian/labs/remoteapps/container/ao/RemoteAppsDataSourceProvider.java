package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.activeobjects.spi.DataSourceProvider;
import com.atlassian.activeobjects.spi.DatabaseType;
import com.atlassian.labs.remoteapps.api.DatabaseUrlProvider;
import com.google.common.base.Supplier;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Driver;
import java.sql.SQLException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.memoize;

final class RemoteAppsDataSourceProvider implements DataSourceProvider
{
    private static final Class<? extends Driver> POSTGRES_DRIVER = org.postgresql.Driver.class;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Supplier<ServiceReference> dbUrl;
    private final Supplier<DataSource> dataSource;
    private final BundleContext bundleContext;

    RemoteAppsDataSourceProvider(final BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
        this.dbUrl = memoize(new Supplier<ServiceReference>()
        {
            @Override
            public ServiceReference get()
            {
                return bundleContext.getServiceReference(DatabaseUrlProvider.class.getName());
            }
        });
        this.dataSource = memoize(getC3p0DataSource(dbUrl));
    }

    private Supplier<DataSource> getC3p0DataSource(final Supplier<ServiceReference> url)
    {
        return new Supplier<DataSource>()
        {
            @Override
            public DataSource get()
            {
                final DatabaseUrlProvider service = (DatabaseUrlProvider) bundleContext.getService(url.get());
                return getC3p0DataSource(service.getUrl());
            }
        };
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
        bundleContext.ungetService(dbUrl.get());
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
