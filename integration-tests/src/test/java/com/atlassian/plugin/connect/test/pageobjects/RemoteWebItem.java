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
    public static enum ItemMatchingMode { ID, LINK_TEXT }

    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-servlet-";

    @Inject
    private PageElementFinder elementFinder;

    private final ItemMatchingMode mode;
    private final String matchValue;
    private final Optional<String> dropDownLinkId;

    private PageElement webItem;
    private String path;

    public RemoteWebItem(ItemMatchingMode mode, String matchValue, Optional<String> dropDownLinkId)
    {
        this.mode = mode;
        this.matchValue = matchValue;
        this.dropDownLinkId = dropDownLinkId;
    }

    public RemoteWebItem(String id, Optional<String> dropDownLinkId)
    {
        this(ItemMatchingMode.ID, id, dropDownLinkId);
    }

    @Init
    public void init()
    {
        webItem = findWebItem();
        waitUntilTrue(webItem.timed().isPresent());

        if (!isPointingToOldXmlInternalUrl() && !isPointingToACInternalUrl())
        {
            path = elementFinder.find(By.id(IFRAME_ID_PREFIX + matchValue + IFRAME_ID_SUFFIX)).getAttribute("src");
        }
        else
        {
            path = webItem.getAttribute("href");
        }
    }

    private PageElement findWebItem()
    {
        By by = null;
        switch (mode) {
            case ID:
            default:
                by = By.id(matchValue);
                break;
            case LINK_TEXT:
                by = By.linkText(matchValue);
                break;
        }
        return elementFinder.find(by);
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

    public boolean isInlineDialog()
    {
        if(null == webItem)
        {
            return false;
        }
        if(webItem.hasClass("ap-inline-dialog")){
            return true;
        }
        return false;
    }

    public boolean isActiveInlineDialog()
    {
        if(isInlineDialog())
        {
            return webItem.hasClass("active");
        }
        return false;
    }
}
