package it.com.atlassian.plugin.connect.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraWebitemModuleProviderTest extends AbstractConnectAddonTest
{
    public static final String PROJECT_KEY = "TEST";
    public static final Long PROJECT_ID = 1234L;

    public JiraWebitemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception
    {
        WebItemModuleDescriptor descriptor = registerWebItem("myProject={project.key}", "atl.admin/menu");

        descriptor.enabled();

        Map<String, Object> context = ImmutableMap.<String, Object>of("project", project(PROJECT_ID, PROJECT_KEY));

        String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);

        assertTrue("wrong url prefix. expected: " + BASE_URL + ADDON_PATH + ", but got: " + convertedUrl, convertedUrl.startsWith(BASE_URL + "/my/addon"));
        assertTrue("project key not found in: " + convertedUrl, convertedUrl.contains("myProject=" + PROJECT_KEY));
    }

    private Project project(long id, String key)
    {
        Project result = mock(Project.class);
        when(result.getKey()).thenReturn(key);
        when(result.getId()).thenReturn(id);
        return result;
    }
}