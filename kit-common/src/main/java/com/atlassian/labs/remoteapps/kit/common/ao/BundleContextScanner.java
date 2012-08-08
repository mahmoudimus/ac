package com.atlassian.labs.remoteapps.kit.common.ao;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Enumeration;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Iterables.*;

final class BundleContextScanner
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    <T> Iterable<T> findClasses(BundleContext bundleContext, String packageName, Function<String, T> f, Predicate<T> p)
    {
        checkNotNull(bundleContext);
        checkNotNull(packageName);
        checkNotNull(f);
        checkNotNull(p);

        return filter(toIterable(getBundleEntries(bundleContext, packageName), f), p);
    }

    private Enumeration getBundleEntries(BundleContext bundleContext, String packageName)
    {
        log.debug("Scanning package '{}' of bundle {}", packageName, bundleContext.getBundle());
        return bundleContext.getBundle().findEntries(toFolder(packageName), "*.class", true);
    }

    private <T> Iterable<T> toIterable(Enumeration entries, Function<String, T> f)
    {
        final ImmutableList.Builder<T> classes = ImmutableList.builder();
        if (entries != null)
        {
            while (entries.hasMoreElements())
            {
                final String className = getClassName((URL) entries.nextElement());
                log.debug("Found class '{}'", className);
                classes.add(f.apply(className));
            }
        }
        return classes.build();
    }

    private String getClassName(URL url)
    {
        return getClassName(url.getFile());
    }

    private String getClassName(String file)
    {
        String className = file.substring(1);  // remove the leading /
        className = className.substring(0, className.lastIndexOf('.')); // remove the .class
        return className.replace('/', '.');
    }

    private String toFolder(String packageName)
    {
        return '/' + packageName.replace('.', '/');
    }
}
