package com.atlassian.plugin.connect.test.common.at.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnPage;

import static com.atlassian.plugin.connect.test.common.util.IframeUtils.iframeServletPath;

public class ScopesTestPage extends ConnectAddOnPage implements Page
{
    public ScopesTestPage(String addOnKey, String pageElementKey, boolean includedEmbeddedPrefix)
    {
        super(addOnKey, pageElementKey, includedEmbeddedPrefix);
    }

    public ScopesTestPage(String addOnKey)
    {
        this(addOnKey, "ac-acceptance-test-scope-checker-page-jira", true);
    }

    @Override
    public String getUrl()
    {
        return iframeServletPath(addOnKey, pageElementKey);
    }

    public String getCodeForScope(Scope scope)
    {
        return waitForValue(scope.getId() + "-code");
    }

    public static enum Scope
    {
        ADMIN {
            @Override
            public String getId()
            {
                return "admin";
            }
        };

        public abstract String getId();
    }
}
