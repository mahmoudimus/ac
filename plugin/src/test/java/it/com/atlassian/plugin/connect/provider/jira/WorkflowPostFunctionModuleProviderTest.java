package it.com.atlassian.plugin.connect.provider.jira;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WorkflowPostFunctionModuleProvider;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionModuleProviderTest
{
    public static final String CONTEXT_PATH = "http://ondemand.com/jira";
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Post Function";
    public static final String MODULE_KEY = "my-post-function";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String PROJECT_KEY = "TEST";
    public static final Long PROJECT_ID = 1234L;
    private static final String SRC = "src:";

    private final WorkflowPostFunctionModuleProvider workflowPostFunctionModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private HttpServletRequest servletRequest;

    public WorkflowPostFunctionModuleProviderTest(WorkflowPostFunctionModuleProvider workflowPostFunctionModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                                  IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.workflowPostFunctionModuleProvider = workflowPostFunctionModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @BeforeClass
    public void setup()
    {
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);

        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = newWorkflowPostFunctionBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withDescription(new I18nProperty("My Description", "my.function.desc"))
                .withTriggered(new UrlBean("/triggered"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("jiraWorkflowPostFunctions", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            //NOTE: we have to get the descriptor from the enabled plugin instead of using the module provider directly
            //due to a crappy class forname call in the descript itself
            WorkflowFunctionModuleDescriptor descriptor = (WorkflowFunctionModuleDescriptor) plugin.getModuleDescriptor(MODULE_KEY);

            ModuleContextParameters moduleContextParameters = new JiraModuleContextParametersImpl();
            StringWriter sw = new StringWriter();
            IFrameRenderStrategy createRenderStrategy = iFrameRenderStrategyRegistry.get(PLUGIN_KEY, MODULE_KEY, RESOURCE_NAME_INPUT_PARAMETERS);
            createRenderStrategy.render(moduleContextParameters, sw);

            // I'm not gonna lie to you. It's gonna get weird. Two dragons.
            // Sadly as we don't have a clean REST service but instead use velocity to create some html and js this is impossible to test unflakely
            final String velocityFart = sw.toString();
            final int startIndex = velocityFart.indexOf(SRC);
            final int endIndex = velocityFart.indexOf(",", startIndex);
            final String iframeUrlStr = velocityFart.substring(startIndex + SRC.length() + 1, endIndex - 1);
            final URI iframeUrl = new URI(iframeUrlStr);
            final String baseUrl = iframeUrl.getScheme() + "://" + iframeUrl.getAuthority();

            assertThat(baseUrl, is(BASE_URL));
            assertThat(iframeUrl.getPath(), is("/create"));

//            Map<String, Object> context = new HashMap<String, Object>();
//            Project project = mock(Project.class);
//            when(project.getKey()).thenReturn(PROJECT_KEY);
//            when(project.getId()).thenReturn(PROJECT_ID);
//
//            context.put("project",project);
//
//            String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);
//
//            assertTrue("wrong url prefix. expected: " + BASE_URL + "/my/addon but got: " + convertedUrl,convertedUrl.startsWith(BASE_URL + "/my/addon"));
//            assertTrue("project key not found in: " + convertedUrl, convertedUrl.contains("myProject=" + PROJECT_KEY));
        }
        finally
        {
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }
}
