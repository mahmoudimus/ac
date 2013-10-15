package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;

/**
 * @since version
 */
public class AppRunnerIssueTab
{
    public static final String JIRA = "http://localhost:2990/jira";
    public static final String CONFLUENCE = "http://localhost:1990/confluence";

    public static void main(String[] args)
    {
        try
        {
            ConnectCapabilitiesRunner remotePlugin = new ConnectCapabilitiesRunner(JIRA,"my-plugin")
                    .addCapability(newWebItemBean()
                            .withName(new I18nProperty("AC General Web Item", "ac.gen"))
                            .withLocation("system.top.navigation.bar")
                            .withWeight(1)
                            .withLink("/irwi")
                            .build())
                    .addCapability(newWebItemBean()
                            .withName(new I18nProperty("Quick project link", "ac.qp"))
                            .withLocation("system.top.navigation.bar")
                            .withWeight(1)
                            .withLink(JIRA + "/browse/ACDEV-1234")
                            .build())
                    .addCapability(newWebItemBean()
                            .withName(new I18nProperty("google link","ac.gl"))
                            .withLocation("system.top.navigation.bar")
                            .withWeight(1)
                            .withLink("http://www.google.com")
                            .build())
                    .addCapability(IssueTabPageCapabilityBean.newIssueTabPageBean()
                            .withName(new I18nProperty("My Tab","ac.mytab"))
                            .withWeight(1)
                            .withUrl("/irwi")
                            .build())
                    .start();
            while (true)
            {
                //do nothing
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static final class MessageServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/plain");
            resp.getWriter().write(req.getParameter("message"));
            resp.getWriter().close();
        }
    }
}
