package com.atlassian.labs.remoteapps.container.service.sal;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Encapsulates a strategy for looking up a {@link ResourceBundle} from a {@link ClassLoader} given the bundle name
 * and a locale.
 *
 * <b>NOTE:</b> This is not part of the public SAL API, but an internal interface used by the reference implementation,
 * primarily to allow {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)} to be mocked in tests.
 *
 * @since 2.0
 */
public interface ResourceBundleResolver
{
    /**
     * Gets a resource bundle using the specified base name, locale, and class loader.  This obeys the same general
     * contract as {@link ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)}.
     * 
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @param classLoader the class loader from which to load the resource bundle
     * @return a resource bundle for the given base name and locale
     * @throws NullPointerException if {@code baseName}, {@code locale}, or {@code loader} is {@code null}
     * @throws java.util.MissingResourceException if no resource bundle for the specified base name can be found 
     * @see ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
     */
    ResourceBundle getBundle(String baseName, Locale locale, ClassLoader classLoader);
}
