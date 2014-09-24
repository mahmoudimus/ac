package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
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
    public boolean isRemotePluginLinkPresent()
    {
        return link.get().isDefined();
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        return link.get().fold(
                new Supplier<ConnectAddOnEmbeddedTestPage>()
                {
                    @Override
                    public ConnectAddOnEmbeddedTestPage get()
                    {
                        throw new IllegalStateException(format("Could not find link '%s'", link()));
                    }
                },
                new Function<WebElement, ConnectAddOnEmbeddedTestPage>()
                {
                    @Override
                    public ConnectAddOnEmbeddedTestPage apply(WebElement l)
                    {
                        l.click();
                        logger.debug("Link '{}' was found and clicked.", l);
                        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addOnKey, moduleKey, true);
                    }
                }
        );
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
