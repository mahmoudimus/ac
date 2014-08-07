package it.modules.jira;

import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.ProjectReportPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.ReportLink;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestReport extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "some-test-report";
    private static final String REPORT_TITLE = "report";
    private static final String REPORT_DESCIRPTION = "description";

    private static ConnectRunner runner;

    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        runner = new ConnectRunner(product, PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule("jiraReports", ReportModuleBean.newBuilder()
                        .withWeight(100)
                        .withUrl("/report?projectKey={project.key}&projectId={project.id}")
                        .withDescription(new I18nProperty(REPORT_DESCIRPTION, "description i18n"))
                        .withName(new I18nProperty(REPORT_TITLE, "report i18n"))
                        .withKey(MODULE_KEY)
                        .build())
                .addRoute("/report", ConnectAppServlets.apRequestServlet())
                .start();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void connectReportDisplayedOnReportsList()
    {
        final ProjectReportPage projectReportPage = goToProjectsReportPage();
        List<ReportLink> reports = projectReportPage.getReports();
        assertThat(reports, Matchers.hasItem(reportLinkTypeSafeMatcher(REPORT_DESCIRPTION, REPORT_TITLE)));
    }

    @Test
    public void connectReportDisplaysIframe()
    {
        ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage();

        assertThat(embeddedReportPage.getMessage(), is("Success"));

    }

    @Test
    public void projectContextParametersPassedToReport()
    {
        final ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage();

        final Map<String, String> queryParams = embeddedReportPage.getIframeQueryParams();
        assertThat(queryParams, hasEntry(equalTo("projectKey"), equalTo(project.getKey())));
        assertThat(queryParams, hasEntry(equalTo("projectId"), equalTo(String.valueOf(project.getId()))));
    }

    private ProjectReportPage goToProjectsReportPage()
    {
        return loginAndVisit(TestUser.ADMIN, ProjectReportPage.class, project.getKey());
    }

    private ConnectAddOnEmbeddedTestPage goToEmbeddedReportPage()
    {
        final ProjectReportPage projectReportPage = goToProjectsReportPage();
        ReportLink reportLink = Iterables.find(projectReportPage.getReports(), new Predicate<ReportLink>()
        {
            @Override
            public boolean apply(final ReportLink reportLink)
            {
                return reportLinkTypeSafeMatcher(REPORT_DESCIRPTION, REPORT_TITLE).matches(reportLink);
            }
        });

        return reportLink.open(PLUGIN_KEY, MODULE_KEY);
    }

    private TypeSafeMatcher<ReportLink> reportLinkTypeSafeMatcher(final String description, final String title)
    {
        return new TypeSafeMatcher<ReportLink>()
        {
            @Override
            protected boolean matchesSafely(final ReportLink link)
            {
                return link.getDescription().equals(description) && link.getTitle().contains(title);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Report links should contains report " + REPORT_TITLE);
            }
        };
    }

}
