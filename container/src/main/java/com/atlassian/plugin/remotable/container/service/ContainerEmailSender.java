package com.atlassian.plugin.remotable.container.service;

import com.atlassian.plugin.remotable.api.service.EmailSender;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.host.common.rest.RemoteEmail;
import com.atlassian.mail.Email;

import java.net.URI;

import static com.atlassian.plugin.remotable.container.util.JaxbJsonConverter.pojoToJson;

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
        hostHttpClient.newRequest(URI.create("/rest/remotable-plugins/latest/email"), "application/json", body).post();
    }

    @Override
    public void flush()
    {
        hostHttpClient.newRequest(URI.create("/rest/remotable-plugins/latest/email/flush")).get();
    }
}
