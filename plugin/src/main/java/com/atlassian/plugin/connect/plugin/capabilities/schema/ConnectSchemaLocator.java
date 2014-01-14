package com.atlassian.plugin.connect.plugin.capabilities.schema;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Named
public class ConnectSchemaLocator
{
    public static final String RAW = "/schema/%s-schema.json";
    public static final String PRETTY = "/schema/%s-schema-pretty.json";

    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;

    @Inject
    public ConnectSchemaLocator(PluginRetrievalService pluginRetrievalService, ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    public String getPrettySchemaForCurrentProduct() throws IOException
    {
        return getSchema(getFilterForCurrentProduct(),PRETTY);
    }

    public String getPrettySchema(ProductFilter productFilter) throws IOException
    {
        return getSchema(productFilter,PRETTY);
    }

    public String getSchemaForCurrentProduct() throws IOException
    {
        return getSchema(getFilterForCurrentProduct(),RAW);
    }
    
    public String getSchema(ProductFilter productFilter) throws IOException
    {
        return getSchema(productFilter,RAW);
    }

    private String getSchema(ProductFilter productFilter, String format) throws IOException
    {
        String path = String.format(format, productFilter.name().toLowerCase());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream in = plugin.getResourceAsStream(path);
                
        return IOUtils.toString(in);
    }

    private ProductFilter getFilterForCurrentProduct()
    {
        return ProductFilter.valueOf(applicationProperties.getDisplayName().toUpperCase());
    }
}
