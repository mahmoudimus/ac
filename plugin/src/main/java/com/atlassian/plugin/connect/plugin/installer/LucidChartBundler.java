package com.atlassian.plugin.connect.plugin.installer;

import java.io.IOException;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.sal.api.ApplicationProperties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class LucidChartBundler implements InitializingBean
{
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public LucidChartBundler(PluginController pluginController, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if(!applicationProperties.getDisplayName().equalsIgnoreCase("Confluence"))
        {
            return;
        }
        
        Plugin oldLucid = pluginAccessor.getPlugin("lucidchart-app");
        if(null != oldLucid)
        {
            pluginController.uninstall(oldLucid);
        }
        
        pluginController.installPlugins(getArtifact());
        
    }
    
    private PluginArtifact getArtifact()
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-lucidchart-app", new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", ClassLoaderUtils.getResourceAsStream("lucid-chart-plugin.xml",getClass()));
            }
        }));
    }
}
