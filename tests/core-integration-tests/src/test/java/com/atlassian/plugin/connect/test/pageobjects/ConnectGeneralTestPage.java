package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;

public class ConnectGeneralTestPage extends ConnectAddonEmbeddedTestPage
        implements Page {

    public ConnectGeneralTestPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl() {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }
}
