package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.confluence.plugin.descriptor.web.descriptors.ConfluenceWebItemModuleDescriptor;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
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
    private final MultiQueueTaskManager taskManager;

    public ConfluenceProductAccessor(MultiQueueTaskManager taskManager)
    {
        this.taskManager = taskManager;
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
    public void sendEmail(String userName, final Email email, String bodyAsHtml, String bodyAsText)
    {
        // todo: support html emails for Confluence
        email.setBody(bodyAsText);
        try
        {
            ContextClassLoaderSwitchingUtil.runInContext(MailFactory.class.getClassLoader(), new Runnable()

            {
                @Override
                public void run()
                {
                    SMTPMailServer defaultSMTPMailServer = MailFactory.getServerManager()
                            .getDefaultSMTPMailServer();
                    if (defaultSMTPMailServer != null)
                    {
                        try
                        {
                            defaultSMTPMailServer.send(email);
                        }
                        catch (MailException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        log.warn("Can't send email - no mail server defined");
                    }
                }
            });

        }
        catch (RuntimeException e)
        {
            log.warn("Unable to send email: " + email, e);
        }
    }

    @Override
    public void flushEmail()
    {
        taskManager.flush("mail");
    }
}
