package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Optional;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * A remote web-item, which link is expected to pass context.
 */
public class RemoteWebItem
{
    public static enum ItemMatchingMode { ID, LINK_TEXT, JQUERY }

    private static final String INLINE_DIALOG_ACTIVE_CLASS = "active";
    private final static Logger log = LoggerFactory.getLogger(RemoteWebItem.class);

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
        path = webItem.getAttribute("href");
    }

    private PageElement findWebItem()
    {
        By by;
        switch (mode) {
            case ID:
            default:
                by = By.id(matchValue);
                break;
            case LINK_TEXT:
                by = By.linkText(matchValue);
                break;
            case JQUERY:
                String escapedMatchValue = AddonTestUtils.escapeJQuerySelector(matchValue);
                by = ByJquery.$(escapedMatchValue);
                break;
        }
        return elementFinder.find(by);
    }

    private boolean isPageBackedWebItem()
    {
        String href = webItem.getAttribute("href");
        return href.contains("/plugins/servlet/atlassian-connect/") || href.contains("/plugins/servlet/ac/");
    }

    public String getLinkText()
    {
        return webItem.getText();
    }

    public String getTitle()
    {
        return webItem.getAttribute("title");
    }

    public void click()
    {
        if (dropDownLinkId.isPresent())
        {
            PageElement element = elementFinder.find(By.id(dropDownLinkId.get()));
            waitUntilTrue(element.timed().isVisible());
            element.javascript().mouse().click();
        }
        webItem.javascript().mouse().click();
    }

    public void hover()
    {
        if (dropDownLinkId.isPresent())
        {
            PageElement element = elementFinder.find(By.id(dropDownLinkId.get()));
            waitUntilTrue(element.timed().isVisible());
            element.javascript().mouse().click();
        }
        webItem.javascript().mouse().mouseover();
    }

    public boolean isVisible()
    {
        return webItem != null && webItem.isVisible();
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
        return null != webItem && webItem.hasClass("ap-inline-dialog");
    }

    public boolean isActiveInlineDialog()
    {
        if (!isInlineDialog())
        {
            return false;
        }

        try
        {
            waitUntilTrue(webItem.timed().hasClass(INLINE_DIALOG_ACTIVE_CLASS));
            return true;
        }
        catch (AssertionError e)
        {
            log.error(String.format("Timed out waiting for web item with id '%s' to get css class '%s'.", webItem.getAttribute("id"), INLINE_DIALOG_ACTIVE_CLASS), e);
            return false;
        }
    }

    public boolean isDialog()
    {
        return null != webItem && webItem.hasClass("ap-dialog");
    }

    public boolean isActiveDialog()
    {
        PageElement dialog = elementFinder.find(By.className("aui-dialog2-content"));
        return (dialog.isPresent() && dialog.isVisible());
    }

}
