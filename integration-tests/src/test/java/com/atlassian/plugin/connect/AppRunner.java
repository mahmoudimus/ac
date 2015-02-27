package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;

/**
 * @since 1.0
 */
public class AppRunner
{
    public static final String JIRA = "http://localhost:2990/jira";
    public static final String CONFLUENCE = "http://localhost:1990/confluence";

    public static void main(String[] args)
    {
        try
        {
            ConnectRunner runner = new ConnectRunner(JIRA,"tab-panels-plugin")
                    .setAuthenticationToNone()
                    .addModules("jiraIssueTabPanels",
                            newTabPanelBean()
                                    .withName(new I18nProperty("Name and Key", "name.and.key"))
                                    .withUrl("http://www.google.com")
                                    .build()
                            ,newTabPanelBean()
                                .withName(new I18nProperty("Name Only", null))
                                .withUrl("http://www.google.com")
                                .build()
                    )
                    .addModules("jiraProjectTabPanels",
                            newTabPanelBean()
                                    .withName(new I18nProperty("Name and Key", "name.and.key"))
                                    .withUrl("http://www.google.com")
                                    .build()
                            ,newTabPanelBean()
                            .withName(new I18nProperty("Name Only", null))
                            .withUrl("http://www.google.com")
                            .build()
                    )
                    .addModules("jiraProfileTabPanels",
                            newTabPanelBean()
                                    .withName(new I18nProperty("Name and Key", "name.and.key"))
                                    .withUrl("http://www.google.com")
                                    .build()
                            ,newTabPanelBean()
                            .withName(new I18nProperty("Name Only", null))
                            .withUrl("http://www.google.com")
                            .build()
                    )
                                    .start();


//            ConnectRunner remotePlugin = new ConnectRunner(JIRA,"my-plugin")
//                    .addModules("webItems",
//                            newWebItemBean()
//                                .withName(new I18nProperty("AC General Web Item", "ac.gen"))
//                                .withLocation("system.top.navigation.bar")
//                                .withWeight(1)
//                                .withUrl("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}")
//                                .build(),
//                            newWebItemBean()
//                                .withContext(AddOnUrlContext.product)
//                                .withName(new I18nProperty("Quick project link", "ac.qp"))
//                                .withLocation("system.top.navigation.bar")
//                                .withWeight(1)
//                                .withUrl("/browse/ACDEV-1234")
//                                .build(),
//                            newWebItemBean()
//                                .withName(new I18nProperty("google link", "ac.gl"))
//                                .withLocation("system.top.navigation.bar")
//                                .withWeight(1)
//                                .withUrl("http://www.google.com")
//                                .withConditions(
//                                        newSingleConditionBean().withCondition("user_is_logged_in").build(),
//                                        newSingleConditionBean().withCondition("/onlyBettyCondition").build()
//                                ).build())
//                    .addModules("webPanels",
//                            newWebPanelBean()
//                                    .withName(new I18nProperty("clock", "ac.clock"))
//                                    .withLocation("atl.jira.view.issue.right.context")
//                                    .withWeight(1)
//                                    .withUrl("http://free.timeanddate.com/clock/i3w2dcse/n109/fn2/tcccc/bo2/ts1/ta1")
//                                    .withConditions(
//                                            newSingleConditionBean().withCondition("user_is_logged_in").build(),
//                                            newSingleConditionBean().withCondition("/onlyBettyCondition").build()
//                                    )
//                                    .build(),
//                            newWebPanelBean()
//                                    .withName(new I18nProperty("another clock", "ac.clock"))
//                                    .withLocation("atl.jira.view.issue.right.context")
//                                    .withWeight(1)
//                                    .withUrl("http://free.timeanddate.com/clock/i3w2e3ys/n109/szw110/szh110/hocf00/hbw0/hfcc00/cf100/hnca32/fas20/facfff/fdi86/mqcfff/mqs2/mql3/mqw4/mqd70/mhcfff/mhs2/mhl3/mhw4/mhd70/mmv0/hhcfff/hhs2/hmcfff/hms2/hsv0")
//                                    .build()
//                    )
//                    .addModules("generalPages",
//                            newPageBean()
//                                    .withName(new I18nProperty("My Awesome Page", "my.awesome.page"))
//                                    .withUrl("/pg?page_id={page.id}")
//                                    .withWeight(1234)
//                                    .build(),
//                            newPageBean()
//                                    .withName(new I18nProperty("Another Awesome Page", "another.awesome.page"))
//                                    .withUrl("/pg?page_id={page.id}")
//                                    .withWeight(1234)
//                                    .build())
//                    .addModules("adminPages",
//                            newPageBean()
//                                    .withName(new I18nProperty("My Admin Page", "my.admin.page"))
//                                    .withUrl("/pg")
//                                    .withWeight(1234)
//                                    .build(),
//                            newPageBean()
//                                    .withName(new I18nProperty("Another Admin Page", "another.admin.page"))
//                                    .withUrl("/pg")
//                                    .withWeight(1234)
//                                    .build())
//                    .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet("betty"))
//                    .addRoute("/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}",
//                            ConnectAppServlets.helloWorldServlet())
//                    .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
//                    .start();
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
