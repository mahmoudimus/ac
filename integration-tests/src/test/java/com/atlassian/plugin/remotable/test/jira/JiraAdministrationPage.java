package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationPage extends AbstractJiraPage
{
    private final static String URI = "/secure/admin/ViewApplicationProperties.jspa";

    @Override
    public String getUrl()
    {
        return URI;
    }

    public JiraAdministrationPage()
    {
        super();
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("general_configuration")).timed().isPresent();
    }

    public RemotePluginEmbeddedTestPage goTo(final String adminPageKey)
    {
        elementFinder.find(By.id("webitem-" + adminPageKey)).click();
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, "servlet-" + adminPageKey);
    }

    public boolean containsLink(final String adminPageKey)
    {
        return elementFinder.find(By.id("webitem-" + adminPageKey)) != null;
    }
}
