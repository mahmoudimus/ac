package it.jira.util;

import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.functest.rule.HttpUnitConfigurationRule;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

final class NavigationFactory {
    private final JIRAEnvironmentData environmentData;

    public NavigationFactory(final JIRAEnvironmentData environmentData) {
        this.environmentData = environmentData;
    }

    public Navigation createNavigation() {
        Navigation navigation = new NavigationImpl(getWebTester(environmentData), environmentData);
        navigation.login("admin", "admin");
        return navigation;
    }

    private WebTester getWebTester(JIRAEnvironmentData environmentData) {

        WebTester webTester = WebTesterFactory.createNewWebTester(environmentData);
        HttpUnitConfigurationRule.restoreDefaults();
        webTester.beginAt("/");

        return webTester;
    }
}
