package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.ComponentTabPageModuleDescriptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;

import java.net.URI;
import javax.inject.Inject;

/**
 * Describes JIRA component tab
 *
 * @since v6.1
 */
public class JiraComponentTabPage extends AbstractJiraPage
{
    private final String projectKey;
    private final String componentId;
    private final String componentTabId;

    protected PageElement tabField;

    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-component-tab-";
    private static final String IFRAME_ID_SUFFIX = "-panel_provider";
    private PageElement iframe;

    @Inject
    private PageElementFinder elementFinder;

    public JiraComponentTabPage(final String projectKey, final String componentId, final String componentTabId)
    {
        this.projectKey = projectKey;
        this.componentId = componentId;
        this.componentTabId = componentTabId;
    }


    @Override
    public TimedCondition isAt() {
        final String componentTabPanelId = ComponentTabPageModuleDescriptor.COMPONENT_TAB_PAGE_MODULE_PREFIX + componentTabId + "-panel-panel";
        tabField = elementFinder.find(By.id(componentTabPanelId));

        return tabField.timed().isPresent();
    }

    public void clickTab() {
        tabField.click();

        final String iframeId = IFRAME_ID_PREFIX + componentTabId + IFRAME_ID_SUFFIX;
        iframe = elementFinder.find(By.id(iframeId));

        iframe.timed().isPresent();
    }


    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey + "/component/" + componentId;
    }

    public String getProjectKey() {
        return findInContext("project_key");
    }

    public String getComponentId() {
        return findInContext("component_id");
    }

    private String findInContext(final String key)
    {
        final String src = iframe.getAttribute("src");
        for (final NameValuePair pair : URLEncodedUtils.parse(URI.create(src), "UTF-8"))
        {
            if (key.equals(pair.getName()))
            {
                return pair.getValue();
            }
        }
        return null;
    }
}
