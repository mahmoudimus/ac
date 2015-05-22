package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
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
    private final String addonKey;

    private final Supplier<Option<WebElement>> link = new Supplier<Option<WebElement>>()
    {
        @Override
        public Option<WebElement> get()
        {
            dismissCreateProjectDialogIfPresent();
            expandMoreMenuIfExists();

            return driver.elementExists(link()) ? some(driver.findElement(link())) : Option.<WebElement>none();
        }
    };

    public JiraGeneralPage(String pageKey, String addonKey)
    {
        this.pageKey = pageKey;
        this.addonKey = addonKey;
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        final WebElement webElement = link.get().get();
        webElement.click();
        logger.debug("Link '{}' was found and clicked.", webElement);
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addonKey, pageKey, true);
    }

    public void clickRemotePluginLinkWithoutBinding()
    {
        link.get().fold(
                new Supplier<Void>()
                {
                    @Override
                    public Void get()
                    {
                        throw new IllegalStateException(format("Could not find link '%s'", link()));
                    }
                },
                new Function<WebElement, Void>()
                {
                    @Override
                    public Void apply(WebElement l)
                    {
                        l.click();
                        logger.debug("Link '{}' was found and clicked.", l);
                        
                        return null;
                    }
                }
        );
    }

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
                    public String apply(WebElement l)
                    {
                        return l.getAttribute("href");
                    }
                }
        );
    }

    private boolean expandMoreMenuIfExists()
    {
        return getElement(MORE_MENU).fold(
                new Supplier<Boolean>()
                {
                    @Override
                    public Boolean get()
                    {
                        logger.debug("'More' menu was not found. Nothing to expand.");
                        return false;
                    }
                },
                new Function<WebElement, Boolean>()
                {
                    @Override
                    public Boolean apply(WebElement moreElement)
                    {
                        final String cssClass = " " + moreElement.getAttribute("class") + " ";
                        if (!cssClass.contains(" active "))
                        {
                            logger.debug("'More' menu found and is not active ({}). Expanding as our link might be in there.", cssClass);
                            moreElement.click();
                        }
                        else
                        {
                            logger.debug("'More' menu found, already active and expanded ({}).", cssClass);
                        }
                        return true;
                    }
                }
        );
    }

    private Option<WebElement> getElement(By locator)
    {
        return driver.elementExists(locator) ? some(driver.findElement(locator)) : none(WebElement.class);
    }

    private By link()
    {
        return By.id(ModuleKeyUtils.addonAndModuleKey(addonKey, pageKey));
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
