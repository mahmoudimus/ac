package com.atlassian.plugin.connect.plugin.rest.license;

import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.scopes.ApiScopingFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    @AnonymousAllowed
    @Produces("application/json")
    public Response getLicense(@Context javax.servlet.http.HttpServletRequest request)
    {
        String pluginKey = ApiScopingFilter.extractClientKey(request);
        if (pluginKey == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Requests to this resource must be authenticated by an add-on.")
                           .build();
        }

        Option<PluginLicense> license = licenseRetriever.getLicense(pluginKey);
        if (license.isDefined())
        {
            Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));

            return Response.ok(LicenseDetailsFactory.createRemotablePluginLicense(license.get()))
                    .expires(expirationDate)
                    .build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
