package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.pageobjects.AbstractConnectIFrameComponent;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class RenderedMacro extends AbstractConnectIFrameComponent<RenderedMacro>
{
    private final String idPrefix;
    private final int indexOnPage;

    private String iframeId;

    public RenderedMacro(String idPrefix)
    {
        this(idPrefix, 0);
    }

    public RenderedMacro(String idPrefix, int indexOnPage)
    {
        this.idPrefix = IframeUtils.IFRAME_ID_PREFIX + idPrefix;
        this.indexOnPage = indexOnPage;
    }

    @Init
    public void init()
    {
        // wait until at least one iframe is present, before we iterate through them
        waitUntilTrue(elementFinder.find(By.tagName("iframe")).timed().isPresent());

        int index = 0;
        for (PageElement iframeOnPage : elementFinder.findAll(By.tagName("iframe")))
        {
            String id = iframeOnPage.getAttribute("id");
            if (id != null && id.startsWith(idPrefix) && indexOnPage == index++) {
                this.iframe = iframeOnPage;
                break;
            }
        }

        if (iframe == null)
        {
            throw new NoSuchElementException("Couldn't find iframe with id starting with " + idPrefix);
        }

        iframeId = iframe.getAttribute("id");
        iframeSrc = iframe.getAttribute("src");
    }

    @Override
    protected String getFrameId()
    {
        return iframeId;
    }
}
