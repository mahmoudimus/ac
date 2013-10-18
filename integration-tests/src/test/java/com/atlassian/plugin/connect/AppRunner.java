package com.atlassian.plugin.connect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.atlassian.plugin.connect.test.server.module.Condition;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;
import com.atlassian.plugin.connect.test.server.module.RemoteWebItemModule;

import it.MyContextAwareWebPanelServlet;
import it.TestPageModules;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;

/**
 * @since version
 */
public class AppRunner
{
    public static final String JIRA = "http://localhost:2990/jira";
    public static final String CONFLUENCE = "http://localhost:1990/confluence";

    public static void main(String[] args)
    {
        try
        {
//            AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(JIRA,"xml-condition-plugin")
//                    .addOAuth()
//                    .addPermission("resttest")
//                    .add(GeneralPageModule.key("remotePluginGeneral")
//                                          .name("Remotable Plugin app1 General")
//                                          .path("/rpg")
//                                          .linkName("Remotable Plugin app1 General Link")
//                                          .iconUrl("/public/sandcastles.jpg")
//                                          .height("600")
//                                          .width("700")
//                                          .resource(newMustacheServlet("iframe.mu")))
//                    .add(GeneralPageModule.key("amdTest")
//                                          .name("AMD Test app1 General")
//                                          .path("/amdTest")
//                                          .resource(newMustacheServlet("amd-test.mu")))
//                    .add(GeneralPageModule.key("onlyBetty")
//                                          .name("Only Betty")
//                                          .path("/ob")
//                                          .conditions(Condition.name("user_is_logged_in"), Condition.at("/onlyBettyCondition").resource(new TestPageModules.OnlyBettyConditionServlet()))
//                                          .resource(newMustacheServlet("iframe.mu")))
//                    .add(DialogPageModule.key("remotePluginDialog")
//                                         .name("Remotable Plugin app1 Dialog")
//                                         .path("/rpd")
//                                         .resource(newMustacheServlet("dialog.mu")))
//                    .add(GeneralPageModule.key("sizeToParent")
//                                          .name("Size to parent general page")
//                                          .path("/fsg")
//                                          .resource(newMustacheServlet("iframe-size-to-parent.mu")))
//                    .add(DialogPageModule.key("sizeToParentDialog")
//                                         .name("Size to parent dialog page")
//                                         .path("/fsg")
//                                         .resource(newMustacheServlet("iframe-size-to-parent.mu")))
//                    .start();


            ConnectCapabilitiesRunner remotePlugin = new ConnectCapabilitiesRunner(JIRA,"my-plugin2")
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
                            .withConditions(
                                    newSingleConditionBean().withCondition("user_is_logged_in").build()
                                    , newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                            )
                            .build())
                    .addCapability(newWebItemBean()
                            .withName(new I18nProperty("google link", "ac.gl"))
                            .withLocation("system.top.navigation.bar")
                            .withWeight(1)
                            .withLink("http://www.google.com")
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
