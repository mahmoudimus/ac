package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.jira.JiraCreateIssuePage;
import com.atlassian.labs.remoteapps.test.jira.JiraOps;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import redstone.xmlrpc.XmlRpcFault;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TestJira
{
    private static TestedProduct<WebDriverTester> product = TestedProductFactory.create(JiraTestedProduct.class);

    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    public void testIssueCreatedWebHookFired() throws IOException, JSONException, InterruptedException, XmlRpcFault
    {
        final String userName = "admin";
        product.visit(LoginPage.class).login(userName, "admin", HomePage.class);

        long projectId = JiraOps.createProject(product);
        String issueKey = product.visit(JiraCreateIssuePage.class, projectId)
                                 .summary("All your hook are belong to us")
                                 .submit()
                                 .readKeyFromPage();

        JSONObject issue = null;
        for (int x=0; x<5; x++)
        {
            JSONObject event = waitForEvent(product.getProductInstance(), "issue_created");
            issue = event.getJSONObject("issue");
            if (issueKey.equals(issue.getString("key")))
            {
                break;
            }
        }

        assertNotNull(issue);
        assertEquals(userName, issue.getString("reporterName"));
    }
}
