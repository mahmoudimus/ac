package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.remotable.plugin.loader.StartableForPlugins;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.atlassian.plugin.remotable.host.common.util.BundleUtil.findBundleWithName;
import static com.atlassian.plugin.remotable.host.common.util.BundleUtil.toBundleNames;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.abs;

/**
 * This class loads a plugin called "remotable.plugins.i18n", which solely exists to store and expose generated i18n files
 * from remote descriptors.  Ideally, the products would support generated i18n resources, but currently they expect
 * them to be declared via <resource> tags pointing at physical files in the plugin.  This plugin is a way to get
 * around that.
 */
@Component
public final class I18nPropertiesPluginManager
{
    private static final Logger log = LoggerFactory.getLogger(I18nPropertiesPluginManager.class);
    private static final String I18N_SYMBOLIC_NAME = "remotable.plugins.i18n";

    private final ModuleFactory moduleFactory;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final BundleContext bundleContext;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, String> i18nToRegister = CopyOnWriteMap.newHashMap();

    private volatile Bundle i18nBundle;

    @Autowired
    public I18nPropertiesPluginManager(ModuleFactory moduleFactory,
                                       PluginAccessor accessor,
                                       PluginController pluginController,
                                       BundleContext bundleContext, StartableForPlugins startableForPlugins,
                                       PluginRetrievalService pluginRetrievalService
    )
    {
        this.moduleFactory = checkNotNull(moduleFactory);
        this.bundleContext = checkNotNull(bundleContext);
        this.pluginAccessor = checkNotNull(accessor);
        this.pluginController = checkNotNull(pluginController);
        startableForPlugins.register(pluginRetrievalService.getPlugin().getKey(), new Runnable()
        {
            @Override
            public void run()
            {
                loadBundle();
                registerI18n(i18nToRegister);
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
                while (files.hasMoreElements())
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
            setI18nBundle();
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
        i18nToRegister.put(name, data);
        if (started.get())
        {
            registerI18n(i18nToRegister);
        }
        return name;
    }

    private synchronized void registerI18n(final Map<String, String> i18n)
    {
        forBundle(i18nBundle, new BundleManipulator()
        {
            public boolean includeEntry(String entryName)
            {
                return !i18n.keySet().contains(entryName.substring(0, entryName.length() - ".properties".length()));
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
            I18nModuleDescriptor descriptor = new I18nModuleDescriptor(moduleFactory);
            descriptor.init(findI18nPlugin(), DocumentHelper.createElement("i18n-something")
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
        if (findI18nPlugin() == null)
        {
            createAndInstallI18nPlugin();
        }

        ensureI18nPluginIsEnabled();
        setI18nBundle();
    }

    private Plugin findI18nPlugin()
    {
        return pluginAccessor.getPlugin(I18N_SYMBOLIC_NAME);
    }

    private Plugin createAndInstallI18nPlugin()
    {
        final File i18nPluginTempFile = createI18nPluginTempFile();
        pluginController.installPlugins(new JarPluginArtifact(i18nPluginTempFile));

        if (!i18nPluginTempFile.delete())
        {
            log.warn("Could not delete i18n temp file at '{}'", i18nPluginTempFile);
        }

        return findI18nPlugin();
    }

    private File createI18nPluginTempFile()
    {
        try
        {
            final File tmpFile = File.createTempFile(I18N_SYMBOLIC_NAME, ".jar");

            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmpFile));
            addManifest(zout);
            zout.close();
            return tmpFile;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not create " + I18N_SYMBOLIC_NAME + " plugin temp jar file.", e);
        }
    }

    private void addManifest(ZipOutputStream zout) throws IOException
    {
        zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        final Manifest mf = createI18nPluginManifest();
        mf.write(zout);
    }

    private Manifest createI18nPluginManifest()
    {
        final Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1");
        mf.getMainAttributes().putValue(Constants.BUNDLE_SYMBOLICNAME, I18N_SYMBOLIC_NAME);
        mf.getMainAttributes().putValue(OsgiPlugin.ATLASSIAN_PLUGIN_KEY, I18N_SYMBOLIC_NAME);
        mf.getMainAttributes().putValue(Constants.BUNDLE_VERSION, "1");
        mf.getMainAttributes().putValue(Constants.BUNDLE_DESCRIPTION, "I18n properties files for remote plugins");
        mf.getMainAttributes().putValue(Constants.BUNDLE_NAME, "Remotable Plugins I18n plugin");
        mf.getMainAttributes().putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
        mf.getMainAttributes().putValue("Spring-Context", "*");

        // forces a dependency between the i18n plugin and the remotable plugin, so that disabling the latter disables the former
        mf.getMainAttributes().putValue(Constants.IMPORT_PACKAGE, "com.atlassian.plugin.remotable.api");

        return mf;
    }

    private void setI18nBundle()
    {
        this.i18nBundle = findBundleWithName(bundleContext, I18N_SYMBOLIC_NAME);
        if (i18nBundle == null)
        {
            throw new IllegalStateException("The i18n bundle (" + I18N_SYMBOLIC_NAME + ") was not found amongst bundles: " + toBundleNames(bundleContext.getBundles()));
        }
    }

    private void ensureI18nPluginIsEnabled()
    {
        pluginController.enablePlugins(I18N_SYMBOLIC_NAME);
    }

    private static interface BundleManipulator
    {
        boolean includeEntry(String name);

        void finish(Bundle bundle, ZipOutputStream zout) throws IOException;
    }
}
