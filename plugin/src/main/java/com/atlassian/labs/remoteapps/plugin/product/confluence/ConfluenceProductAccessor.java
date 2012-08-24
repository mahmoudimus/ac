package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class ConfluenceProductAccessor implements ProductAccessor
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceProductAccessor.class);
    private final UserManager userManager;

    public ConfluenceProductAccessor(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new ConfluenceWebItemModuleDescriptor();
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/admin.pages";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 100;
    }

    @Override
    public String getKey()
    {
        return "confluence";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "system.browse";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.profile"; 
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "page_id", "$!helper.page.id",
                "page_type", "$!helper.page.type");
    }

    @Override
    public void sendEmail(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        UserProfile userProfile = userManager.getUserProfile(userName);
        if (userProfile == null)
        {
            throw new IllegalArgumentException("Unknown user: " + userName);
        }

        email.setTo(userProfile.getEmail());

        // todo: support html emails for Confluence
        email.setBody(bodyAsText);
        try
        {
            SMTPMailServer defaultSMTPMailServer = MailFactory.getServerManager()
                    .getDefaultSMTPMailServer();
            if (defaultSMTPMailServer != null)
            {
                defaultSMTPMailServer.send(email);
            }
            else
            {
                log.warn("Can't send email - no mail server defined");
            }
        }
        catch (MailException e)
        {
            log.warn("Unable to send email: " + email);
        }
    }
}
