package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.google.common.base.Optional;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static it.TestConstants.IFRAME_ID_SUFFIX;

/**
 * A remote web-item, which link is expected to pass context.
 */
public class RemoteWebItem
{
    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-servlet-";

    @Inject
    private PageElementFinder elementFinder;

    private final String id;
    private final Optional<String> dropDownLinkId;

    private PageElement webItem;
    private String path;

    public RemoteWebItem(String id, Optional<String> dropDownLinkId)
    {
        this.id = id;
        this.dropDownLinkId = dropDownLinkId;
    }

    @Init
    public void init()
    {
        webItem = elementFinder.find(By.id(id));
        waitUntilTrue(webItem.timed().isPresent());

        if (!isPointingToOldXmlInternalUrl() && !isPointingToACInternalUrl())
        {
            path = elementFinder.find(By.id(IFRAME_ID_PREFIX + id + IFRAME_ID_SUFFIX)).getAttribute("src");
        }
        else
        {
            path = webItem.getAttribute("href");
        }
    }

    public boolean isPointingToOldXmlInternalUrl()
    {
        return !webItem.getAttribute("href").contains("/plugins/servlet/atlassian-connect/");
    }

    public boolean isPointingToACInternalUrl()
    {
        return !webItem.getAttribute("href").contains("/plugins/servlet/ac/");
    }

    public void click()
    {
        if (dropDownLinkId.isPresent())
        {
            elementFinder.find(By.id(dropDownLinkId.get())).click();
        }
        webItem.click();
    }

    public boolean isVisible()
    {
        if(null == webItem)
        {
            return false;
        }
        
        return webItem.isVisible();
    }
    
    public String getFromQueryString(final String key)
    {
        return RemotePageUtil.findInContext(path, key);
    }

    public String getPath()
    {
        return path;
    }
}
