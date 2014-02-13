package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.plugin.connect.test.utils.WebItemUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Describes JIRA version tab
 */
public class JiraVersionTabPage extends AbstractJiraPage
{
    private final String projectKey;
    private final String versionId;
    private final String pluginKey;
    private final String moduleKey;

    protected PageElement tabField;

    private String iframeSrc;

    @Inject
    private PageElementFinder elementFinder;

    public JiraVersionTabPage(String projectKey, String versionId, String pluginKey, String moduleKey) {
        this.projectKey = projectKey;
        this.versionId = versionId;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public TimedCondition isAt()
    {
        String versionTabPanelId = WebItemUtils.linkId(pluginKey, moduleKey) + "-panel";
        tabField = elementFinder.find(By.id(versionTabPanelId));

        return tabField.timed().isPresent();
    }

    public void clickTab()
    {
        tabField.click();

        PageElement iframe = elementFinder.find(By.id(IframeUtils.iframeId(moduleKey)));
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
