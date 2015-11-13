package it.com.atlassian.plugin.connect.testlifecycle.util;

import com.atlassian.plugin.DefaultPluginArtifactFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginArtifactFactory;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class LifecyclePluginHelper
{

    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private PluginRetrievalService pluginRetrievalService;
    private PluginArtifactFactory pluginArtifactFactory = new DefaultPluginArtifactFactory();
    private Map<String, File> jarDependencies = Maps.newConcurrentMap();

    public LifecyclePluginHelper(PluginController pluginController,
            PluginAccessor pluginAccessor,
            PluginRetrievalService pluginRetrievalService)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    public Plugin installConnectPlugin() throws IOException, URISyntaxException
    {
        return installPlugin(getConnectPluginJarFilename());
    }

    public Plugin installGeneralReferencePlugin() throws IOException, URISyntaxException
    {
        return installPlugin(getGeneralReferencePluginJarFilename());
    }

    private Plugin installPlugin(String jarFilename) throws IOException, URISyntaxException
    {
        File jarFile = getJarFile(jarFilename);
        PluginArtifact artifact = pluginArtifactFactory.create(jarFile.toURI());
        String pluginKey = pluginController.installPlugins(artifact).iterator().next();
        return pluginAccessor.getPlugin(pluginKey);
    }

    private String getConnectPluginJarFilename() throws IOException
    {
        return getPluginDependencyFilename("atlassian-connect-plugin");
    }

    private String getGeneralReferencePluginJarFilename() throws IOException
    {
        return getPluginDependencyFilename("atlassian-connect-reference-plugin");
    }

    private String getPluginDependencyFilename(String artifactId) throws IOException
    {
        return String.format("/%s-%s.jar", artifactId, getPluginVersion());
    }

    private File getJarFile(String jarFilename)
    {
        return jarDependencies.computeIfAbsent(jarFilename, new java.util.function.Function<String, File>()
        {
            @Override
            public File apply(String filename)
            {
                try
                {
                    File tempFile = File.createTempFile(filename, ".jar");
                    URL jarResource = getClass().getResource("/" + filename);
                    FileUtils.copyURLToFile(jarResource, tempFile);
                    return tempFile;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private String getPluginVersion()
    {
        return pluginRetrievalService.getPlugin().getPluginInformation().getVersion();
    }
}
