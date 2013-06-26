package com.atlassian.plugin.remotable.api.service;

import com.atlassian.mail.Email;

/**
 * Sends an email via the Atlassian product
 */
public interface EmailSender
{
    void send(String userName, Email email);
    void send(String userName, Email email, String bodyAsHtml, String bodyAsText);
    void flush();
}
