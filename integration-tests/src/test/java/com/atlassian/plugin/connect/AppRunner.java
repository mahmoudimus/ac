package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;

/**
 * @since version
 */
public class AppRunner
{
    public static void main(String[] args)
    {
        try
        {
            AtlassianConnectAddOnRunner pluginFirst = new AtlassianConnectAddOnRunner("http://localhost:2990/jira", "pluginFirst")
                    .add(GeneralPageModule.key("changedPage")
                                          .name("Changed Page")
                                          .path("/page")
                                          .resource(newMustacheServlet("hello-world-page.mu")))
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
}
