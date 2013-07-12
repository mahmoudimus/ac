package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

/**
 * Test of remote web panels in JIRA.
 */
public class TestWebPanels
{
    private static final TestedProduct<WebDriverTester> product = TestedProductFactory.create(JiraTestedProduct.class);
    private static final Backdoor backdoor = new Backdoor(new ProductInstanceBasedEnvironmentData(product.getProductInstance()));

    // web panel locations
    public static final String ISSUE_PANEL_ID = "jira-remotePluginIssuePanelPage";
    public static final String ISSUE_REMOTE_LEFT_WEB_PANEL_ID = "jira-issue-left-web-panel";
    public static final String ISSUE_REMOTE_RIGHT_WEB_PANEL_ID = "jira-issue-right-web-panel";
    public static final String PROJECT_CONFIG_HEADER_WEB_PANEL = "jira-project-config-header-web-panel";
    public static final String PROJECT_CONFIG_PANEL_ID = "jira-remoteProjectConfigPanel";

}
