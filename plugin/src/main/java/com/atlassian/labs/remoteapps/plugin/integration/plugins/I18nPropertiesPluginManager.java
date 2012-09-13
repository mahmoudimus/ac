package com.atlassian.labs.remoteapps.plugin.integration.plugins;

import com.atlassian.labs.remoteapps.plugin.loader.StartableForPlugins;
import com.atlassian.plugin.*;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.atlassian.labs.remoteapps.host.common.util.BundleUtil.findBundleForPlugin;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Math.abs;

/**
 * This class loads a plugin called "remoteapps.18n", which solely exists to store and expose generated i18n files
 * from remote descriptors.  Ideally, the products would support generated i18n resources, but currently they expect
 * them to be declared via <resource> tags pointing at physical files in the plugin.  This plugin is a way to get
 * around that.
 */
@Component
public class I18nPropertiesPluginManager implements DisposableBean
{
    private static final String I18N_SYMBOLIC_NAME = "remoteapps.i18n";
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final BundleContext bundleContext;

    private volatile Bundle i18nBundle;
    private static final Logger log = LoggerFactory.getLogger(I18nPropertiesPluginManager.class);

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String,String> i18nToRegister = newHashMap();

    @Autowired
    public I18nPropertiesPluginManager(PluginAccessor accessor,
                                       PluginController pluginController,
                                       BundleContext bundleContext, StartableForPlugins startableForPlugins,
                                       PluginRetrievalService pluginRetrievalService
    )
    {
        this.bundleContext = bundleContext;
        this.pluginAccessor = accessor;
        this.pluginController = pluginController;
        startableForPlugins.register(pluginRetrievalService.getPlugin().getKey(), new Runnable()
        {
            @Override
            public void run()
            {
                loadBundle();
                registerI18n(i18nToRegister);
                i18nToRegister.clear();
                started.set(true);
            }
        });
    }

    private void forBundle(Bundle bundle, BundleManipulator manip)
    {
        ByteArrayOutputStream bout = null;
        try
        {
            bout = new ByteArrayOutputStream();
            ZipOutputStream zout = new ZipOutputStream(bout);
            Enumeration<URL> files = bundle.findEntries("/", "*.properties", false);
            if (files != null)
            {
                while(files.hasMoreElements())
                {
                    URL url = files.nextElement();
                    String name = url.getFile();
                    if (name.startsWith("/"))
                    {
                        name = name.substring(1);
                    }
                    if (manip.includeEntry(name))
                    {
                        zout.putNextEntry(new ZipEntry(name));
                        IOUtils.copy(url.openStream(), zout);
                    }
                }
            }
            manip.finish(bundle, zout);
            Manifest mf = new Manifest(bundle.getEntry("META-INF/MANIFEST.MF").openStream());
            zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            mf.write(zout);
            zout.close();

            File tmpFile = File.createTempFile(I18N_SYMBOLIC_NAME, ".jar");
            FileUtils.writeByteArrayToFile(tmpFile, bout.toByteArray());

            pluginController.installPlugins(new JarPluginArtifact(tmpFile));
            tmpFile.delete();
            i18nBundle = findBundleForPlugin(bundleContext, I18N_SYMBOLIC_NAME);
        }
        catch (IOException e)
        {
            log.warn("Unable to save i18n files", e);
        }
        finally
        {
            IOUtils.closeQuietly(bout);
        }
    }

//    public synchronized boolean remove(final String name)
//    {
////        forBundle(new BundleManipulator()
////        {
////
////            public boolean includeEntry(String entryName)
////            {
////                return !name.equals(entryName);
////            }
////
////            public void finish(Bundle bundle, ZipOutputStream zout){}
////        });
////        return true;
//        throw new UnsupportedOperationException();
//    }

    public synchronized String add(String pluginKey, Properties i18nProperties)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            i18nProperties.store(writer, pluginKey);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        final String data = writer.toString();
        final String name = String.valueOf(abs(writer.toString().hashCode()));
        if (!started.get())
        {
            i18nToRegister.put(name, data);
        }
        else
        {
            registerI18n(Collections.singletonMap(name, data));
        }
        return name;
    }

    private synchronized void registerI18n(final Map<String,String> i18n)
    {
        forBundle(i18nBundle, new BundleManipulator()
        {
            public boolean includeEntry(String entryName)
            {
                return !i18n.keySet().contains(entryName);
            }

            public void finish(Bundle bundle, ZipOutputStream zout) throws IOException
            {
                for (Map.Entry<String, String> entry : i18n.entrySet())
                {
                    zout.putNextEntry(new ZipEntry(entry.getKey() + ".properties"));
                    IOUtils.copy(new StringReader(entry.getValue()), zout, "UTF-8");
                }
            }
        });

        for (String name : i18n.keySet())
        {
            I18nModuleDescriptor descriptor = new I18nModuleDescriptor();
            descriptor.init(pluginAccessor.getPlugin(I18N_SYMBOLIC_NAME), DocumentHelper.createElement("i18n-something")
                                                                                        .addAttribute("key", name)
                                                                                        .addElement("resource")
                                                                                        .addAttribute("type", "i18n")
                                                                                        .addAttribute("name", "i18n")
                                                                                        .addAttribute("location", name)
                                                                                        .getParent());
            i18nBundle.getBundleContext().registerService(ModuleDescriptor.class.getName(), descriptor, null);
        }
    }

    private void loadBundle()
    {
        Plugin plugin = pluginAccessor.getPlugin(I18N_SYMBOLIC_NAME);
        if (plugin == null)
        {
            try
            {
                File tmpFile = File.createTempFile(I18N_SYMBOLIC_NAME, ".jar");
                ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmpFile));
                zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                Manifest mf = new Manifest();
                mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1");
                mf.getMainAttributes().putValue(Constants.BUNDLE_SYMBOLICNAME, I18N_SYMBOLIC_NAME);
                mf.getMainAttributes().putValue(OsgiPlugin.ATLASSIAN_PLUGIN_KEY, I18N_SYMBOLIC_NAME);
                mf.getMainAttributes().putValue(Constants.BUNDLE_VERSION, "1");
                mf.getMainAttributes().putValue(Constants.BUNDLE_DESCRIPTION, "I18n properties files for remote plugins");
                mf.getMainAttributes().putValue(Constants.BUNDLE_NAME, "RemoteApps I18n plugin");
                mf.getMainAttributes().putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
                mf.getMainAttributes().putValue("Spring-Context", "*");
                mf.write(zout);
                zout.close();
                pluginController.installPlugins(new JarPluginArtifact(tmpFile));
                tmpFile.delete();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to retrieve bundle", e);
            }
        }

        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles)
        {
            if (bundle.getSymbolicName().equals(I18N_SYMBOLIC_NAME))
            {
                i18nBundle = bundle;
                return;
            }
        }
        throw new IllegalStateException("The i18n bundle is not found");
    }

    @Override
    public void destroy() throws Exception
    {
        i18nBundle.uninstall();
    }

    private static interface BundleManipulator
    {
        boolean includeEntry(String name);
        void finish(Bundle bundle, ZipOutputStream zout) throws IOException;
    }
}
