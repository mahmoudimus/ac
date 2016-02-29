package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import it.jira.project.TestProject;
import it.jira.util.JiraTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.rmi.RemoteException;

public class JiraTestBase {
    protected final static JiraTestedProduct product = JiraTestHelper.PRODUCT;

    protected static TestProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        project = JiraTestHelper.addProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        product.backdoor().project().deleteProject(project.getKey());
    }
}
