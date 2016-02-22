package it.jira.servlet;

import javax.servlet.http.HttpServlet;

import com.atlassian.connect.test.jira.pageobjects.RemoteRefreshIssuePageWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.MustacheServlet;
import com.atlassian.plugin.connect.test.common.servlet.FormParameterExtractor;

import com.google.common.collect.Lists;

import static com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets.wrapContextAwareServlet;

/**
 * Utility methods for creating test servlets suitable for serving JIRA-Specific Connect iframes.
 */
public class JiraAppServlets {

    private static final String DASHBOARD_ITEM_ID_QUERY_PARAM = "dashboardItemId";
    private static final String DASHBOARD_ID_QUERY_PARAM = "dashboardId";

    /**
     * @return a servlet that provides a button to trigger refreshing a JIRA
     * issue page
     */
    public static HttpServlet refreshIssuePageButtonServlet() {
        return wrapContextAwareServlet(new MustacheServlet(RemoteRefreshIssuePageWebPanel.TEMPLATE_PATH));
    }

    /**
     * @return a servlet that will create a workflow post function
     */
    public static HttpServlet workflowPostFunctionServlet() {
        return wrapContextAwareServlet(
                new MustacheServlet("jira/iframe-workflow-post-function.mu"));
    }

    /**
     * @return a servlet that will create a workflow post function that will fail validation
     */
    public static HttpServlet failValidateWorkflowPostFunctionServlet() {
        return wrapContextAwareServlet(
                new MustacheServlet("jira/iframe-fail-validate-workflow-post-function.mu"));
    }

    public static HttpServlet dashboardItemServlet() {
        return wrapContextAwareServlet(new MustacheServlet("jira/dashboardItem/dashboard-item.mu"), Lists.newArrayList(
                new FormParameterExtractor(DASHBOARD_ITEM_ID_QUERY_PARAM),
                new FormParameterExtractor(DASHBOARD_ID_QUERY_PARAM)));
    }

    /**
     * @return a servlet that tests AP.onDialogMessage() and captures parameters sent to it.
     */
    public static HttpServlet quickCreateIssueServlet() {
        return wrapContextAwareServlet(
                new MustacheServlet("jira/iframe-quick-issue-create.mu"));
    }

    /**
     * @return a servlet that contains 3 buttons to navigate to different parts of confluence
     */
    public static HttpServlet navigatorServlet() {
        return wrapContextAwareServlet(new MustacheServlet("jira/iframe-navigator.mu"));
    }
}
