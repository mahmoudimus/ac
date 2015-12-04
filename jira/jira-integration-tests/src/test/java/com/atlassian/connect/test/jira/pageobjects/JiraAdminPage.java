package com.atlassian.connect.test.jira.pageobjects;

import javax.inject.Inject;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.some;
import static java.lang.String.format;

public final class JiraAdminPage implements AdminPage
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String addOnKey;
    private final String moduleKey;

    private final Supplier<Option<WebElement>> link = new Supplier<Option<WebElement>>()
    {
        @Override
        public Option<WebElement> get()
        {
            return driver.elementExists(link()) ? some(driver.findElement(link())) : Option.<WebElement>none();
        }
    };

    public JiraAdminPage(String addOnKey, String moduleKey)
    {
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        clickAddOnLinkWithoutBinding();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addOnKey, moduleKey, true);
    }

    @Override
    public void clickAddOnLinkWithoutBinding()
    {
        final WebElement webElement = link.get().get();
        webElement.click();
        logger.debug("Link '{}' was found and clicked.", webElement);
    }

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
        return By.id(ModuleKeyUtils.addonAndModuleKey(addOnKey, moduleKey));
    }

}
