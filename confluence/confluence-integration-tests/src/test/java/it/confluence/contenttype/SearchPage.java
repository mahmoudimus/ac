package it.confluence.contenttype;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

public class SearchPage extends com.atlassian.confluence.pageobjects.page.search.SearchPage {
    public boolean noResult() {
        return this.pageElementFinder
                .find(By.className("search-results-container"))
                .timed()
                .getText()
                .now()
                .toLowerCase()
                .contains("no results");
    }

    public SearchPage searchQueryWithOutWaiting(String query) {
        PageElement searchInputBox = this.pageElementFinder.find(By.id("query-string"));
        PageElement searchButton = this.pageElementFinder.find(By.id("search-query-submit-button"));
        PageElement searchResultsContainer = this.pageElementFinder.find(By.className("search-results-container"));

        searchInputBox.clear();
        searchInputBox.type(query);
        searchButton.click();
        Poller.waitUntilTrue(searchResultsContainer.timed().isVisible());
        return this;
    }
}
