package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.uri.Uri;

import java.util.Map;

public abstract class AbstractContentMacro implements Macro
{
    private final String pluginKey;
    private final BaseContentMacroModuleBean macroBean;
    private final UserManager userManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    protected AbstractContentMacro(String pluginKey,
                                   BaseContentMacroModuleBean macroBean,
                                   UserManager userManager,
                                   RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                                   UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.pluginKey = pluginKey;
        this.macroBean = macroBean;
        this.userManager = userManager;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    protected Uri resolveUrlTemplate(Map<String, Object> parameters) throws Uri.UriException
    {
        return Uri.parse(urlVariableSubstitutor.replace(macroBean.getUrl(), parameters));
    }

    @Override
    public BodyType getBodyType()
    {
        return MacroEnumMapper.map(macroBean.getBodyType());
    }

    @Override
    public OutputType getOutputType()
    {
        return MacroEnumMapper.map(macroBean.getOutputType());
    }

    protected UserProfile getUser()
    {
        return userManager.getRemoteUser();
    }

    protected String getUsername()
    {
        UserProfile user = getUser();
        return null == user ? "" : user.getUsername();
    }

    protected String getPluginKey()
    {
        return pluginKey;
    }

    protected RemotablePluginAccessor getRemotablePluginAccessor()
    {
        return remotablePluginAccessorFactory.get(getPluginKey());
    }
}
