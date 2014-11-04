package it.com.atlassian.plugin.connect.provider.jira;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants.*;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderImpl.IFrameRenderStrategyImpl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionModuleProviderTest
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String PLUGIN_NAME = "My Plugin";
    private static final String MODULE_NAME = "My Post Function";
    private static final String MODULE_KEY = "my-post-function";
    private static final String BASE_URL = "http://my.connect.addon.com";

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
                .withView(new UrlBean("/view"))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("jiraWorkflowPostFunctions", bean)
                .build();

        // simulate a descriptor that includes an absolute url
        addon = ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(ConnectModulesGsonFactory.addonBeanToJson(addon).replace("/view", BASE_URL + "/view"), null);

        plugin = testPluginInstaller.installAddon(addon);
    }

    @AfterClass
    public void cleanup() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
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

    @Test
    public void uiParamsNotInUrlWhenNotProvided() throws URISyntaxException
    {
        ModuleContextParameters moduleContextParameters = new HashMapModuleContextParameters();
        IFrameRenderStrategyImpl renderStrategy = (IFrameRenderStrategyImpl)iFrameRenderStrategyRegistry.get(PLUGIN_KEY, MODULE_KEY, RESOURCE_NAME_INPUT_PARAMETERS);
        final String iframeUrlStr = renderStrategy.buildUrl(moduleContextParameters, Option.<String>none());
        final URI iframeUrl = new URI(iframeUrlStr);
        assertThat(iframeUrl.getQuery(), not(containsString("ui-params")));
    }

    @Test
    public void uiParamsInUrlWhenProvided() throws URISyntaxException
    {
        ModuleContextParameters moduleContextParameters = new HashMapModuleContextParameters();
        IFrameRenderStrategyImpl renderStrategy = (IFrameRenderStrategyImpl)iFrameRenderStrategyRegistry.get(PLUGIN_KEY, MODULE_KEY, RESOURCE_NAME_INPUT_PARAMETERS);
        final String iframeUrlStr = renderStrategy.buildUrl(moduleContextParameters, Option.<String>some("blah"));
        final URI iframeUrl = new URI(iframeUrlStr);
        assertThat(iframeUrl.getQuery(), containsString("ui-params=blah"));
    }

    private void checkWorkflowUrlIsAbsolute(String classifier, String workflowUrl) throws IOException, URISyntaxException
    {
        ModuleContextParameters moduleContextParameters = new HashMapModuleContextParameters();
        IFrameRenderStrategyImpl renderStrategy = (IFrameRenderStrategyImpl)iFrameRenderStrategyRegistry.get(PLUGIN_KEY, MODULE_KEY, classifier);

        final String iframeUrlStr = renderStrategy.buildUrl(moduleContextParameters, Option.<String>none());
        final URI iframeUrl = new URI(iframeUrlStr);
        final String baseUrl = iframeUrl.getScheme() + "://" + iframeUrl.getAuthority();

        assertThat(baseUrl, is(BASE_URL));
        assertThat(iframeUrl.getPath(), is(workflowUrl));
    }
}
