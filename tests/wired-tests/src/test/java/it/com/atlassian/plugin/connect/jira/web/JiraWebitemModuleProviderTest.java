package it.com.atlassian.plugin.connect.jira.web;

import java.util.Map;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraWebitemModuleProviderTest extends AbstractConnectAddonTest {
    private final JiraTestUtil jiraTestUtil;

    public JiraWebitemModuleProviderTest(WebItemModuleProvider webItemModuleProvider,
                                         TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                         JiraTestUtil jiraTestUtil) {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
        this.jiraTestUtil = jiraTestUtil;
    }

    @Test
    public void singleAddonLinkWithContextPrams() throws Exception {
        Project project = jiraTestUtil.createProject();
        WebItemModuleDescriptor descriptor = registerWebItem("myProject={project.key}", "atl.admin/menu");

        descriptor.enabled();

        Map<String, Object> context = ImmutableMap.<String, Object>of("project", project);

        String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);

        assertThat(convertedUrl, containsString(RedirectServletPath.forModule(PLUGIN_KEY, MODULE_KEY)));
        assertThat(convertedUrl, containsString("project.key=" + project.getKey()));
    }
}