package com.atlassian.plugin.remotable.container.service.sal;

import java.util.ResourceBundle;
import java.util.Locale;

public class ResourceBundleResolverImpl implements ResourceBundleResolver
{
    public ResourceBundle getBundle(String bundleName, Locale locale, ClassLoader classLoader)
    {
        return ResourceBundle.getBundle(bundleName, locale, classLoader);
    }
}
