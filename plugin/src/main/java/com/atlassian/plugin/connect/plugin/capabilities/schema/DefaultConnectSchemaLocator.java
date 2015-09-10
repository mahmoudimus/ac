package com.atlassian.plugin.connect.plugin.capabilities.schema;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService
public class DefaultConnectSchemaLocator implements ConnectSchemaLocator
{
    public static final String RAW = "/schema/%s-schema.json";
    public static final String PRETTY = "/schema/%s-schema-pretty.json";

    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;

    @Inject
    public DefaultConnectSchemaLocator(PluginRetrievalService pluginRetrievalService, ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getPrettySchemaForCurrentProduct() throws IOException
    {
        return getSchema(getFilterForCurrentProduct().name().toLowerCase(),PRETTY);
    }

    @Override
    public String getPrettySchema(ProductFilter productFilter) throws IOException
    {
        return getSchema(productFilter.name().toLowerCase(),PRETTY);
    }

    @Override
    public String getSchemaForCurrentProduct() throws IOException
    {
        return getSchema(getFilterForCurrentProduct().name().toLowerCase(),RAW);
    }
    
    @Override
    public String getShallowSchema() throws IOException
    {
        return getSchema("shallow", RAW);
    }
    
    @Override
    public String getSchema(ProductFilter productFilter) throws IOException
    {
        return getSchema(productFilter.name().toLowerCase(),RAW);
    }

    @Override
    public String getSchema(String schemaPrefix) throws IOException
    {
        return getSchema(schemaPrefix,RAW);
    }

    @Override
    public String getPrettySchema(String schemaPrefix) throws IOException
    {
        return getSchema(schemaPrefix,PRETTY);
    }

    private String getSchema(String schemaPrefix, String format) throws IOException
    {
        String path = String.format(format, schemaPrefix);
        InputStream in = plugin.getResourceAsStream(path);
                
        return IOUtils.toString(in);
    }

    private ProductFilter getFilterForCurrentProduct()
    {
        return ProductFilter.valueOf(applicationProperties.getDisplayName().toUpperCase());
    }
}
