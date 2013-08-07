package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.mail.Email;
import com.atlassian.plugin.connect.api.service.EmailSender;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

public final class LocalEmailSender implements EmailSender
{
    private final String pluginKey;
    private final ProductAccessor productAccessor;
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public LocalEmailSender(String pluginKey, ProductAccessor productAccessor,
            PermissionManager permissionManager, UserManager userManager)
    {
        this.pluginKey = pluginKey;
        this.productAccessor = productAccessor;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
    }

    @Override
    public void send(String userName, Email email)
    {
        send(userName, email, email.getBody(), email.getBody());
    }

    @Override
    public void send(String userName, Email email, String bodyAsHtml, String bodyAsText)
            throws PermissionDeniedException
    {
        permissionManager.requirePermission(pluginKey, Permissions.SEND_EMAIL);

        UserProfile userProfile = userManager.getUserProfile(userName);
        if (userProfile == null)
        {
            throw new IllegalArgumentException("Unknown user: " + userName);
        }

        email.setTo(userProfile.getEmail());
        productAccessor.sendEmail(userName, email, bodyAsHtml, bodyAsText);
    }

    @Override
    public void flush()
    {
        permissionManager.requirePermission(pluginKey, Permissions.SEND_EMAIL);
        productAccessor.flushEmail();
    }
}
