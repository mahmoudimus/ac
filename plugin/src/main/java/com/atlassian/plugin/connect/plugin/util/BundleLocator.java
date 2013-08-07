package com.atlassian.plugin.connect.plugin.util;

import org.osgi.framework.Bundle;

/**
 *
 */
public interface BundleLocator
{
    Bundle getBundle(String pluginKey);
}
