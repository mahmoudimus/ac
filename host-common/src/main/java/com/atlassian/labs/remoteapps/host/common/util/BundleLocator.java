package com.atlassian.labs.remoteapps.host.common.util;

import org.osgi.framework.Bundle;

/**
 *
 */
public interface BundleLocator
{
    Bundle getBundle(String pluginKey);
}
