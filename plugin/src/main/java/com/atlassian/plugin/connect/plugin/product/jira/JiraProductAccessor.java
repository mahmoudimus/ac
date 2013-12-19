package com.atlassian.plugin.connect.plugin.product.jira;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.CanConvertToIssueCondition;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.Email;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.plugin.connect.plugin.capabilities.beans.JiraConditions;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.ViewingOwnProfileCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.collect.Maps.newHashMap;

@JiraComponent
public final class JiraProductAccessor implements ProductAccessor
{
    private final UserManager userManager;
    private final MailQueue mailQueue;
    private final JiraConditions jiraConditions;

    @Autowired
    public JiraProductAccessor(UserManager userManager, MailQueue mailQueue, JiraConditions jiraConditions)
    {
        this.userManager = userManager;
        this.mailQueue = mailQueue;
        this.jiraConditions = jiraConditions;
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "advanced_menu_section/advanced_section";
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
        return "system.top.navigation.bar";
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
                "project_key", "$!helper.project.key",
                "issue_id", "$!issue.id",
                "issue_key", "$!issue.key");
    }

    @Override
    public void sendEmail(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        User user = userManager.getUser(userName);

        JiraUserPreferences userPrefs = new JiraUserPreferences(user);
        String prefFormat = userPrefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to text if the property is not configured.
        if ("html".equalsIgnoreCase(prefFormat))
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

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        return jiraConditions.getConditions();
    }
}
