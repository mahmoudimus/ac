package com.atlassian.plugin.connect.api.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.I18nProperty;

/**
 * The interface for all capability modules that need to be marshalled from json
 */
public interface CapabilityBean
{
    String getKey();
    I18nProperty getName();
    I18nProperty getDescription();
}
