package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationHomePage extends AbstractJiraPage
{
    private static final String JIRA_ADMIN_PAGE_URI = "/secure/admin/ViewApplicationProperties.jspa";

    public JiraAdministrationHomePage()
    {
    }

    @Override
    public String getUrl()
    {
        return JIRA_ADMIN_PAGE_URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("general_configuration")).timed().isPresent();
    }
}
