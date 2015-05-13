package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.spi.web.ProductWebPanelElementEnhancer;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

/**
 * This class is here because when autowiring a collection Spring expects at least one bean to match
 * @see com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel.WebPanelConnectModuleDescriptorFactory
 */
@Component
public class DummyWebPanelElementEnhancer implements ProductWebPanelElementEnhancer
{
    @Override
    public void enhance(final WebPanelModuleBean bean, final Element webPanelElement)
    {

    }
}
