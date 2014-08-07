package it.modules.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.servlet.ConnectAppServlets;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class TestReport extends TestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "some-test-report";
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String REPORT_TITLE = "report";
    private static final String REPORT_DESCIRPTION = "description";

    private static ConnectRunner remotePlugin;
    private long projectId;

    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        remotePlugin = new ConnectRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
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
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUpTest() throws Exception
    {
        projectId = backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
    }

    @After
    public void after()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testJiraReport()
    {
        ProjectReportPage projectReportTab = jira().quickLoginAsAdmin(ProjectReportPage.class, PROJECT_KEY);
        List<ReportLink> reports = projectReportTab.getReports();
        assertThat(reports, Matchers.hasItem(reportLinkTypeSafeMatcher(REPORT_DESCIRPTION, REPORT_TITLE)));

        ReportLink reportLink = Iterables.find(reports, new Predicate<ReportLink>()
        {
            @Override
            public boolean apply(final ReportLink reportLink)
            {
                return reportLinkTypeSafeMatcher(REPORT_DESCIRPTION, REPORT_TITLE).matches(reportLink);
            }
        });
        ConnectAddOnEmbeddedTestPage embeddedTestPage = reportLink.open();
        assertThat(embeddedTestPage.getMessage(), is("Success"));

        Map<String, String> queryParams = embeddedTestPage.getIframeQueryParams();
        assertThat(queryParams, hasEntry(equalTo("projectKey"), equalTo(PROJECT_KEY)));
        assertThat(queryParams, hasEntry(equalTo("projectId"), equalTo(String.valueOf(projectId))));
    }

    public static class ProjectReportPage implements Page
    {
        private final String projectKey;

        @Inject private PageElementFinder elementFinder;

        public ProjectReportPage(final String projectKey)
        {
            this.projectKey = projectKey;
        }

        @WaitUntil
        public TimedCondition isOpen()
        {
            return elementFinder.find(By.id("project-tab")).timed().isPresent();
        }

        @Override
        public String getUrl()
        {
            return String.format("/browse/%s/?selectedTab=com.atlassian.jira.jira-projects-plugin:reports-panel", projectKey);
        }

        public List<ReportLink> getReports()
        {
            List<PageElement> reportLinkElements = elementFinder.findAll(By.className("version-block-container"));
            return Lists.transform(reportLinkElements, new Function<PageElement, ReportLink>()
            {
                @Override
                public ReportLink apply(final PageElement element)
                {
                    PageElement reportTitle = element.find(By.className("version-title"));
                    String title = reportTitle.getText();
                    PageElement reportLink = reportTitle.find(By.tagName("a"));
                    String description = element.find(By.className("version-description")).getText();
                    return pageBinder().bind(ReportLink.class, title, description, reportLink);
                }
            });
        }
    }

    public static class ReportLink
    {
        private final String title;
        private final String description;
        private final PageElement reportLink;

        @Inject
        private PageBinder pageBinder;

        public ReportLink(final String title, final String description, final PageElement reportLink)
        {
            this.title = title;
            this.description = description;
            this.reportLink = reportLink;
        }

        @Init
        public void init()
        {
            waitUntilTrue(reportLink.timed().isPresent());
        }

        public ConnectAddOnEmbeddedTestPage open()
        {
            reportLink.click();
            return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, PLUGIN_KEY, MODULE_KEY, true);
        }

        public String getTitle()
        {
            return title;
        }

        public String getDescription()
        {
            return description;
        }
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
