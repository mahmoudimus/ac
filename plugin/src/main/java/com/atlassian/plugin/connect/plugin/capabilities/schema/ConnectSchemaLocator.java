package com.atlassian.plugin.connect.plugin.capabilities.schema;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.ProductFilter;
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

    @Inject
    public ConnectSchemaLocator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    public String getPrettySchema(ProductFilter productFilter) throws IOException
    {
        return getSchema(productFilter,PRETTY);
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
}
