package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.lang.String.format;

public final class JiraGeneralPage implements GeneralPage
{
    private static final By MORE_MENU = By.linkText("More");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;

    private final Supplier<Option<WebElement>> link = Suppliers.memoize(new Supplier<Option<WebElement>>()
    {
        @Override
        public Option<WebElement> get()
        {
            dismissCreateProjectDialogIfPresent();
            expandMoreMenuIfExists();

            return driver.elementExists(link()) ? Option.some(driver.findElement(link())) : Option.<WebElement>none();
        }
    });

    public JiraGeneralPage(String pageKey, String linkText)
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
                    public RemotePluginTestPage apply(WebElement l)
                    {
                        l.click();
                        logger.debug("Link '{}' was found and clicked.", l);
                        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
                    }
                }
        );
    }

    private void expandMoreMenuIfExists()
    {
        final boolean moreMenu = clickElementIfExist(MORE_MENU);
        if (moreMenu)
        {
            logger.debug("'More' menu found, expanding as our link might be in there.");
        }
    }

    private By link()
    {
        return By.linkText(linkText);
    }

    private void dismissCreateProjectDialogIfPresent()
    {
        clickElementIfExist(createProjectDialogCancelButton());
    }

    private boolean clickElementIfExist(By by)
    {
        final boolean exists = driver.elementExists(by);
        if (exists)
        {
            logger.debug("Clicking element '{}'.", by);
            driver.findElement(by).click();
        }
        else
        {
            logger.debug("Element '{}' was NOT found, therefore not clicked.", by);
        }
        return exists;
    }

    private By createProjectDialogCancelButton()
    {
        return By.className("button-panel-cancel-link");
    }
}
