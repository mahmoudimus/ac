package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import com.google.common.collect.ImmutableList;

/**
 * Represents the paired URLs needed to support Addon content in an iFrame.
 * One URL is for the addon itself. The other for the host application that will sign the addon URL and set it as the iframe src
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
     * The URLTemplate for the addons content
     */
    public UrlTemplate getAddonUrlTemplate()
    {
        return addonUrlTemplate;
    }

    /**
     * The URLTemplate and servlet paths for the signing service on the host application
     */
    public HostUrlPaths getHostUrlPaths()
    {
        return hostUrlPaths;
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

        public UrlTemplate getHostUrlTemplate()
        {
            return hostUrlTemplate;
        }

        public Iterable<String> getServletRegistrationPaths()
        {
            return servletRegistrationPaths;
        }
    }
}