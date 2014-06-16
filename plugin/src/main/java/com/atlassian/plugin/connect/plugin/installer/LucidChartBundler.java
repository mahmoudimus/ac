package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.sal.api.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class LucidChartBundler implements InitializingBean, DisposableBean
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

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent e)
    {
        if (!ConnectPluginInfo.getPluginKey().equals(e.getPlugin().getKey()))
        {
            return;
        }

        if (!applicationProperties.getDisplayName().equalsIgnoreCase("Confluence"))
        {
            return;
        }

        pluginController.installPlugins(getArtifact());
        logger.debug("installed new lucid charts");
    }

    @XmlDescriptor
    private PluginArtifact getArtifact()
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-lucidchart-app", new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, ClassLoaderUtils.getResourceAsStream("lucid-chart-plugin.xml", getClass()));
            }
        }));
    }

}
