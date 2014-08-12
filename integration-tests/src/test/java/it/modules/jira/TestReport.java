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
import hudson.plugins.jira.soap.RemoteProject;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestReport extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();

    private static final TestReportInfo firstTestReport = new TestReportInfo("report", "description", "some-test-report", "projectKey")
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return project.getKey();
        }
    };
    private static final TestReportInfo secondTestReport = new TestReportInfo("another-test-report", "some description", "another-report-key", "projectId")
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return String.valueOf(project.getId());
        }
    };

    private static final TestReportInfo[] reportInfos = new TestReportInfo[] {firstTestReport, secondTestReport};

    private static ConnectRunner runner;

    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        runner = new ConnectRunner(product, PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModules("jiraReports",
                    ReportModuleBean.newBuilder()
                        .withWeight(100)
                        .withUrl("/report?projectKey={project.key}")
                        .withDescription(new I18nProperty(firstTestReport.description, "description i18n"))
                        .withName(new I18nProperty(firstTestReport.title, "report i18n"))
                        .withKey(firstTestReport.key)
                        .build(),
                    ReportModuleBean.newBuilder()
                        .withWeight(101)
                        .withUrl("/report?projectId={project.id}")
                        .withDescription(new I18nProperty(secondTestReport.description, "second description i18n"))
                        .withName(new I18nProperty(secondTestReport.title, "report i18n"))
                        .withKey(secondTestReport.key)
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
    public void allConnectReportsDisplayedOnReportsList()
    {
        final ProjectReportPage projectReportPage = goToProjectsReportPage();
        List<ReportLink> reports = projectReportPage.getReports();

        assertThat(reports, hasItems(reportLinkTypeSafeMatcher(firstTestReport),
                reportLinkTypeSafeMatcher(secondTestReport)));
    }

    @Test
    public void connectReportDisplaysIframe()
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(reportInfo);
            assertThat(embeddedReportPage.getMessage(), is("Success"));
        }
    }

    @Test
    public void contextParameterPassedToReport()
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            final ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(reportInfo);
            final Map<String, String> queryParams = embeddedReportPage.getIframeQueryParams();

            assertThat(queryParams, hasEntry(equalTo(reportInfo.contextParam), equalTo(reportInfo.getExpectedContextParamValue(project))));
        }
    }

    private ProjectReportPage goToProjectsReportPage()
    {
        return loginAndVisit(TestUser.ADMIN, ProjectReportPage.class, project.getKey());
    }

    private ConnectAddOnEmbeddedTestPage goToEmbeddedReportPage(final TestReportInfo reportInfo)
    {
        final ProjectReportPage projectReportPage = goToProjectsReportPage();
        ReportLink reportLink = Iterables.find(projectReportPage.getReports(), new Predicate<ReportLink>()
        {
            @Override
            public boolean apply(final ReportLink reportLink)
            {
                return reportLinkTypeSafeMatcher(reportInfo).matches(reportLink);
            }
        });

        return reportLink.open(PLUGIN_KEY, reportInfo.key);
    }

    private TypeSafeMatcher<ReportLink> reportLinkTypeSafeMatcher(final TestReportInfo reportInfo)
    {
        return new TypeSafeMatcher<ReportLink>()
        {
            @Override
            protected boolean matchesSafely(final ReportLink link)
            {
                return link.getDescription().equals(reportInfo.description) && link.getTitle().contains(reportInfo.title);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Report links should contains report " + reportInfo.title);
            }
        };
    }

    static abstract class TestReportInfo
    {
        final String title;
        final String description;
        final String key;
        final String contextParam;

        private TestReportInfo(final String title, final String description, final String key, final String contextParam)
        {
            this.title = title;
            this.description = description;
            this.key = key;
            this.contextParam = contextParam;
        }

        public abstract String getExpectedContextParamValue(final RemoteProject project);
    }
}
