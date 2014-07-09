package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.apache.commons.lang.NotImplementedException;
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

    private final Supplier<Option<WebElement>> link = new Supplier<Option<WebElement>>()
    {
        @Override
        public Option<WebElement> get()
        {
            return driver.elementExists(link()) ? some(driver.findElement(link())) : Option.<WebElement>none();
        }
    };

    public JiraAdminPage(String pageKey)
    {
        this.pageKey = pageKey;
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return link.get().isDefined();
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        return withLinkElement(new Function<WebElement, RemotePluginTestPage>()
        {
            @Override
            public RemotePluginTestPage apply(WebElement linkElement)
            {
                linkElement.click();
                logger.debug("Link '{}' was found and clicked.", linkElement);
                return pageBinder.bind(RemotePluginTestPage.class, pageKey);
            }
        });
    }

    @Override
    public ConnectAddOnTestPage clickAddOnLink()
    {
        throw new NotImplementedException("TODO as part of porting tests from xml descriptors to json descriptors");
    }

    @Override
    public String getRemotePluginLinkHref()
    {
        return withLinkElement(new Function<WebElement, String>()
        {
            @Override
            public String apply(WebElement linkElement)
            {
                return linkElement.getAttribute("href");
            }
        });
    }

    public String getRemotePluginLinkText()
    {
        return withLinkElement(new Function<WebElement, String>()
        {
            @Override
            public String apply(WebElement linkElement)
            {
                return linkElement.getText();
            }
        });
    }

    private <T> T withLinkElement(Function<WebElement, T> function)
    {
        return link.get().fold(
                new Supplier<T>()
                {
                    @Override
                    public T get()
                    {
                        throw new IllegalStateException(format("Could not find link '%s'", link()));
                    }
                },
                function
        );
    }

    private By link()
    {
        return By.id(pageKey);
    }

}
