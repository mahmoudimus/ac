package com.atlassian.plugin.connect.test.jira.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;

public class JiraRequestExperimentalTestPage extends ConnectAddonEmbeddedTestPage implements Page
{
    public JiraRequestExperimentalTestPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public String getIndexedClientHttpStatus(int index) {
        return waitForValue("client-http-status-" + Integer.toString(index));
    }

    public String getIndexedClientHttpStatusText(int index) {
        return waitForValue("client-http-status-text-" + Integer.toString(index));
    }

    public String getIndexedClientHttpResponseText(int index) {
        return waitForValue("client-http-response-text-" + Integer.toString(index));
    }

}

