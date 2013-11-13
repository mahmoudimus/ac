package com.atlassian.plugin.connect.plugin.rest.email;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.plugin.connect.plugin.module.permission.LegacyApiScopingFilter;
import com.atlassian.plugin.connect.plugin.service.LocalEmailSender;

@Path("email")
public class EmailSenderResource
{
    private final LocalEmailSender localEmailSender;

    public EmailSenderResource(LocalEmailSender localEmailSender)
    {
        this.localEmailSender = localEmailSender;
    }

    @POST
    public Response sendEmail(@Context javax.servlet.http.HttpServletRequest request, RemoteEmail remoteEmail)
    {
        localEmailSender.send(LegacyApiScopingFilter.extractClientKey(request), remoteEmail.getTo(), remoteEmail.toEmail(), remoteEmail.getBodyAsHtml(), remoteEmail.getBodyAsText());
        return Response.noContent().build();
    }

    @GET
    @Path("/flush")
    public Response flush(@Context javax.servlet.http.HttpServletRequest request)
    {
        localEmailSender.flush(LegacyApiScopingFilter.extractClientKey(request));
        return Response.noContent().build();
    }
}
