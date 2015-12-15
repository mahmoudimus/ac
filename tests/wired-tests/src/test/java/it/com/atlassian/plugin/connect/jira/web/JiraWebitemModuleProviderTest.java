package it.com.atlassian.plugin.connect.jira.web;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraWebitemModuleProviderTest extends AbstractConnectAddonTest
{
    private final JiraTestUtil jiraTestUtil;

    public JiraWebitemModuleProviderTest(WebItemModuleProvider webItemModuleProvider,
                                         TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                         JiraTestUtil jiraTestUtil)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
        this.jiraTestUtil = jiraTestUtil;
    }

    @Test
    public void singleAddonLinkWithContextPrams() throws Exception
    {
        Project project = jiraTestUtil.createProject();
        WebItemModuleDescriptor descriptor = registerWebItem("myProject={project.key}", "atl.admin/menu");

        descriptor.enabled();

        Map<String, Object> context = ImmutableMap.<String, Object>of("project", project);

        String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);

        String expectedUrlPrefix = UriBuilder.fromPath(CONTEXT_PATH).path(RedirectServletPath.forModule(PLUGIN_KEY, MODULE_KEY)).build().toString();
        assertTrue("wrong url prefix. expected: " + expectedUrlPrefix + ", but got: " + convertedUrl, convertedUrl.startsWith(expectedUrlPrefix));
        assertTrue("project key not found in: " + convertedUrl, convertedUrl.contains("project.key=" + project.getKey()));
    }
}