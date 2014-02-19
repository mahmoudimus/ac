package it.com.atlassian.plugin.connect.provider.jira;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS;
import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderImpl.IFrameRenderStrategyImpl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionModuleProviderTest
{
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Post Function";
    public static final String MODULE_KEY = "my-post-function";
    public static final String BASE_URL = "http://my.connect.addon.com";
    private static final String SRC = "src:";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private Plugin plugin;

    public WorkflowPostFunctionModuleProviderTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                                  IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @BeforeClass
    public void setup() throws IOException
    {
        testAuthenticator.authenticateUser("admin");

        WorkflowPostFunctionModuleBean bean = newWorkflowPostFunctionBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withDescription(new I18nProperty("My Description", "my.function.desc"))
                .withTriggered(new UrlBean("/triggered"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean(BASE_URL + "/view"))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("jiraWorkflowPostFunctions", bean)
                .build();

        plugin = testPluginInstaller.installPlugin(addon);
    }

    @AfterClass
    public void cleanup() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallPlugin(plugin);
        }

    }

    @Test
    public void workflowLinksAreAbsoluteToBaseUrl() throws Exception
    {
        checkWorkflowUrlIsAbsolute(RESOURCE_NAME_INPUT_PARAMETERS, "/create");
        checkWorkflowUrlIsAbsolute(RESOURCE_NAME_EDIT_PARAMETERS, "/edit");
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteWorkflowLinksAreRejected() throws Exception
    {
        // Url's must be relative
        checkWorkflowUrlIsAbsolute(RESOURCE_NAME_VIEW, "/view");
    }

    private void checkWorkflowUrlIsAbsolute(String classifier, String workflowUrl) throws IOException, URISyntaxException
    {
        ModuleContextParameters moduleContextParameters = new JiraModuleContextParametersImpl();
        IFrameRenderStrategyImpl renderStrategy = (IFrameRenderStrategyImpl)iFrameRenderStrategyRegistry.get(PLUGIN_KEY, MODULE_KEY, classifier);

        final String iframeUrlStr = renderStrategy.buildUrl(moduleContextParameters);
        final URI iframeUrl = new URI(iframeUrlStr);
        final String baseUrl = iframeUrl.getScheme() + "://" + iframeUrl.getAuthority();

        assertThat(baseUrl, is(BASE_URL));
        assertThat(iframeUrl.getPath(), is(workflowUrl));
    }
}
