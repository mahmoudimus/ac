package com.atlassian.labs.remoteapps.plugin.rest.email;

import com.atlassian.labs.remoteapps.host.common.rest.RemoteEmail;
import com.atlassian.labs.remoteapps.plugin.module.permission.ApiScopingFilter;
import com.atlassian.labs.remoteapps.plugin.service.LocalEmailSender;
import com.atlassian.labs.remoteapps.plugin.service.LocalEmailSenderServiceFactory;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 */
@Path("email")
public class EmailSenderResource
{
    private final LocalEmailSenderServiceFactory localEmailSender;

    public EmailSenderResource(LocalEmailSenderServiceFactory localEmailSender)
    {
        this.localEmailSender = localEmailSender;
    }

    @POST
    public Response sendEmail(@Context HttpServletRequest request, RemoteEmail remoteEmail)
    {
        LocalEmailSender sender = localEmailSender.getService(ApiScopingFilter.extractClientKey(request));
        sender.send(remoteEmail.getTo(), remoteEmail.toEmail(), remoteEmail.getBodyAsHtml(), remoteEmail.getBodyAsText());
        return Response.noContent().build();
    }

    @GET
    @Path("/flush")
    public Response flush(@Context HttpServletRequest request)
    {
        LocalEmailSender sender = localEmailSender.getService(ApiScopingFilter.extractClientKey(request));
        sender.flush();
        return Response.noContent().build();
    }
}
