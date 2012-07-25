package com.atlassian.labs.remoteapps.apputils.ao;

import com.google.common.base.Function;
import org.osgi.framework.Bundle;

import static com.google.common.base.Preconditions.*;

class LoadClassFromBundleFunction implements Function<String, Class>
{
    private final Bundle bundle;

    LoadClassFromBundleFunction(Bundle bundle)
    {
        this.bundle = checkNotNull(bundle);
    }

    @Override
    public Class<?> apply(String className)
    {
        try
        {
            return bundle.loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("How did this happen? We're loading class '" + className + "'from the " + bundle, e);
        }
    }
}
