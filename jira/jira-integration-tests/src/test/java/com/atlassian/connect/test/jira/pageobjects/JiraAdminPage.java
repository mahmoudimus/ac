package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.atlassian.fugue.Option.some;
import static java.lang.String.format;

public final class JiraAdminPage implements AdminPage {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    private final String addonKey;
    private final String moduleKey;

    private final Supplier<Option<PageElement>> link = new Supplier<Option<PageElement>>() {
        @Override
        public Option<PageElement> get() {
            return driver.elementExists(link()) ? some(elementFinder.find(link())) : Option.<PageElement>none();
        }
    };

    public JiraAdminPage(String addonKey, String moduleKey) {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public ConnectAddonEmbeddedTestPage clickAddonLink() {
        final PageElement linkElement = link.get().get();
        linkElement.click();
        logger.debug("Link '{}' was found and clicked.", linkElement);
        return pageBinder.bind(ConnectAddonEmbeddedTestPage.class, addonKey, moduleKey, true);
    }

    @Override
    public PageElement findLinkElement() {
        return link.get().get();
    }

    public String getRemotePluginLinkHref() {
        return withLinkElement(linkElement -> linkElement.getAttribute("href"));
    }

    public String getRemotePluginLinkText() {
        return withLinkElement(PageElement::getText);
    }

    private <T> T withLinkElement(Function<PageElement, T> function) {
        return link.get().fold(
                () -> {
                    throw new IllegalStateException(format("Could not find link '%s'", link()));
                },
                function
        );
    }

    private By link() {
        return By.id(ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
    }

}
