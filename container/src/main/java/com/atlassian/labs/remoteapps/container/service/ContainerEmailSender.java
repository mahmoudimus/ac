package com.atlassian.labs.remoteapps.container.service;

import com.atlassian.labs.remoteapps.api.service.EmailSender;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.host.common.rest.RemoteEmail;
import com.atlassian.mail.Email;

import static com.atlassian.labs.remoteapps.container.util.JaxbJsonConverter.pojoToJson;

/**
 */
public class ContainerEmailSender implements EmailSender
{
    private final HostHttpClient hostHttpClient;

    public ContainerEmailSender(HostHttpClient hostHttpClient)
    {
        this.hostHttpClient = hostHttpClient;
    }

    @Override
    public void send(String userName, Email email)
    {
        send(userName, email, email.getBody(), email.getBody());
    }

    @Override
    public void send(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        RemoteEmail remoteEmail = new RemoteEmail(email);
        remoteEmail.setTo(userName);
        remoteEmail.setBodyAsHtml(bodyAsHtml);
        remoteEmail.setBodyAsText(bodyAsText);
        String body = pojoToJson(remoteEmail);
        hostHttpClient.newRequest("/rest/remoteapps/latest/email", "application/json", body).post();
    }

    @Override
    public void flush()
    {
        hostHttpClient.newRequest("/rest/remoteapps/latest/email/flush").get();
    }
}
