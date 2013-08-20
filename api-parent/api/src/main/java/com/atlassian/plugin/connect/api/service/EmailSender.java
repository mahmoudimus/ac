package com.atlassian.plugin.connect.api.service;

import com.atlassian.mail.Email;

/**
 * Sends an email via the Atlassian product
 */
public interface EmailSender
{
    void send(String pluginKey, String userName, Email email);

    void send(String pluginKey, String userName, Email email, String bodyAsHtml, String bodyAsText);

    void flush(String pluginKey);
}
