package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.VersionTabPageModuleDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Describes JIRA version tab
 */
public class JiraVersionTabPage extends AbstractJiraPage
{
    private final String projectKey;
    private final String versionId;
    private final String versionTabId;

    protected PageElement tabField;

    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-";
    private static final String IFRAME_ID_SUFFIX = "-panel_provider";
    private PageElement iframe;
    private String iframeSrc;

    @Inject
    private PageElementFinder elementFinder;

    public JiraVersionTabPage(final String projectKey, final String versionId, final String versionTabId) {
        this.projectKey = projectKey;
        this.versionId = versionId;
        this.versionTabId = versionTabId;
    }

    @Override
    public TimedCondition isAt()
    {
        final String versionTabPanelId = versionTabId + "-panel-panel";
        tabField = elementFinder.find(By.id(versionTabPanelId));

        return tabField.timed().isPresent();
    }

    public void clickTab()
    {
        tabField.click();

        final String iframeId = IFRAME_ID_PREFIX + versionTabId + IFRAME_ID_SUFFIX;
        iframe = elementFinder.find(By.id(iframeId));
        iframeSrc = iframe.getAttribute("src");


        iframe.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey + "/fixforversion/" + versionId;
    }

    public String getProjectKey()
    {
        return RemotePageUtil.findInContext(iframeSrc, "project_key");
    }

    public String getVersionId()
    {
        return RemotePageUtil.findInContext(iframeSrc, "version_id");
    }
}
