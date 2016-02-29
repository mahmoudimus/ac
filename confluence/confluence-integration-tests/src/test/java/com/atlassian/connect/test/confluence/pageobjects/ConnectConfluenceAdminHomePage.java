package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 *
 */
public class ConnectConfluenceAdminHomePage extends ConfluenceAdminHomePage {
    public PageElement getWebItem(String pageKey) {
        return pageElementFinder.find(By.id(pageKey), TimeoutType.DEFAULT);
    }
}
