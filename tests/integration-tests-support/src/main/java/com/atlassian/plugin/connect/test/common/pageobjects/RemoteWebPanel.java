package com.atlassian.plugin.connect.test.common.pageobjects;

import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import org.openqa.selenium.By;

/**
 * A remote web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel extends AbstractConnectIFrameComponent<RemoteWebPanel> {
    private final String id;

    public RemoteWebPanel(final String id) {
        this.id = id;
    }

    protected String getFrameId() {
        return IframeUtils.iframeId(id);
    }

    public String getUserId() {
        return getFromQueryString("user_id");
    }

    public String getUserKey() {
        return getFromQueryString("user_key");
    }

    public String getProjectId() {
        return getFromQueryString("project_id");
    }

    public String getIssueId() {
        return getFromQueryString("issue_id");
    }

    public String getSpaceId() {
        return getFromQueryString("space_id");
    }

    public String getSpaceKey() {
        return getFromQueryString("space_key");
    }

    public String getPageId() {
        return getFromQueryString("page_id");
    }

    public String getContentId() {
        return getFromQueryString("content_id");
    }

    public String getCustomMessage() {
        return getIFrameElementText("custom-message");
    }

    public String getApRequestMessage() {
        return getIFrameElementText("message");
    }

    public String getApRequestStatusCode() {
        return getIFrameElementText("client-http-status");
    }

    public String getApRequestUnauthorizedStatusCode() {
        return getIFrameElementText("client-http-unauthorized-code");
    }

    public boolean containsHelloWorld() {
        return withinIFrame(frame -> frame.findElement(By.id("hello-world-message")).isDisplayed());
    }
}
