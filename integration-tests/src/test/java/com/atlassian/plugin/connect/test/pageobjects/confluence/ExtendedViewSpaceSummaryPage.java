package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.google.inject.Inject;

public class ExtendedViewSpaceSummaryPage extends ViewSpaceSummaryPage
{

    public static final String SPACE_TAB_SELECTOR_PATTERN = "li[data-web-item-key='%s'] > a:first";
    @Inject
    private ConnectPageOperations connectPageOperations;

    public ExtendedViewSpaceSummaryPage(Space space)
    {
        super(space);
    }

    public LinkedRemoteContent findSpaceToolsTab(String moduleKey)
    {
        String locator = String.format(SPACE_TAB_SELECTOR_PATTERN, moduleKey);
        return connectPageOperations.findRemoteLinkedContent(
                RemoteWebItem.ItemMatchingMode.JQUERY, locator, Option.<String>none(), moduleKey);
    }
}
