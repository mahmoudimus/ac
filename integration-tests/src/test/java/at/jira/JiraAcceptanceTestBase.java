package at.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;

public class JiraAcceptanceTestBase
{
    protected static final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);
    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());
}
