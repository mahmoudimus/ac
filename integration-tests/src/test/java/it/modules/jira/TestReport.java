package it.modules.jira;

import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.tests.FuncTestHelper;
import com.atlassian.json.schema.util.StringUtil;
import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.*;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractProjectReportPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.plugins.jira.soap.RemoteProject;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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

    private static final TestReportInfo firstTestReport = new TestReportInfo("report", "description", "some-test-report", "projectKey", ReportCategory.AGILE)
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return project.getKey();
        }
    };
    private static final TestReportInfo secondTestReport = new TestReportInfo("another-test-report", "some description", "another-report-key", "projectId", ReportCategory.OTHER)
    {
        @Override
        public String getExpectedContextParamValue(final RemoteProject project)
        {
            return String.valueOf(project.getId());
        }
    };

    private static final TestReportInfo[] reportInfos = new TestReportInfo[] { firstTestReport, secondTestReport };

    private static ConnectRunner runner;

    private static Backdoor backdoor = new FuncTestHelper().backdoor;

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
                                .withReportCategory(ReportCategory.AGILE)
                                .withThumbnailUrl("http://localhost:2990/jira/images/64jira.png")
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
    public void allConnectReportsDisplayedOnLegacyReportsList()
    {
        testAllConnectReportsDisplayedOnReportPage(LegacyProjectReportPage.class);
    }

    @Test
    public void connectLegacyReportDisplaysIframe()
    {
        connectReportDisplaysIframe(LegacyProjectReportPage.class);
    }

    @Test
    public void contextParameterPassedToLegacyReport()
    {
        contextParameterPassedToReport(LegacyProjectReportPage.class);
    }

    @Test
    public void allConnectReportsDisplayedOnProjectCentricNavigationReportPage()
    {
        testWithEnabledProjectCentricNavigation(new Runnable(){
            @Override
            public void run() {
                testAllConnectReportsDisplayedOnReportPage(ProjectCentricNavigationProjectReportPage.class);
            }
        });
    }

    @Test
    public void connectProjectOrientedNavigationReportDisplaysIframe()
    {
        testWithEnabledProjectCentricNavigation(new Runnable(){
            @Override
            public void run() {
                connectReportDisplaysIframe(ProjectCentricNavigationProjectReportPage.class);
            }
        });
    }

    @Test
    public void contextParameterPassedToProjectOrientedNavigationReport()
    {
        testWithEnabledProjectCentricNavigation(new Runnable(){
            @Override
            public void run() {
                contextParameterPassedToReport(ProjectCentricNavigationProjectReportPage.class);
            }
        });
    }

    private void testWithEnabledProjectCentricNavigation(final Runnable testFunction)
    {
        try{
            backdoor.darkFeatures().enableForSite("com.atlassian.jira.projects.ProjectCentricNavigation");
            testFunction.run();
        }finally {
            backdoor.darkFeatures().disableForSite("com.atlassian.jira.projects.ProjectCentricNavigation");
        }
    }

    private <T extends AbstractProjectReportPage> void testAllConnectReportsDisplayedOnReportPage(final Class<T> page)
    {
        final AbstractProjectReportPage reportPage = goToProjectsReportPage(page);
        List<ReportLink> reports = reportPage.getReports();

        assertThat(reports, hasItems(equalToReport(firstTestReport), equalToReport(secondTestReport)));
    }

    public <T extends AbstractProjectReportPage> void connectReportDisplaysIframe(final Class<T> page)
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(page, reportInfo);
            assertThat(embeddedReportPage.getMessage(), is("Success"));
        }
    }

    public <T extends AbstractProjectReportPage> void contextParameterPassedToReport(final Class<T> page)
    {
        for (TestReportInfo reportInfo : reportInfos)
        {
            final ConnectAddOnEmbeddedTestPage embeddedReportPage = goToEmbeddedReportPage(page, reportInfo);
            final Map<String, String> queryParams = embeddedReportPage.getIframeQueryParams();

            assertThat(queryParams, hasEntry(equalTo(reportInfo.contextParam), equalTo(reportInfo.getExpectedContextParamValue(project))));
        }
    }

    private <T extends AbstractProjectReportPage> T goToProjectsReportPage(final Class<T> page)
    {
        return loginAndVisit(TestUser.ADMIN, page, project.getKey());
    }

    private <T extends AbstractProjectReportPage> ConnectAddOnEmbeddedTestPage goToEmbeddedReportPage(final Class<T> page, final TestReportInfo reportInfo)
    {
        final AbstractProjectReportPage abstractProjectReportPage = goToProjectsReportPage(page);
        ReportLink reportLink = Iterables.find(abstractProjectReportPage.getReports(), new Predicate<ReportLink>()
        {
            @Override
            public boolean apply(final ReportLink reportLink)
            {
                return equalToReport(reportInfo).matches(reportLink);
            }
        });

        return reportLink.open(PLUGIN_KEY, reportInfo.key);
    }

    private Matcher<ReportLink> equalToReport(final TestReportInfo reportInfo)
    {

        return new TypeSafeMatcher<ReportLink>()
        {
            @Override
            protected boolean matchesSafely(final ReportLink link)
            {
                final boolean onlyLegacyParams = link.getClass().equals(LegacyReportLink.class);
                return matchCommonReportLinkPrams(link) && (onlyLegacyParams || matchProjectCentricNavigationReportParam(link));
            }

            private boolean matchCommonReportLinkPrams(final ReportLink link)
            {
                return link.getDescription().equals(reportInfo.description)
                        && link.getTitle().contains(reportInfo.title);
            }

            private boolean matchProjectCentricNavigationReportParam(final ReportLink link)
            {
                return link.getReportCategory().equals(reportInfo.reportCategory)
                        && StringUtil.isNotBlank(link.getThumbnailCssClass());
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
