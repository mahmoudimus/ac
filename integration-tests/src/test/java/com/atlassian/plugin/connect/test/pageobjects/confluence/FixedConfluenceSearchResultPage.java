package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.page.SearchResultPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

public class FixedConfluenceSearchResultPage extends SearchResultPage
{
    @ElementBy(id = "query-string")
    private PageElement resultPageSearchInputBox;

    @ElementBy(id = "search-query-submit-button")
    private PageElement resultPageSearchButton;

    @Override
    public void doWait()
    {
        // don't check that js variable that only exists in confluence tests
    }

    public void setSearchField(String searchTerms) {
        resultPageSearchInputBox.clear();
        resultPageSearchInputBox.type(searchTerms);
    }

    public SearchResultPage clickSearchButton() {
        resultPageSearchButton.click();
        return pageBinder.bind(getSearchResultPageClass());
    }

    protected Class<? extends SearchResultPage> getSearchResultPageClass()
    {
        return getClass();
    }
}
