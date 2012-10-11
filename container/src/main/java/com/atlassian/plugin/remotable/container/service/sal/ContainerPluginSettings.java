package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.sal.core.pluginsettings.AbstractStringPluginSettings;

import java.util.Map;

/**
 * Super simple plugin settings that uses a local XML file.
 *
 * TODO: change this to use a database
 */
public class ContainerPluginSettings extends AbstractStringPluginSettings
{
    private final Map<String,String> map;
    public ContainerPluginSettings(Map<String, String> map)
    {
        this.map = map;
    }

    protected void putActual(String key, String val)
    {
        map.put(key, XmlEncodingUtils.escape(val));
    }

    protected String getActual(String key)
    {
        return XmlEncodingUtils.unescape(map.get(key));
    }

    protected void removeActual(String key)
    {
        map.remove(key);
    }
}
