package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A remote web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel extends AbstractConnectIFrameComponent<RemoteWebPanel>
{
    private final String id;
    /**
     * For retrieving web panels registered by legacy descriptors.
     * TODO remove when we drop support for XML descriptors.
     */
    @Deprecated
    @XmlDescriptor
    private final String extraPrefix;

    @Deprecated
    @XmlDescriptor
    public RemoteWebPanel(final String id, String extraPrefix)
    {
        this.id = id;
        this.extraPrefix = extraPrefix;
    }

    public RemoteWebPanel(final String id)
    {
        this(id, "");
    }

    protected String getFrameId()
    {
        return IframeUtils.iframeId(extraPrefix + id);
    }

    public String getUserId()
    {
        return getFromQueryString("user_id");
    }

    public String getUserKey()
    {
        return getFromQueryString("user_key");
    }

    public String getProjectId()
    {
        return getFromQueryString("project_id");
    }

    public String getIssueId()
    {
        return getFromQueryString("issue_id");
    }

    public String getSpaceId()
    {
        return getFromQueryString("space_id");
    }

    public String getSpaceKey()
    {
        return getFromQueryString("space_key");
    }

    public String getPageId()
    {
        return getFromQueryString("page_id");
    }

    public String getCustomMessage()
    {
        return getIFrameElementText("custom-message");
    }

    public String getApRequestMessage()
    {
        return getIFrameElementText("message");
    }

    public String getApRequestStatusCode()
    {
        return getIFrameElementText("client-http-status");
    }

    public String getApRequestUnauthorizedStatusCode()
    {
        return getIFrameElementText("client-http-unauthorized-code");
    }

    public boolean containsHelloWorld()
    {
        return withinIFrame(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(WebDriver frame)
            {
                return frame.findElement(By.id("hello-world-message")).isDisplayed();
            }
        });
    }
}
