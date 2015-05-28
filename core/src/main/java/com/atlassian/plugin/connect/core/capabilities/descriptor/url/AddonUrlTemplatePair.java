package com.atlassian.plugin.connect.core.capabilities.descriptor.url;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Represents the pair of URLs needed to support Addon content in an iFrame.
 * One URL is for the addon itself. The other for the host application servlet that will sign the addon URL and set it as the iframe src
 */
public class AddonUrlTemplatePair
{
    private final UrlTemplate addonUrlTemplate;
    private final HostUrlPaths hostUrlPaths;

    public AddonUrlTemplatePair(String urlTemplateStr, String pluginKey)
    {
        addonUrlTemplate = new SignedUrlTemplate(urlTemplateStr);

        RelativeAddOnUrl relativeAddOnUrl = new RelativeAddOnUrlConverter().addOnUrlToLocalServletUrl(pluginKey, urlTemplateStr);
        String servletDescriptorUrl = relativeAddOnUrl.getServletDescriptorUrl();

        hostUrlPaths = new HostUrlPaths(new UrlTemplate(relativeAddOnUrl.getRelativeUri()),
                ImmutableList.<String>of(servletDescriptorUrl, servletDescriptorUrl + "/*"));
    }


    /**
     * @return the URLTemplate for the addons content
     */
    public UrlTemplate getAddonUrlTemplate()
    {
        return addonUrlTemplate;
    }

    /**
     * @return the URLTemplate and servlet paths for the signing service on the host application
     */
    public HostUrlPaths getHostUrlPaths()
    {
        return hostUrlPaths;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }
        AddonUrlTemplatePair rhs = (AddonUrlTemplatePair) obj;
        return new EqualsBuilder()
                .append(addonUrlTemplate, rhs.addonUrlTemplate)
                .append(hostUrlPaths, rhs.getHostUrlPaths())
                .isEquals();
    }

    /**
     * The URL template for the host application plus the corresponding servlet paths that will be registered to support it
     */
    public static class HostUrlPaths
    {
        private final UrlTemplate hostUrlTemplate;
        private final Iterable<String> servletRegistrationPaths;

        private HostUrlPaths(UrlTemplate hostUrlTemplate, Iterable<String> servletRegistrationPaths)
        {
            this.hostUrlTemplate = hostUrlTemplate;
            this.servletRegistrationPaths = servletRegistrationPaths;
        }

        /**
         * @return the URLTemplate for the host signing servlet
         */
        public UrlTemplate getHostUrlTemplate()
        {
            return hostUrlTemplate;
        }

        /**
         * @return the servlet paths to register
         */
        public Iterable<String> getServletRegistrationPaths()
        {
            return servletRegistrationPaths;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            if (obj.getClass() != getClass())
            {
                return false;
            }
            HostUrlPaths rhs = (HostUrlPaths) obj;
            return ObjectUtils.equals(hostUrlTemplate, rhs.getHostUrlTemplate()) &&
                    Iterables.elementsEqual(servletRegistrationPaths, rhs.getServletRegistrationPaths());
        }
    }
}