package com.atlassian.plugin.connect.plugin;

public class ConnectPluginInfo
{
    public static final String PLUGIN_KEY = "${project.groupId}.${project.artifactId}";
    public static final String PLUGIN_VERSION = "${project.version}";
    public static final String BUILD_NUMBER = "${buildNumber}";
}
