package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.sal.api.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class LucidChartBundler implements InitializingBean
{
    private static final String LUCIDCHART_KEY = "lucidchart-app";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EventPublisher eventPublisher;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public LucidChartBundler(EventPublisher eventPublisher, PluginController pluginController, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties)
    {
        this.eventPublisher = eventPublisher;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent e)
    {
        if(!"com.atlassian.plugins.atlassian-connect-plugin".equals(e.getPlugin().getKey()))
        {
            return;
        }

        if(!applicationProperties.getDisplayName().equalsIgnoreCase("Confluence"))
        {
            return;
        }

        Plugin oldLucid = pluginAccessor.getPlugin(LUCIDCHART_KEY);
        if(null != oldLucid)
        {
            pluginController.uninstall(oldLucid);
            logger.debug("uninstalled old lucid charts");
        }

        pluginController.installPlugins(getArtifact());
        logger.debug("installed new lucid charts");
    }
    
    private PluginArtifact getArtifact()
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-lucidchart-app", new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", ClassLoaderUtils.getResourceAsStream("lucid-chart-plugin.xml", getClass()));
            }
        }));
    }
}
