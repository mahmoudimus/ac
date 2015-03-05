package it.modules.jira;

import com.atlassian.jira.projects.pageobjects.webdriver.page.ReportsPage;
import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteProject;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestReport extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();

    private static final TestReportInfo firstTestReport = new TestReportInfo("Agile Test Report", "description", "agile-test-report", "projectKey", ReportCategory.AGILE)
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return project.getKey();
        }
    };
    private static final TestReportInfo secondTestReport = new TestReportInfo("Other Test Report", "some description", "other-test-report", "projectId", ReportCategory.OTHER)
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return String.valueOf(project.getId());
        }
    };

    private static final TestReportInfo[] reportInfos = new TestReportInfo[] { firstTestReport, secondTestReport };

    private static ConnectRunner addon;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        product.getTester().getDriver().manage().deleteAllCookies();

        addon = new ConnectRunner(product, ADDON_KEY)
                .setAuthenticationToNone()
                .addModules("jiraReports",
                        ReportModuleBean.newBuilder()
                                .withWeight(100)
                                .withUrl("/report?projectKey={project.key}")
                                .withDescription(new I18nProperty(firstTestReport.description, null))
                                .withName(new I18nProperty(firstTestReport.title, null))
                                .withKey(firstTestReport.key)
                                .withReportCategory(firstTestReport.reportCategory)
                                .withThumbnailUrl("http://localhost:2990/jira/images/64jira.png")
                                .build(),
                        ReportModuleBean.newBuilder()
                                .withWeight(101)
                                .withUrl("/report?projectId={project.id}")
                                .withDescription(new I18nProperty(secondTestReport.description, null))
                                .withName(new I18nProperty(secondTestReport.title, null))
                                .withKey(secondTestReport.key)
                                .build())
                .addRoute("/report", ConnectAppServlets.apRequestServlet())
                .start();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void allConnectReportsDisplayedOnProjectCentricNavigationReportPage()
    {
        ReportsPage reportsPage = goToProjectReportPage();
        for (TestReportInfo reportInfo : reportInfos)
        {
            ReportsPage.Report report = getReportFromReportsPage(reportsPage, reportInfo);
            assertThat(report.getDescription().byDefaultTimeout(), is(reportInfo.description));
        }
    }

    @Test
    public void connectProjectOrientedNavigationReportDisplaysIframe()
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(reportInfo);
            assertThat(embeddedReportPage.getMessage(), is("Success"));
        }
    }

    @Test
    public void contextParameterPassedToProjectOrientedNavigationReport()
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            final ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(reportInfo);
            final Map<String, String> queryParams = embeddedReportPage.getIframeQueryParams();

            assertThat(queryParams, hasEntry(equalTo(reportInfo.contextParam),
                    equalTo(reportInfo.getExpectedContextParamValue(project))));
        }
    }

    private ReportsPage goToProjectReportPage()
    {
        return product.visit(ReportsPage.class, project.getKey());
    }

    private ReportsPage.Report getReportFromReportsPage(ReportsPage reportsPage, TestReportInfo reportInfo)
    {
        return reportsPage.getReportsSection(reportInfo.reportCategory.getKey()).getReport(reportInfo.title);
    }

    private ConnectAddOnEmbeddedTestPage goToEmbeddedReportPage(TestReportInfo reportInfo)
    {
        ReportsPage reportsPage = goToProjectReportPage();
        connectPageOperations.dismissAnyAuiDialog();
        ReportsPage.Report report = getReportFromReportsPage(reportsPage, reportInfo);
        return report.visit(ConnectAddOnEmbeddedTestPage.class, ADDON_KEY, reportInfo.key, true);
    }

    private static abstract class TestReportInfo
    {
        final String title;
        final String description;
        final String key;
        final String contextParam;
        final ReportCategory reportCategory;

        private TestReportInfo(final String title, final String description, final String key, final String contextParam, final ReportCategory reportCategory)
        {
            this.title = title;
            this.description = description;
            this.key = key;
            this.contextParam = contextParam;
            this.reportCategory = reportCategory;
        }

        public abstract String getExpectedContextParamValue(final RemoteProject project);
    }
}
