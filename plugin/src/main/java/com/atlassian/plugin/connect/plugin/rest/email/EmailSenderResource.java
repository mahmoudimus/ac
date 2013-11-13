package com.atlassian.plugin.connect.plugin.rest.email;

import com.atlassian.plugin.connect.plugin.module.permission.RequestAddOnKeyLabeler;
import com.atlassian.plugin.connect.plugin.service.LocalEmailSender;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("email")
public class EmailSenderResource
{
    private final LocalEmailSender localEmailSender;
    private final RequestAddOnKeyLabeler requestAddOnKeyLabeler;

    public EmailSenderResource(LocalEmailSender localEmailSender, RequestAddOnKeyLabeler requestAddOnKeyLabeler)
    {
        this.localEmailSender = localEmailSender;
        this.requestAddOnKeyLabeler = checkNotNull(requestAddOnKeyLabeler);
    }

    @POST
    public Response sendEmail(@Context javax.servlet.http.HttpServletRequest request, RemoteEmail remoteEmail)
    {
        localEmailSender.send(requestAddOnKeyLabeler.getAddOnKey(request), remoteEmail.getTo(), remoteEmail.toEmail(), remoteEmail.getBodyAsHtml(), remoteEmail.getBodyAsText());
        return Response.noContent().build();
    }

    @GET
    @Path("/flush")
    public Response flush(@Context javax.servlet.http.HttpServletRequest request)
    {
        localEmailSender.flush(requestAddOnKeyLabeler.getAddOnKey(request));
        return Response.noContent().build();
    }
}
