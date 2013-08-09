package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.atlassian.plugin.*;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
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

import static com.atlassian.plugin.connect.plugin.util.BundleUtil.findBundleWithName;
import static com.atlassian.plugin.connect.plugin.util.BundleUtil.toBundleNames;
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
    public static final String I18N_SYMBOLIC_NAME = "atlassian-connect.i18n";

    private final ModuleFactory moduleFactory;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final BundleContext bundleContext;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, String> i18nToRegister = CopyOnWriteMap.newHashMap();

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
                initI18nPlugin();
                registerI18n(i18nToRegister);
                started.set(true);
            }
        });
    }

    /**
     * Lookup the currently installed remotable plugins i18n bundle and process it with the supplied {@link BundleManipulator}
     */
    private void updateI18nBundle(BundleManipulator bundleManipulator)
    {
        Bundle i18nBundle = lookupI18nBundle();
        ByteArrayOutputStream bout = null;
        try
        {
            bout = new ByteArrayOutputStream();
            ZipOutputStream zout = new ZipOutputStream(bout);
            Enumeration<URL> files = i18nBundle.findEntries("/", "*.properties", false);
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
                    if (bundleManipulator.includeEntry(name))
                    {
                        zout.putNextEntry(new ZipEntry(name));
                        IOUtils.copy(url.openStream(), zout);
                    }
                }
            }
            bundleManipulator.finish(i18nBundle, zout);
            Manifest mf = new Manifest(i18nBundle.getEntry("META-INF/MANIFEST.MF").openStream());
            zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            mf.write(zout);
            zout.close();

            File tmpFile = File.createTempFile(I18N_SYMBOLIC_NAME, ".jar");
            FileUtils.writeByteArrayToFile(tmpFile, bout.toByteArray());

            pluginController.installPlugins(new JarPluginArtifact(tmpFile));
            tmpFile.delete();
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

    /**
     * Register i18n properties for a newly installed plugin.
     */
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

    /**
     * Add a map of i18n keys to the currently installed remotable plugins i18n bundle.
     */
    private synchronized void registerI18n(final Map<String, String> i18n)
    {
        updateI18nBundle(new BundleManipulator() {
            public boolean includeEntry(String entryName) {
                return !i18n.keySet().contains(entryName.substring(0, entryName.length() - ".properties".length()));
            }

            public void finish(Bundle bundle, ZipOutputStream zout) throws IOException {
                for (Map.Entry<String, String> entry : i18n.entrySet()) {
                    zout.putNextEntry(new ZipEntry(entry.getKey() + ".properties"));
                    IOUtils.copy(new StringReader(entry.getValue()), zout, "UTF-8");
                }
            }
        });

        // look up the i18nBundle fresh to ensure we have an installed, enabled version of the bundle. The
        // updateI18nBundle operation above causes the plugin system to scan for new plugins.
        Bundle i18nBundle = lookupI18nBundle();
        for (String name : i18n.keySet())
        {
            I18nModuleDescriptor descriptor = new I18nModuleDescriptor(moduleFactory);
            descriptor.init(findI18nPlugin(), DocumentHelper.createElement("i18n-something")
                    .addAttribute("key", name)
                    .addAttribute("system", "true")
                    .addElement("resource")
                    .addAttribute("type", "i18n")
                    .addAttribute("name", "i18n")
                    .addAttribute("location", name)
                    .getParent());
            i18nBundle.getBundleContext().registerService(ModuleDescriptor.class.getName(), descriptor, null);
        }
    }

    private void initI18nPlugin()
    {
        if (findI18nPlugin() == null)
        {
            createAndInstallI18nPlugin();
        }
        ensureI18nPluginIsEnabled();
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
        mf.getMainAttributes().putValue(Constants.IMPORT_PACKAGE, "com.atlassian.plugin.connect.api*");

        return mf;
    }

    /**
     * @return the currently installed remotable plugins i18n bundle after ensuring it is enabled.
     */
    private Bundle lookupI18nBundle()
    {
        Bundle i18nBundle = findBundleWithName(bundleContext, I18N_SYMBOLIC_NAME);
        if (i18nBundle == null)
        {
            throw new IllegalStateException("The i18n bundle (" + I18N_SYMBOLIC_NAME + ") was not found amongst bundles: " + toBundleNames(bundleContext.getBundles()));
        }
        ensureI18nPluginIsEnabled();
        return i18nBundle;
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
