package com.atlassian.labs.remoteapps.apputils.spring.properties;


import java.util.Map;

/**
 * A simple interface to load properties as {@link Map maps of String}.
 */
public interface PropertiesLoader
{
    Map<String, String> load();
}
