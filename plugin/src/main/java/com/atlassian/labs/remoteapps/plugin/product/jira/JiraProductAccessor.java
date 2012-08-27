package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 *
 */
public class JiraProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;
    private final UserManager userManager;
    private final MailQueue mailQueue;

    public JiraProductAccessor(WebInterfaceManager webInterfaceManager, UserManager userManager,
            MailQueue mailQueue)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.userManager = userManager;
        this.mailQueue = mailQueue;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new JiraWebItemModuleDescriptor(ComponentManager.getInstance().getJiraAuthenticationContext(), webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/system";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 150;
    }

    @Override
    public String getKey()
    {
        return "jira";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "general_dropdown_linkId/jira-remoteapps.general";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.user.options/personal";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "project_id", "$!helper.project.id",
                "issue_id", "$!issue.id");
    }

    @Override
    public void sendEmail(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        User user = userManager.getUser(userName);

        JiraUserPreferences userPrefs = new JiraUserPreferences(user);
        String prefFormat = userPrefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to text if the property is not configured.
        if(!"html".equalsIgnoreCase(prefFormat))
        {
            email.setMimeType("text/html");
            email.setBody(bodyAsHtml);
        }
        else
        {
            email.setMimeType("text/plain");
            email.setBody(bodyAsText);
        }
        mailQueue.addItem(new SingleMailQueueItem(email));
    }

    @Override
    public void flushEmail()
    {
        mailQueue.sendBuffer();
    }
}
