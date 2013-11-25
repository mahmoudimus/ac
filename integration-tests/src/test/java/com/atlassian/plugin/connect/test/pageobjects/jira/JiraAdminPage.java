package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.atlassian.fugue.Option.some;
import static java.lang.String.format;

public final class JiraAdminPage implements AdminPage
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;

    private final Supplier<Option<WebElement>> link = new Supplier<Option<WebElement>>()
    {
        @Override
        public Option<WebElement> get()
        {
            return driver.elementExists(link()) ? some(driver.findElement(link())) : Option.<WebElement>none();
        }
    };

    public JiraAdminPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return link.get().isDefined();
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        return link.get().fold(
                new Supplier<RemotePluginTestPage>()
                {
                    @Override
                    public RemotePluginTestPage get()
                    {
                        throw new IllegalStateException(format("Could not find link '%s'", link()));
                    }
                },
                new Function<WebElement, RemotePluginTestPage>()
                {
                    @Override
                    public RemotePluginTestPage apply(WebElement linkElement)
                    {
                        linkElement.click();
                        logger.debug("Link '{}' was found and clicked.", linkElement);
                        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
                    }
                }
        );
    }

    @Override
    public String getRemotePluginLinkHref()
    {
        return link.get().fold(
                new Supplier<String>()
                {
                    @Override
                    public String get()
                    {
                        throw new IllegalStateException(format("Could not find link '%s'", link()));
                    }
                },
                new Function<WebElement, String>()
                {
                    @Override
                    public String apply(WebElement linkElement)
                    {
                        return linkElement.getAttribute("href");
                    }
                }
        );
    }

    private By link()
    {
        return By.linkText(linkText);
    }

}
