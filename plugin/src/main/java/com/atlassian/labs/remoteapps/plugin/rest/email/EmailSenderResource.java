package com.atlassian.labs.remoteapps.plugin.rest.email;

import com.atlassian.labs.remoteapps.host.common.rest.RemoteEmail;
import com.atlassian.labs.remoteapps.plugin.service.LocalEmailSender;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 */
@Path("email")
public class EmailSenderResource
{
    private final LocalEmailSender localEmailSender;

    public EmailSenderResource(LocalEmailSender localEmailSender)
    {
        this.localEmailSender = localEmailSender;
    }

    @POST
    public Response sendEmail(RemoteEmail remoteEmail)
    {
        localEmailSender.send(remoteEmail.getTo(), remoteEmail.toEmail(), remoteEmail.getBodyAsHtml(), remoteEmail.getBodyAsText());
        return Response.noContent().build();
    }
}
