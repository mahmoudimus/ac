package com.atlassian.plugin.connect.core.util;

import org.osgi.framework.Bundle;

/**
 *
 */
public interface BundleLocator
{
    Bundle getBundle(String pluginKey);
}
