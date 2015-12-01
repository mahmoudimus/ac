package it.com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AtlassianPluginsTestRunner.class)
public class PluggableParametersExtractorTest
{

    private static final String PLUGIN_VERSION = "some-version";

    private PluggableParametersExtractor pluggableParametersExtractor;
    private TestAuthenticator authenticator;

    public PluggableParametersExtractorTest(PluggableParametersExtractor pluggableParametersExtractor,
            TestAuthenticator authenticator)
    {
        this.pluggableParametersExtractor = pluggableParametersExtractor;
        this.authenticator = authenticator;
    }

    @Before
    public void setUp()
    {
        authenticator.unauthenticate();
    }

    @Test
    public void shouldReturnNoParametersForEmptyContext()
    {
        Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(Collections.emptyMap());
        assertThat(contextParameters.keySet(), empty());
    }

    @Test
    public void shouldReturnNoParametersForAnonymous()
    {
        Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(createContextForPlugin());
        assertThat(contextParameters.keySet(), empty());
    }

    @Test
    public void shouldExtractPluginProvidedContextParameterForAdmin()
    {
        authenticator.authenticateUser("admin");
        Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(createContextForPlugin());
        assertThat(contextParameters, hasEntry("plugin.version", PLUGIN_VERSION));
    }

    private Map<String, Object> createContextForPlugin()
    {
        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(pluginInformation.getVersion()).thenReturn(PLUGIN_VERSION);

        Plugin plugin = mock(Plugin.class);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);

        return Collections.singletonMap("plugin", plugin);
    }
}
