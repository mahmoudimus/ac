package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Describes JIRA component tab
 */
public class JiraComponentTabPage extends AbstractJiraPage
{
    private final String projectKey;
    private final String componentId;
    private final String componentTabId;

    protected PageElement tabField;

    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-";
    private static final String IFRAME_ID_SUFFIX = "-panel_provider";
    private PageElement iframe;
    private String iframeSrc;

    @Inject
    private PageElementFinder elementFinder;

    public JiraComponentTabPage(final String projectKey, final String componentId, final String componentTabId)
    {
        this.projectKey = projectKey;
        this.componentId = componentId;
        this.componentTabId = componentTabId;
    }


    @Override
    public TimedCondition isAt()
    {
        final String componentTabPanelId = componentTabId + "-panel-panel";
        tabField = elementFinder.find(By.id(componentTabPanelId));

        return tabField.timed().isPresent();
    }

    public void clickTab()
    {
        tabField.click();

        final String iframeId = IFRAME_ID_PREFIX + componentTabId + IFRAME_ID_SUFFIX;
        iframe = elementFinder.find(By.id(iframeId));
        iframeSrc = iframe.getAttribute("src");

        iframe.timed().isPresent();
    }


    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey + "/component/" + componentId;
    }

    public String getProjectKey()
    {
        return RemotePageUtil.findInContext(iframeSrc, "project_key");
    }

    public String getComponentId()
    {
        return RemotePageUtil.findInContext(iframeSrc, "component_id");
    }
}
