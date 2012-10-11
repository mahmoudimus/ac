package com.atlassian.plugin.remotable.container.internal;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.google.common.base.Objects;
import org.osgi.framework.Bundle;

import static com.google.common.base.Preconditions.*;

public final class BundleKey
{
    public final Bundle bundle;
    public final String pluginKey;

    public BundleKey(Bundle bundle)
    {
        this.bundle = checkNotNull(bundle);
        this.pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final BundleKey other = (BundleKey) obj;

        return Objects.equal(this.pluginKey, other.pluginKey);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(pluginKey);
    }
}
