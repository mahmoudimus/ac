package com.atlassian.plugin.connect.test.jira.pageobjects;

import javax.inject.Inject;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil;
import com.atlassian.webdriver.AtlassianWebDriver;

import com.google.common.base.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static java.lang.String.format;

public final class JiraGeneralPage implements GeneralPage {
    private static final By MORE_MENU = By.linkText("More");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    private final String pageKey;
    private final String addonKey;

    private final Supplier<Option<PageElement>> link = new Supplier<Option<PageElement>>() {
        @Override
        public Option<PageElement> get() {
            dismissCreateProjectDialogIfPresent();
            expandMoreMenuIfExists();
            return some(elementFinder.find(link()));
        }
    };

    public JiraGeneralPage(String pageKey, String addonKey) {
        this.pageKey = pageKey;
        this.addonKey = addonKey;
    }

    @Override
    public ConnectAddonEmbeddedTestPage clickAddonLink() {
        final PageElement linkElement = findLinkElement();
        RemotePageUtil.clickAddonLinkWithKeyboardFallback(linkElement);
        logger.debug("Link '{}' was found and clicked.", linkElement);
        return pageBinder.bind(ConnectAddonEmbeddedTestPage.class, addonKey, pageKey, true);
    }

    @Override
    public PageElement findLinkElement() {
        return link.get().get();
    }

    public void clickRemotePluginLinkWithoutBinding() {
        link.get().fold(
                () -> {
                    throw new IllegalStateException(format("Could not find link '%s'", link()));
                },
                actualLink -> {
                    actualLink.click();
                    logger.debug("Link '{}' was found and clicked.", actualLink);

                    return null;
                }
        );
    }

    public String getRemotePluginLinkHref() {
        return link.get().fold(
                () -> {
                    throw new IllegalStateException(format("Could not find link '%s'", link()));
                },
                actualLink -> actualLink.getAttribute("href")
        );
    }

    private boolean expandMoreMenuIfExists() {
        return getElement(MORE_MENU).fold(
                () -> {
                    logger.debug("'More' menu was not found. Nothing to expand.");
                    return false;
                },
                moreMenuElement -> {
                    final String cssClass = " " + moreMenuElement.getAttribute("class") + " ";
                    if (!cssClass.contains(" active ")) {
                        logger.debug("'More' menu found and is not active ({}). Expanding as our link might be in there.", cssClass);
                        moreMenuElement.click();
                    } else {
                        logger.debug("'More' menu found, already active and expanded ({}).", cssClass);
                    }
                    return true;
                }
        );
    }

    private Option<WebElement> getElement(By locator) {
        return driver.elementExists(locator) ? some(driver.findElement(locator)) : none(WebElement.class);
    }

    private By link() {
        return By.id(ModuleKeyUtils.addonAndModuleKey(addonKey, pageKey));
    }

    private void dismissCreateProjectDialogIfPresent() {
        clickElementIfExist(createProjectDialogCancelButton());
    }

    private boolean clickElementIfExist(By by) {
        final boolean exists = driver.elementExists(by);
        if (exists) {
            logger.debug("Clicking element '{}'.", by);
            driver.findElement(by).click();
        } else {
            logger.debug("Element '{}' was NOT found, therefore not clicked.", by);
        }
        return exists;
    }

    private By createProjectDialogCancelButton() {
        return By.className("button-panel-cancel-link");
    }
}
