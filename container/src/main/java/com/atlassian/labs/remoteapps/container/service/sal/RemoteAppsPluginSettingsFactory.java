package com.atlassian.labs.remoteapps.container.service.sal;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This implementation can be backed by a file on the file system.
 *
 * Based off the plugin settings from sal.  Really needs to be redone.
 */
public class RemoteAppsPluginSettingsFactory implements PluginSettingsFactory
{
    private static final Logger log = Logger.getLogger(RemoteAppsPluginSettingsFactory.class);
    private final Properties properties;
    private final File file;

    public RemoteAppsPluginSettingsFactory()
    {
        File file = null;
        properties = new Properties();
        // Use refapp home directory
        File dataDir = new File(new File("."), "data");
        try
        {
            if (!dataDir.exists())
            {
                dataDir.mkdirs();
            }
            file = new File(dataDir, "pluginsettings.xml");
            if (!file.exists())
            {
                file.createNewFile();
            }
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Cannot create plugin settings xml file", ioe);
        }
        if (file.length() > 0)
        {
            file = load(file);
        }
        if (file != null)
        {
            // File is a new file
            log.debug("Using " + file.getAbsolutePath() + " as plugin settings store");
        }
        this.file = file;
    }

    private File load(File file)
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
            properties.loadFromXML(is);
        }
        catch (Exception e)
        {
            log.error("Error loading plugin settings properties, using memory store", e);
            file = null;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ioe)
                {
                    log.error("Error closing file", ioe);
                }
            }
        }
        return file;
    }

    public PluginSettings createSettingsForKey(String key)
    {
        if (key != null)
        {
            throw new UnsupportedOperationException();
        }
        return new RemoteAppsPluginSettings(new SettingsMap(key));
    }

    public PluginSettings createGlobalSettings()
    {
        return createSettingsForKey(null);
    }

    @SuppressWarnings("AccessToStaticFieldLockedOnInstance")
    private synchronized void store()
    {
        if (file == null || !file.canWrite())
        {
            // Read only settings
            return;
        }
        OutputStream os = null;
        try
        {
            os = new FileOutputStream(file);
            properties.storeToXML(os, "SAL Reference Implementation plugin settings");
        }
        catch (IOException ioe)
        {
            log.error("Error storing properties", ioe);
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException ioe)
                {
                    log.error("Error closing output stream", ioe);
                }
            }
        }
    }

    private class SettingsMap extends AbstractMap<String, String>
    {
        private final String settingsKey;

        private SettingsMap(String settingsKey)
        {
            if (settingsKey == null)
            {
                this.settingsKey = "global.";
            }
            else
            {
                this.settingsKey = "keys." + settingsKey + ".";
            }
        }

        public Set<Entry<String, String>> entrySet()
        {
            Set<Entry<String, String>> set = new HashSet<Entry<String, String>>();

            for(Entry entry:properties.entrySet())
            {
                set.add(entry);
            }

            return set;
        }

        public String get(Object key)
        {
            return properties.getProperty(settingsKey + key);
        }

        public String put(String key, String value)
        {
            String result = (String) properties.setProperty(settingsKey + key, value);
            store();
            return result;
        }

        public String remove(Object key)
        {
            String result = (String) properties.remove(settingsKey + key);
            store();
            return result;
        }
    }
}
