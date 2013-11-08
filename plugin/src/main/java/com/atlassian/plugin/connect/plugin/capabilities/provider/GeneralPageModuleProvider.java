package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.google.common.collect.ImmutableMap;

public class GeneralPageModuleProvider extends AbstractConnectPageModuleProvider
{
    public GeneralPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     IFramePageServletDescriptorFactory servletDescriptorFactory)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, "atl.general", "",
                ImmutableMap.<String, String>of(), new AlwaysDisplayCondition());

        // TODO what becomes of the context param bit of => .setDecorator("atl.general").addIframeContextParam("general", "1");
    }
}
