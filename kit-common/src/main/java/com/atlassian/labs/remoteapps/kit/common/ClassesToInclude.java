package com.atlassian.labs.remoteapps.kit.common;

import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.spi.modules.ModuleMarker;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * This class exists solely to force the plugin installation process to add package imports on
 * the packages of these classes when it scans the bytecode of the plugin contents.
 */
public class ClassesToInclude
{
    private static final Class[] CLASSES_TO_FORCE_PACKAGE_IMPORTS = new Class[]{
            PluginSettingsFactory.class,
            PluginRetrievalService.class,
            DescriptorGenerator.class,
            SignedRequestHandler.class,
            ModuleMarker.class,
            SearchRequestView.class
    };
}
