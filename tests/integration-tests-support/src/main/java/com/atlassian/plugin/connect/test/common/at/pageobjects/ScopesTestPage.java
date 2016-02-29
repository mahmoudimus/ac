package com.atlassian.plugin.connect.test.common.at.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;

import static com.atlassian.plugin.connect.test.common.util.IframeUtils.iframeServletPath;

public class ScopesTestPage extends ConnectAddonPage implements Page {
    public ScopesTestPage(String addonKey, String pageElementKey, boolean includedEmbeddedPrefix) {
        super(addonKey, pageElementKey, includedEmbeddedPrefix);
    }

    @SuppressWarnings("unused")
    public ScopesTestPage(String addonKey) {
        this(addonKey, "ac-acceptance-test-scope-checker-page", true);
    }

    @Override
    public String getUrl() {
        return iframeServletPath(addonKey, pageElementKey);
    }

    public String getCodeForScope(Scope scope) {
        return waitForValue(scope.getId() + "-code");
    }

    public enum Scope {
        ADMIN {
            @Override
            public String getId() {
                return "admin";
            }
        };

        public abstract String getId();
    }
}
