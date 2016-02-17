package it.jira;

import java.rmi.RemoteException;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import it.jira.project.TestProject;
import it.jira.util.JiraTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JiraTestBase
{
    protected final static JiraTestedProduct product = JiraTestHelper.PRODUCT;

    protected static TestProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        project = JiraTestHelper.addProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        product.backdoor().project().deleteProject(project.getKey());
    }
}
