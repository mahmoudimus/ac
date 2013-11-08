package com.atlassian.plugin.connect.plugin.capabilities.descriptor.url;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AddonUrlTemplatePairTest
{

    private static final String ADDON_PATH = "/blah";
    private static final String ONE_VARIABLE_URL = ADDON_PATH + "?pageid=${page.id}";
    private static final String PLUGIN_KEY = "FOO";

    @Test
    public void addonURLTemplateStringIsRawAddonURL()
    {
        assertThat(addonUrlTemplateString(), is(ONE_VARIABLE_URL));
    }

    @Test
    public void hostURLTemplateStringIsPrependedWithHostPath()
    {
        assertThat(hostUrlTemplateString(), is("/plugins/servlet/ac/" + PLUGIN_KEY + ONE_VARIABLE_URL));
    }

    @Test
    public void twoServletPathsAreCreated()
    {
        assertThat(servletPaths(), hasSize(2));
    }

    @Test
    public void aRawServletPathIsCreated()
    {
        assertThat(servletPaths(), hasItem("/ac/" + PLUGIN_KEY + ADDON_PATH));
    }

    @Test
    public void aWildcardedServletPathIsCreated()
    {
        assertThat(servletPaths(), hasItem("/ac/" + PLUGIN_KEY + ADDON_PATH + "/*"));
    }

    private String addonUrlTemplateString()
    {
        return addonUrlTemplateString(ONE_VARIABLE_URL, PLUGIN_KEY);
    }

    private String addonUrlTemplateString(String addonURLStr, String pluginKey)
    {
        return new AddonUrlTemplatePair(addonURLStr, pluginKey).getAddonUrlTemplate().getTemplateString();
    }

    private String hostUrlTemplateString()
    {
        return hostUrlTemplateString(ONE_VARIABLE_URL, PLUGIN_KEY);
    }

    private String hostUrlTemplateString(String addonURLStr, String pluginKey)
    {
        return new AddonUrlTemplatePair(addonURLStr, pluginKey).getHostUrlPaths().getHostUrlTemplate().getTemplateString();
    }

    private List<String> servletPaths()
    {
        return ImmutableList.copyOf(new AddonUrlTemplatePair(ONE_VARIABLE_URL, PLUGIN_KEY).getHostUrlPaths().getServletRegistrationPaths());
    }
}
