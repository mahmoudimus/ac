package com.atlassian.plugin.remotable.plugin.rest.license;

import com.atlassian.plugin.remotable.plugin.license.LicenseRetriever;
import com.atlassian.plugin.remotable.plugin.module.permission.ApiScopingFilter;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * fixme: mostly copied from UPM master at 75cee855ebd6475a3e7d9b619694e613c8906f09
 *
 * Remove this once UPM supports this rest resource
 */
@Path("license")
public class LicenseResource
{
    private final LicenseRetriever licenseRetriever;

    public LicenseResource(final LicenseRetriever licenseRetriever)
    {
        this.licenseRetriever = licenseRetriever;
    }

    @GET
    public Response getLicense(@Context HttpServletRequest request)
    {
        String pluginKey = ApiScopingFilter.extractClientKey(request);
        Option<PluginLicense> license = licenseRetriever.getLicense(pluginKey);
        if (license.isDefined())
        {
            return Response.ok(LicenseDetailsFactory.createRemotablePluginLicense(license.get())).build();
        }
        else
        {
            return Response.status(404).build();
        }
    }
}
