package com.atlassian.labs.remoteapps.api.service;

import com.atlassian.mail.Email;

/**
 * Sends an email via the Atlassian product
 */
public interface EmailSender
{
    void send(String userName, Email email);
    void send(String userName, Email email, String bodyAsHtml, String bodyAsText);
}
