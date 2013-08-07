package com.atlassian.plugin.connect.plugin.rest.email;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.plugin.connect.plugin.rest.email.RemoteEmail;
import com.atlassian.plugin.connect.plugin.module.permission.ApiScopingFilter;
import com.atlassian.plugin.connect.plugin.service.LocalEmailSender;
import com.atlassian.plugin.connect.plugin.service.LocalEmailSenderServiceFactory;

@Path("email")
public class EmailSenderResource
{
    private final LocalEmailSenderServiceFactory localEmailSender;

    public EmailSenderResource(LocalEmailSenderServiceFactory localEmailSender)
    {
        this.localEmailSender = localEmailSender;
    }

    @POST
    public Response sendEmail(@Context javax.servlet.http.HttpServletRequest request, RemoteEmail remoteEmail)
    {
        LocalEmailSender sender = localEmailSender.getService(ApiScopingFilter.extractClientKey(request));
        sender.send(remoteEmail.getTo(), remoteEmail.toEmail(), remoteEmail.getBodyAsHtml(), remoteEmail.getBodyAsText());
        return Response.noContent().build();
    }

    @GET
    @Path("/flush")
    public Response flush(@Context javax.servlet.http.HttpServletRequest request)
    {
        LocalEmailSender sender = localEmailSender.getService(ApiScopingFilter.extractClientKey(request));
        sender.flush();
        return Response.noContent().build();
    }
}
