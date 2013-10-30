package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * A holder for a URL that targets a local servlet that is registered on behalf of a remote addon.
 */
public class RelativeAddOnUrl
{

    private static String PLUGINS_SERVLET_PREFIX = "/plugins/servlet";

    private final String relativeUrl;

    public RelativeAddOnUrl(@Nonnull String relativeUrl)
    {
        this.relativeUrl = Preconditions.checkNotNull(relativeUrl);
    }

    /**
     * @return a URL targeting a local servlet that is registered on behalf of a remote addon.
     */
    public String getRelativeUrl()
    {
        return PLUGINS_SERVLET_PREFIX + ensureLeadingSlash(relativeUrl);
    }

    /**
     * @return a URL suitable for use as the value of the &lt;url-pattern&gt; of a servlet capable of servicing requests
     *         targeting {@link #getRelativeUrl()}. This differs slightly from the result of {@link #getRelativeUrl()} as it is
     *         not prefixed by {@link #PLUGINS_SERVLET_PREFIX}, which is added automatically by the plugin system.
     */
    public String getServletDescriptorUrl()
    {
        return relativeUrl;
    }

    private String ensureLeadingSlash(String s)
    {
        return s.startsWith("/") ? s : "/" + s;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RelativeAddOnUrl that = (RelativeAddOnUrl) o;

        if (!relativeUrl.equals(that.relativeUrl))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return relativeUrl.hashCode();
    }
}
