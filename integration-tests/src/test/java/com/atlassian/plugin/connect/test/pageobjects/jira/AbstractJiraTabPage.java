package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.plugin.connect.test.utils.WebItemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import javax.inject.Inject;

public abstract class AbstractJiraTabPage extends AbstractJiraPage
{
    protected final String projectKey;
    protected final String tabId;
    private final String pluginKey;
    private final String moduleKey;

    protected PageElement tabField;

    protected String iframeSrc;

    @Inject
    private PageElementFinder elementFinder;

    protected AbstractJiraTabPage(String projectKey, String tabId, String pluginKey, String moduleKey)
    {
        this.projectKey = projectKey;
        this.tabId = tabId;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("project-tab")).timed().isPresent(); // check that the project tab section has loaded
    }

    public boolean isAddOnTabPresent()
    {
        return findAddOnTab().timed().isPresent().byDefaultTimeout();
    }

    public void clickTab()
    {
        if (!isAddOnTabPresent())
        {
            throw new NoSuchElementException("Couldn't find add-on tab with id " + tabPanelId());
        }
        findAddOnTab().click();

        PageElement iframe = elementFinder.find(By.id(IframeUtils.iframeId(moduleKey)));

        iframe.timed().isPresent();

        iframeSrc = iframe.getAttribute("src");

        System.out.println(iframeSrc);

    }

    public PageElement findAddOnTab()
    {
        if (tabField == null)
        {
            tabField = elementFinder.find(By.id(tabPanelId()));
        }
        return tabField;
    }

    protected String tabPanelId()
    {
        return WebItemUtils.linkId(pluginKey, moduleKey) + "-panel";
    }

    public String getProjectKey()
    {
        return RemotePageUtil.findInContext(iframeSrc, "project_key");
    }

}
