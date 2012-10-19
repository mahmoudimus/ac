package com.atlassian.plugin.remotable.plugin.product.refapp;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.module.UserIsAdminCondition;
import com.atlassian.plugin.remotable.spi.module.UserIsLoggedInCondition;
import com.atlassian.plugin.remotable.spi.module.UserIsSysAdminCondition;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.Sets;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class RefappProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;
    private static final Logger log = LoggerFactory.getLogger(RefappProductAccessor.class);

    public RefappProductAccessor(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/general";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 10;
    }

    @Override
    public String getKey()
    {
        return "refapp";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "index.links";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.admin/general";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return emptyMap();
    }

    @Override
    public void sendEmail(String userName, Email originalEmail, String bodyAsHtml, String bodyAsText)
    {
        org.apache.commons.mail.Email email = new SimpleEmail();

        try
        {
            if ("betty".equals(userName))
            {
                email = new HtmlEmail();
                ((HtmlEmail)email).setHtmlMsg(bodyAsHtml);
                ((HtmlEmail)email).setTextMsg(bodyAsText);
            }
            else
            {
                email.setMsg(bodyAsText);
            }

            email.setHostName("localhost");
            email.setSmtpPort(2525);
            email.setFrom(originalEmail.getFrom(), originalEmail.getFromName());
            email.setSubject("[test] " + originalEmail.getSubject());
            email.addTo(originalEmail.getTo());
            final org.apache.commons.mail.Email finalEmail = email;
            ContextClassLoaderSwitchingUtil.runInContext(Email.class.getClassLoader(), new Runnable()

            {
                @Override
                public void run()
                {
                    try
                    {
                        finalEmail.send();
                    }
                    catch (EmailException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });

        }
        catch (RuntimeException e)
        {
            if (e.getCause() instanceof EmailException)
            {
                handleError(email, (EmailException) e.getCause());
            }
            else
            {
                throw e;
            }
        }
        catch (EmailException e)
        {
            handleError(email, e);
        }
    }

    private void handleError(org.apache.commons.mail.Email email, EmailException e)
    {
        log.error("Unable to send email", e);
        if (log.isDebugEnabled())
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try
            {
                email.getMimeMessage().writeTo(bout);
                log.debug("Sent email:\n" + new String(bout.toByteArray()));
            }
            catch (MessagingException ex)
            {
                throw new RuntimeException(ex);
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e1);
            }
        }
    }

    @Override
    public void flushEmail()
    {
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        Map<String,Class<? extends Condition>> conditions = newHashMap();
        conditions.put("user_is_sysadmin", UserIsSysAdminCondition.class);
        conditions.put("user_is_logged_in", UserIsLoggedInCondition.class);
        conditions.put("user_is_admin", UserIsAdminCondition.class);
        return conditions;
    }

    @Override
    public Set<String> getAllowedPermissions(InstallationMode installationMode)
    {
        return Permissions.DEFAULT_REMOTE_PERMISSIONS;
    }
}
