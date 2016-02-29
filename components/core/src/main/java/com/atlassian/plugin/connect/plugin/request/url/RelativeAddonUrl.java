package com.atlassian.plugin.connect.plugin.request.url;

import com.atlassian.uri.Uri;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;

/**
 * A holder for a URL targeting a local servlet that is registered on behalf of a remote addon.
 */
public class RelativeAddonUrl {

    private static String PLUGINS_SERVLET_PREFIX = "/plugins/servlet";

    private final String relativeUrl;
    private final String servletDescriptorUrl;

    public RelativeAddonUrl(@Nonnull Uri relativeUri) {
        this.relativeUrl = PLUGINS_SERVLET_PREFIX + ensureLeadingSlash(relativeUri.toString());
        this.servletDescriptorUrl = ensureLeadingSlash(relativeUri.getPath());
    }

    /**
     * Use this for URIs with variable placeholders to avoid them being url encoded
     *
     * @param relativeUri the relative URI string
     */
    public RelativeAddonUrl(@Nonnull String relativeUri) {
        String path = Iterables.getFirst(Splitter.on('?').split(relativeUri), relativeUri);
        this.relativeUrl = PLUGINS_SERVLET_PREFIX + ensureLeadingSlash(relativeUri);
        this.servletDescriptorUrl = ensureLeadingSlash(path);
    }

    /**
     * @return a URL targeting a local servlet that is registered on behalf of a remote addon.
     */
    public String getRelativeUri() {
        return relativeUrl;
    }

    /**
     * @return a URL suitable for use as the value of the &lt;url-pattern&gt; of a servlet capable of servicing requests
     *         targeting {@link #getRelativeUri()}. This differs slightly from the result of {@link #getRelativeUri()} as it is
     *         not prefixed by {@link #PLUGINS_SERVLET_PREFIX}, which is added automatically by the plugin system.
     */
    public String getServletDescriptorUrl() {
        return servletDescriptorUrl;
    }

    private String ensureLeadingSlash(String s) {
        return s.startsWith("/") ? s : "/" + s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelativeAddonUrl that = (RelativeAddonUrl) o;

        if (!relativeUrl.equals(that.relativeUrl)) {
            return false;
        }
        return servletDescriptorUrl.equals(that.servletDescriptorUrl);

    }

    @Override
    public int hashCode() {
        int result = relativeUrl.hashCode();
        result = 31 * result + servletDescriptorUrl.hashCode();
        return result;
    }
}
