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

    @Inject
    public DefaultConnectSchemaLocator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }
    
    @Override
    public String getShallowSchema() throws IOException
    {
        return getSchema("shallow", RAW);
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
}
