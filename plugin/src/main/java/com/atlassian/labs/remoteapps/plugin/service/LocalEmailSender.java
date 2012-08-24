package com.atlassian.labs.remoteapps.plugin.service;

import com.atlassian.labs.remoteapps.api.service.EmailSender;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;

/**
 */
public class LocalEmailSender implements EmailSender
{
    private final ProductAccessor productAccessor;

    public LocalEmailSender(ProductAccessor productAccessor)
    {
        this.productAccessor = productAccessor;
    }

    @Override
    public void send(String userName, Email email)
    {
        send(userName, email, email.getBody(), email.getBody());
    }

    @Override
    public void send(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        productAccessor.sendEmail(userName, email, bodyAsHtml, bodyAsText);
    }
}
