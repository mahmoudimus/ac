package com.atlassian.plugin.connect.plugin;

public class ConnectPluginInfo
{
    private static final String PLUGIN_KEY = "${project.groupId}.${project.artifactId}";
    private static final String PLUGIN_VERSION = "${project.version}";
    private static final String BUILD_NUMBER = "${buildNumber}";
    
    public static String getPluginKey()
    {
        //we need this for local testing
        if(PLUGIN_KEY.contains("project.groupId"))
        {
            return "com.atlassian.plugins.atlassian-connect-plugin";
        }
        
        return PLUGIN_KEY;
    }
    
    public static String getPluginVersion()
    {
        if(PLUGIN_VERSION.contains("project.version"))
        {
            return "0.0";
        }
        
        return PLUGIN_VERSION;
    }
    
    public static String getBuildNumber()
    {
        if(BUILD_NUMBER.contains("buildNumber"))
        {
            return "0";
        }
        
        return BUILD_NUMBER;
    }
}
