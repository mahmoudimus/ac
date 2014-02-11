package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPluginXmlFactory;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAddOnBundleBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.base.Strings;
import org.dom4j.Document;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates plugin artifacts for plugins installed in remote mode
 */
@Component
public class RemotePluginArtifactFactory
{
    private static final String ATLASSIAN_PLUGIN_KEY = "Atlassian-Plugin-Key";
    private final ConnectPluginXmlFactory pluginXmlFactory;
    private final BundleContext bundleContext;
    private final ContainerManagedPlugin theConnectPlugin;

    public static String CLEAN_FILENAME_PATTERN = "[:\\\\/*?|<> _]";

    @Autowired
    public RemotePluginArtifactFactory(ConnectPluginXmlFactory pluginXmlFactory, BundleContext bundleContext, PluginRetrievalService pluginRetrievalService)
    {
        this.pluginXmlFactory = pluginXmlFactory;
        this.bundleContext = bundleContext;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
    }

    public PluginArtifact create(final Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        changeDescriptorToIncludeRemotePluginHeader(document, username);

        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + pluginKey, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, document);
                //builder.addFile(Filenames.ADDON_IMPORTS_XML,theConnectPlugin.getResourceAsStream(Filenames.ADDON_IMPORTS_RESOURCE));
            }
        }));
    }

    private void changeDescriptorToIncludeRemotePluginHeader(Document document, String username)
    {
        // fixme: plugin osgi manifest generator should respect existing entries, but it currently just blows everything away,
        // so we have to store the values in the plugin descriptor instead
        document.getRootElement()
                .element("plugin-info")
                .addElement("bundle-instructions")
                .addElement(ConnectAddOnIdentifierService.REMOTE_PLUGIN)
                .addText("installer;user=\"" + username + "\";date=\"" + System.currentTimeMillis() + "\"");
    }

    public PluginArtifact create(ConnectAddonBean addOn, String username) throws IOException
    {
        ConnectAddOnBundleBuilder builder = new ConnectAddOnBundleBuilder();

        //create a proper manifest
        builder.manifest(createManifest(addOn, username));

        //create the plugin.xml
        builder.addResource(Filenames.ATLASSIAN_PLUGIN_XML, pluginXmlFactory.createPluginXml(addOn));
        builder.addResource(Filenames.ATLASSIAN_ADD_ON_JSON, ConnectModulesGsonFactory.getGson().toJson(addOn));
        //builder.addResource(Filenames.ADDON_IMPORTS_XML, theConnectPlugin.getResourceAsStream(Filenames.ADDON_IMPORTS_RESOURCE));

        return new JarPluginArtifact(builder.build(addOn.getKey().replaceAll(CLEAN_FILENAME_PATTERN, "-").toLowerCase()));
    }

    private Map<String, String> createManifest(ConnectAddonBean addOn, String username)
    {
        Map<String, String> manifest = new HashMap<String, String>();
        manifest.put(ATLASSIAN_PLUGIN_KEY, addOn.getKey());
        manifest.put(Constants.BUNDLE_SYMBOLICNAME, addOn.getKey());
        manifest.put(Constants.BUNDLE_VERSION, addOn.getVersion());
        manifest.put(Constants.BUNDLE_CLASSPATH, ".");
        manifest.put("Spring-Context", "*");
        manifest.put(ConnectAddOnIdentifierService.CONNECT_ADDON_HEADER, "down with P2");

        if (null != addOn.getVendor())
        {
            if (!Strings.isNullOrEmpty(addOn.getVendor().getName()))
            {
                manifest.put(Constants.BUNDLE_VENDOR, addOn.getVendor().getName());
            }
            if (!Strings.isNullOrEmpty(addOn.getVendor().getUrl()))
            {
                manifest.put(Constants.BUNDLE_DOCURL, addOn.getVendor().getUrl());
            }
        }

        return manifest;
    }


}
